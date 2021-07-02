package com.gamesense.client.manager.managers;

import com.gamesense.api.util.world.combatRewrite.ac.ACSettings;
import com.gamesense.api.util.world.combatRewrite.ac.CrystalInfo;
import com.gamesense.client.manager.Manager;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.combat.AutoCrystal;
import com.gamesense.client.module.modules.combat.AutoCrystalRewrite2;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public enum AutoCrystalManager implements Manager {

    INSTANCE;

    // very big numbers
    private static final EntityEnderCrystal GENERIC_CRYSTAL = new EntityEnderCrystal(null, 0b00101010 * 10^42, 0b00101010 * 10^42, 0b00101010 * 10^42);

    // Threading Stuff
    public static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    // stores all the locations we have attempted to place crystals
    // and the corresponding crystal for that location (if there is any)
    private final ConcurrentHashMap<BlockPos, EntityEnderCrystal> placedCrystals = new ConcurrentHashMap<>();

    private volatile ACSettings settings = null;
    private volatile List<BlockPos> possiblePlacements = new ArrayList<>();
    private volatile List<EntityEnderCrystal> targetableCrystals = new ArrayList<>();

    private volatile CrystalInfo.PlaceInfo renderInfo = null;

    /*
    public void startCalculations(long timeout) {
        if (mainThreadOutput != null) {
            mainThreadOutput.cancel(true);
        }
        mainThreadOutput = mainExecutors.submit(new ACCalculate(settings.get(), targetsInfo.get(), possiblePlacements.get(), timeout));
    }

    // returns null if still calculating
    // returns EMPTY_LIST if finished or not started
    public List<CrystalInfo.PlaceInfo> getOutput(boolean wait) {
        if (mainThreadOutput == null) {
            return EMPTY_LIST;
        }

        if (wait) {
            while (!(mainThreadOutput.isDone() || mainThreadOutput.isCancelled())) {
            }
        } else {
            if (!(mainThreadOutput.isDone())) {
                return null;
            }
            if (mainThreadOutput.isCancelled()) {
                return EMPTY_LIST;
            }
        }

        List<CrystalInfo.PlaceInfo> output = EMPTY_LIST;
        try {
            output = mainThreadOutput.get();
        } catch (InterruptedException | ExecutionException ignored) {
        }

        mainThreadOutput = null;
        return output;
    }
     */

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<TickEvent.ClientTickEvent> onUpdate = new Listener<>(event -> {
        Minecraft mc = getMinecraft();
        if (mc.player == null || mc.world == null) {
            /*
            settings.set(null);
            possiblePlacements.set(null);
            targetableCrystals.set(null);
            targetsInfo.set(null);
            player.set(null);
             */

            placedCrystals.clear();
            return;
        }

        this.settings = ModuleManager.getModule(AutoCrystalRewrite2.class).getSettings();
        /*
        final float armourPercent = settings.armourFacePlace/100.0f;
        final boolean own = settings.breakMode.equalsIgnoreCase("Own");
        final double breakRangeSq = settings.breakRange * settings.breakRange;
        final double enemyDistance = settings.enemyRange + settings.placeRange;
        final double entityRangeSq = (settings.enemyRange) * (enemyDistance);
        PlayerInfo self = settings.player;

        this.settings = settings;

        List<EntityEnderCrystal> crystalTargets = mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityEnderCrystal)
                .map(entity -> (EntityEnderCrystal) entity).collect(Collectors.toList());

        if (own) {
            crystalTargets.removeIf(crystal -> !placedCrystals.containsKey(EntityUtil.getPosition(crystal)));
        }
        synchronized (placedCrystals) {
            // GENERIC_CRYSTAL will always be false here
            // remove own crystals that have been destroyed
            placedCrystals.values().removeIf(crystal -> crystal.isDead);
        }

        // remove all crystals that deal more than max self damage
        // and all crystals outside of break range
        // no point in checking these
        crystalTargets.removeIf(crystal -> {
            float damage = DamageUtil.calculateDamageThreaded(crystal.posX, crystal.posY, crystal.posZ, self);
            if (damage > settings.maxSelfDmg) {
                return true;
            } else return (settings.antiSuicide && damage > self.getHealth()) || self.position.squareDistanceTo(crystal.getPositionVector()) >= breakRangeSq;
        });

        this.targetableCrystals = crystalTargets;
         */
    });

    /*
    public void onPlaceCrystal(BlockPos target) {
        BlockPos up = target.up();
        placedCrystals.put(up, GENERIC_CRYSTAL);
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<EntityJoinWorldEvent> entitySpawnListener = new Listener<>(event -> {
        Entity entity = event.getEntity();
        if (entity instanceof EntityEnderCrystal) {
            EntityEnderCrystal crystal = (EntityEnderCrystal) entity;
            BlockPos crystalPos = EntityUtil.getPosition(crystal);
            placedCrystals.computeIfPresent(crystalPos, ((i, j) -> crystal));
        }
    });
     */

    public ACSettings getSettings() {
        return settings;
    }

    public CrystalInfo.PlaceInfo getRenderInfo() {
        return renderInfo;
    }

    public void setRenderInfo(CrystalInfo.PlaceInfo renderInfo) {
        this.renderInfo = renderInfo;
    }
}
