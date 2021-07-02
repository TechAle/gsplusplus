package com.gamesense.api.util.world.combatRewrite.ac.threads;

import com.gamesense.api.event.Phase;
import com.gamesense.api.event.events.OnUpdateWalkingPlayerEvent;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlayerPacket;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.api.util.world.combatRewrite.CrystalUtil;
import com.gamesense.api.util.world.combatRewrite.DamageUtil;
import com.gamesense.api.util.world.combatRewrite.ac.ACSettings;
import com.gamesense.api.util.world.combatRewrite.ac.ACUtil;
import com.gamesense.api.util.world.combatRewrite.ac.CrystalInfo;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityInfo;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.PlayerInfo;
import com.gamesense.client.manager.managers.AutoCrystalManager;
import com.gamesense.client.manager.managers.EntityTrackerManager;
import com.gamesense.client.manager.managers.PlayerPacketManager;
import com.gamesense.client.manager.managers.WorldCopyManager;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.misc.AutoGG;
import com.gamesense.mixin.mixins.accessor.ICPacketUseEntity;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listenable;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.gamesense.client.module.modules.combat.AutoCrystal.stopAC;

public class ACMain extends Thread implements Listenable {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final AtomicBoolean killSwitch;

    private final Timer breakTimer = new Timer();
    private final Timer placeTimer = new Timer();
    // used to make aiming smoother and less choppy
    private final Timer rotationTimer = new Timer();

    private AxisAlignedBB playerBox = null;

    private ACSettings settings = null;
    private List<PlayerInfo> targets = null;

    private volatile boolean rotating = false;
    private volatile Vec3d lastHitVec = null;

    public ACMain(@Nonnull AtomicBoolean killSwitch) {
        this.setDaemon(true);
        this.killSwitch = killSwitch;
    }

    @Override
    public void run() {
        while (!killSwitch.get()) {
            if (this.isInterrupted()) {
                return;
            }

            if (mc.player == null || mc.world == null || mc.player.isDead) {
                continue;
            }

            if (stopAC) {
                continue;
            }

            this.settings = AutoCrystalManager.INSTANCE.getSettings();
            if (settings != null) {
                if (settings.antiSuicide && settings.player.getHealth() + settings.player.getAbsorption() <= settings.antiSuicideValue) {
                    continue;
                }
                boolean shouldBreak = breakTimer.hasReached(this.settings.breakSpeed);
                boolean shouldPlace = placeTimer.hasReached(this.settings.placeSpeed);
                if (shouldBreak || shouldPlace) {
                    Vec3d playerPos = settings.player.position;
                    final double placeRange = settings.placeRange;
                    playerBox = new AxisAlignedBB(playerPos.x + placeRange, playerPos.y + placeRange, playerPos.z + placeRange, playerPos.x - placeRange, playerPos.y - placeRange, playerPos.z - placeRange);

                    // entity range is the range from each crystal
                    // so adding these together should solve problem
                    // and reduce searching time
                    final double enemyDistance = settings.enemyRange + settings.placeRange;
                    final double enemyRangeSq = (enemyDistance) * (enemyDistance);
                    this.targets = EntityTrackerManager.INSTANCE.getPlayerInfo().stream()
                            .filter(playerInfo -> playerPos.squareDistanceTo(playerInfo.position) < enemyRangeSq)
                            .filter(entity -> !basicChecksEntity(entity))
                            .filter(entity -> entity.getHealth() > 0.0f)
                            .collect(Collectors.toList());

                    if (!(this.targets.size() == 0)) {
                        boolean doneAnything = false;
                        WorldCopyManager.INSTANCE.lock.readLock().lock();
                        try {
                            if (shouldBreak && breakCrystal()) {
                                doneAnything = true;
                                breakTimer.reset();
                            }
                            if (shouldPlace) {
                                if (placeCrystal()) {
                                    doneAnything = true;
                                    placeTimer.reset();
                                } else {
                                    AutoCrystalManager.INSTANCE.setRenderInfo(null);
                                }
                            }
                        } finally {
                            WorldCopyManager.INSTANCE.lock.readLock().unlock();
                        }

                        if (doneAnything) {
                            rotationTimer.reset();
                        }
                    }
                }
            }

            if (rotationTimer.sleep(100)) {
                this.rotating = false;
                this.lastHitVec = null;
            }

            Thread.yield();
        }
    }

    public boolean breakCrystal() {
        List<EntityInfo> crystals = EntityTrackerManager.INSTANCE.getEntitiesInRange(settings.player.position, settings.breakRange);
        // remove all crystals that deal more than max self damage
        // and all crystals outside of break range
        // no point in checking these
        final PlayerInfo self = settings.player;
        final double breakRangeSq = settings.breakRange * settings.breakRange;
        crystals.removeIf(crystal -> {
            if (!crystal.isCrystal) {
                return true;
            }
            float damage = DamageUtil.calculateDamageThreaded(crystal.position.x, crystal.position.y, crystal.position.z, self);
            if (damage > settings.maxSelfDmg) {
                return true;
            } else {
                return (settings.antiSuicide && damage > self.getHealth()) || self.position.squareDistanceTo(crystal.position) >= breakRangeSq;
            }
        });

        TreeSet<CrystalInfo.BreakInfo> possibleCrystals;
        String crystalPriorityValue = settings.crystalPriority;
        if (crystalPriorityValue.equalsIgnoreCase("Health")) {
            possibleCrystals = new TreeSet<>(Comparator.comparingDouble((i) -> -i.target.getHealth()));
        } else if (crystalPriorityValue.equalsIgnoreCase("Closest")) {
            possibleCrystals = new TreeSet<>(Comparator.comparingDouble((i) -> -settings.player.position.squareDistanceTo(i.target.position)));
        } else {
            possibleCrystals = new TreeSet<>(Comparator.comparingDouble((i) -> i.damage));
        }

        for (PlayerInfo target : targets) {
            CrystalInfo.BreakInfo breakInfo = ACUtil.calculateBestBreakable(settings, target, crystals);
            if (breakInfo != null) {
                possibleCrystals.add(breakInfo);
            }
        }
        if (possibleCrystals.size() == 0) {
            return false;
        }

        EntityInfo crystal = possibleCrystals.last().crystal;
        if (settings.antiWeakness && mc.player.isPotionActive(MobEffects.WEAKNESS)) {
            // search for sword and tools in hotbar
            int toolSlot = InventoryUtil.findFirstItemSlot(ItemSword.class, 0, 8);
            if (toolSlot == -1) {
                InventoryUtil.findFirstItemSlot(ItemTool.class, 0, 8);
            }
            // check if any swords or tools were found
            if (toolSlot != -1) {
                switchItem(toolSlot);
                return true;
            }
        }

        this.rotating = settings.rotate;
        this.lastHitVec = crystal.position;

        swingArm(settings);
        attackCrystal(crystal.entityID);
        return true;
    }

    private boolean placeCrystal() {
        // check to see if we are holding crystals or not
        int crystalSlot = mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL ? mc.player.inventory.currentItem : -1;
        if (crystalSlot == -1) {
            crystalSlot = InventoryUtil.findFirstItemSlot(ItemEndCrystal.class, 0, 8);
        }
        boolean offhand = false;
        if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            offhand = true;
        } else if (crystalSlot == -1) {
            return false;
        }

        List<BlockPos> placements = CrystalUtil.findCrystalBlocks((float) settings.placeRange, settings.endCrystalMode, EntityTrackerManager.INSTANCE.getEntitiesInAABB(playerBox));
        // remove all placements that deal more than max self damage
        // no point in checking these
        placements.removeIf(crystal -> {
            float damage = DamageUtil.calculateDamageThreaded((double) crystal.getX() + 0.5d, (double) crystal.getY() + 1.0d, (double) crystal.getZ() + 0.5d, settings.player);
            if (damage > settings.maxSelfDmg) {
                return true;
            } else return settings.antiSuicide && damage > settings.player.getHealth();
        });

        TreeSet<CrystalInfo.PlaceInfo> possiblePlacements;
        String crystalPriorityValue = settings.crystalPriority;
        if (crystalPriorityValue.equalsIgnoreCase("Health")) {
            possiblePlacements = new TreeSet<>(Comparator.comparingDouble((i) -> -i.target.getHealth()));
        } else if (crystalPriorityValue.equalsIgnoreCase("Closest")) {
            possiblePlacements = new TreeSet<>(Comparator.comparingDouble((i) -> -settings.player.position.squareDistanceTo(i.target.position)));
        } else {
            possiblePlacements = new TreeSet<>(Comparator.comparingDouble((i) -> i.damage));
        }

        for (PlayerInfo target: targets) {
            CrystalInfo.PlaceInfo placeInfo = ACUtil.calculateBestPlacement(settings, target, placements);
            if (placeInfo != null) {
                possiblePlacements.add(placeInfo);
            }
        }
        if (possiblePlacements.size() == 0) {
            return false;
        }

        CrystalInfo.PlaceInfo crystal = possiblePlacements.last();
        AutoCrystalManager.INSTANCE.setRenderInfo(crystal);

        // autoSwitch stuff
        if (!offhand && mc.player.inventory.currentItem != crystalSlot) {
            if (settings.autoSwitch) {
                if (!settings.noGapSwitch || !(mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_APPLE)) {
                    switchItem(crystalSlot);
                    // force us to wait
                    return true;
                }
            }
            return false;
        }

        this.rotating = settings.rotate;
        this.lastHitVec = new Vec3d(crystal.crystal).add(0.5, 1.0, 0.5);

        RayTraceResult result = DamageUtil.rayTraceBlocks(settings.player.position.add(0, settings.player.getEyeHeight(), 0), new Vec3d((double) crystal.crystal.getX() + 0.5d, (double) crystal.crystal.getY() - 0.5D, (double) crystal.crystal.getZ() + 0.5d), WorldCopyManager.INSTANCE);
        EnumFacing enumFacing = null;
        if (result != null) {
            enumFacing = result.sideHit;
            this.lastHitVec = result.hitVec;
        } else {
            if (settings.raytrace) {
                AutoCrystalManager.INSTANCE.setRenderInfo(null);
                return false;
            }
        }

        //AutoCrystalManager.INSTANCE.onPlaceCrystal(crystal.crystal);

        mc.player.connection.sendPacket(new CPacketAnimation(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
        if (enumFacing != null) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(crystal.crystal, enumFacing, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
        } else if (crystal.crystal.getY() == 255) {
            // For Hoosiers. This is how we do build height. If the target block (q) is at Y 255. Then we send a placement packet to the bottom part of the block. Thus the EnumFacing.DOWN.
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(crystal.crystal, EnumFacing.DOWN, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
        } else {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(crystal.crystal, EnumFacing.UP, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
        }

        if (ModuleManager.isModuleEnabled(AutoGG.class)) {
            AutoGG.INSTANCE.addTargetedPlayer(crystal.target.getName());
        }

        return true;
    }

    private void switchItem(int itemSlot) {
        mc.player.inventory.currentItem = itemSlot;
        mc.playerController.syncCurrentPlayItem();
    }

    private void attackCrystal(int entityID) {
        CPacketUseEntity packet = new CPacketUseEntity();
        ((ICPacketUseEntity) packet).setId(entityID);
        ((ICPacketUseEntity) packet).setAction(CPacketUseEntity.Action.ATTACK);
        mc.player.connection.sendPacket(packet);
    }

    private boolean basicChecksEntity(PlayerInfo pl) {
        String name = pl.getName();
        return name.equals(mc.player.getName()) || SocialManager.isFriend(name);
    }

    private void swingArm(@Nonnull ACSettings settings) {
        switch (settings.handBreak) {
            case "Both" : {
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.OFF_HAND));
                break;
            }
            case "Offhand": {
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.OFF_HAND));
                break;
            }
            default: {
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                break;
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {
        if (event.getPhase() != Phase.PRE || !this.rotating) return;

        Vec2f rotation = RotationUtil.getRotationTo(lastHitVec);
        if (rotation == null) {
            return;
        }
        PlayerPacket packet = new PlayerPacket(100, null, rotation);
        PlayerPacketManager.INSTANCE.addPacket(packet);
    });
}
