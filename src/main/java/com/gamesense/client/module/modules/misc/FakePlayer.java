package com.gamesense.client.module.modules.misc;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.TotemPopEvent;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.combat.DamageUtil;
import com.gamesense.api.util.world.combat.ac.PlayerInfo;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.modules.combat.PistonCrystal;
import com.mojang.authlib.GameProfile;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.particle.ParticleTotem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/*
    @Author TechAle
    @Since 23/03/21
 */

@Module.Declaration(name = "FakePlayer", category = Category.Misc)
public class FakePlayer extends Module {

    final private ItemStack[] armors = new ItemStack[]{
        new ItemStack(Items.DIAMOND_BOOTS),
        new ItemStack(Items.DIAMOND_LEGGINGS),
        new ItemStack(Items.DIAMOND_CHESTPLATE),
        new ItemStack(Items.DIAMOND_HELMET)
    };

    BooleanSetting copyInventory = registerBoolean("Copy Inventory", false);
    BooleanSetting playerStacked = registerBoolean("Player Stacked", false, () -> !copyInventory.getValue());
    BooleanSetting onShift = registerBoolean("On Shift", false);
    BooleanSetting simulateDamage = registerBoolean("Simulate Damage", false);
    StringSetting nameFakePlayer = registerString("Name FakePlayer", "fit");
    IntegerSetting vulnerabilityTick = registerInteger("Vulnerability Tick", 4, 0, 10);
    IntegerSetting resetHealth = registerInteger("Reset Health", 10, 0, 36);
    IntegerSetting tickRegenVal = registerInteger("Tick Regen", 4, 0, 30);
    IntegerSetting startHealth = registerInteger("Start Health", 20, 0, 30);
    ModeSetting moving = registerMode("Moving", Arrays.asList("None", "Line", "Circle", "Random"), "None");
    DoubleSetting speed = registerDouble("Speed", .36, 0, 4, () -> !(moving.getValue().equals("None") && moving.getValue().equals("Random")));
    DoubleSetting range = registerDouble("Range", 3, 0, 14, () -> moving.getValue().equals("Circle"));
    BooleanSetting followPlayer = registerBoolean("Follow Player", true, () -> moving.getValue().equals("Line"));

    int incr;
    public void onEnable() {
        incr = 0;
        beforePressed = false;
        if (mc.player == null || mc.player.isDead) {
            disable();
            return;
        }
        if (!onShift.getValue())
        spawnPlayer();
    }

    void spawnPlayer() {
        // Clone empty player
        EntityOtherPlayerMP clonedPlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("fdee323e-7f0c-4c15-8d1c-0f277442342a"), nameFakePlayer.getText() + incr));
        // Copy angles
        clonedPlayer.copyLocationAndAnglesFrom(mc.player);
        clonedPlayer.rotationYawHead = mc.player.rotationYawHead;
        clonedPlayer.rotationYaw = mc.player.rotationYaw;
        clonedPlayer.rotationPitch = mc.player.rotationPitch;
        // set gameType
        clonedPlayer.setGameType(GameType.SURVIVAL);
        clonedPlayer.setHealth(startHealth.getValue());
        // Add entity id
        mc.world.addEntityToWorld((-1234 + incr), clonedPlayer);
        incr++;
        // Set invenotry
        if (copyInventory.getValue())
            clonedPlayer.inventory.copyInventory(mc.player.inventory);
        else
        // If enchants
        if (playerStacked.getValue()) {
            // Iterate
            for (int i = 0; i < 4; i++) {
                // Create base
                ItemStack item = armors[i];
                // Add enchants
                item.addEnchantment(
                        i == 2 ? Enchantments.BLAST_PROTECTION : Enchantments.PROTECTION,
                        4);
                // Add it to the player
                clonedPlayer.inventory.armorInventory.set(i, item);
            }
        }
        listPlayers.add(new playerInfo(clonedPlayer.getName()));
        if (!moving.getValue().equals("None"))
            manager.addPlayer(clonedPlayer.entityId, moving.getValue(), speed.getValue(),
                    moving.getValue().equals("Line") ? (
                                getDirection()
                            ) : -1, range.getValue(), followPlayer.getValue());
    }
    boolean beforePressed;
    @Override
    public void onUpdate() {
        // OnShift add
        if (onShift.getValue() && mc.gameSettings.keyBindSneak.isPressed() && !beforePressed) {
            beforePressed = true;
            spawnPlayer();
        } else beforePressed = false;

        // Update tick explosion
        for(int i = 0; i < listPlayers.size(); i++) {
            if (listPlayers.get(i).update()) {
                int finalI = i;
                Optional<EntityPlayer> temp = mc.world.playerEntities.stream().filter(
                        e -> e.getName().equals(listPlayers.get(finalI).name)
                ).findAny();
                if (temp.isPresent())
                    if (temp.get().getHealth() < 20)
                        temp.get().setHealth(temp.get().getHealth() + 1);
            }
        }

        manager.update();
    }

    int getDirection() {
        int yaw = (int) RotationUtil.normalizeAngle(mc.player.getPitchYaw().y);



        if (yaw<0)              //due to the yaw running a -360 to positive 360

            yaw+=360;    //not sure why it's that way



        yaw+=22;    //centers coordinates you may want to drop this line

        yaw%=360;  //and this one if you want a strict interpretation of the zones



       return yaw/45;  //  360degrees divided by 45 == 8 zones
    }

    ArrayList<playerInfo> listPlayers = new ArrayList<>();
    class playerInfo {
        final String name;
        int tickPop = -1;
        int tickRegen = 0;


        public playerInfo(String name) {
            this.name = name;
        }

        boolean update() {
            if (tickPop != -1) {
                if (++tickPop >= vulnerabilityTick.getValue())
                    tickPop = -1;
            }
            if (++tickRegen >= tickRegenVal.getValue()) {
                tickRegen = 0;
                return true;
            } else return false;
        }

        boolean canPop() {
            return this.tickPop == -1;
        }
    }

    static class movingPlayer {
        private final int id;
        private final String type;
        private final double speed;
        private final int direction;
        private final double range;
        private final boolean follow;
        int rad = 0;

        public movingPlayer(int id, String type, double speed, int direction, double range, boolean follow) {
            this.id = id;
            this.type = type;
            this.speed = speed;
            this.direction = Math.abs(direction);
            this.range = range;
            this.follow = follow;
        }

        void move() {
            Entity player = mc.world.getEntityByID(id);
            if (player != null) {
                switch (type) {
                    case "Line":

                        double posX = follow ? mc.player.posX : player.posX,
                                posY = follow ? mc.player.posY : player.posY,
                                 posZ = follow ? mc.player.posZ : player.posZ;


                        switch (direction) {
                            case 0:
                                posZ += speed;
                                break;
                            case 1:
                                posX -= speed/2;
                                posZ += speed/2;
                                break;
                            case 2:
                                posX -= speed/2;
                                break;
                            case 3:
                                posZ -= speed/2;
                                posX -= speed/2;
                                break;
                            case 4:
                                posZ -= speed;
                                break;
                            case 5:
                                posX += speed/2;
                                posZ -= speed/2;
                                break;
                            case 6:
                                posX += speed;
                                break;
                            case 7:
                                posZ += speed/2;
                                posX += speed/2;
                                break;
                        }

                        if (BlockUtil.getBlock(posX, posY, posZ) instanceof BlockAir) {
                            for(int i = 0; i < 5; i++) {
                                if (BlockUtil.getBlock(posX, posY - 1, posZ) instanceof BlockAir) {
                                    posY--;
                                } else break;
                            }
                        } else {
                            for(int i = 0; i < 5; i++) {
                                if (!(BlockUtil.getBlock(posX, posY, posZ) instanceof BlockAir)) {
                                    posY++;
                                } else break;
                            }
                        }

                        player.setPosition(
                                posX,
                                posY,
                                posZ
                        );
                        break;
                    case "Circle":

                        double posXCir = Math.cos(rad/100.0) * range + mc.player.posX, posZCir = Math.sin(rad/100.0) * range + mc.player.posZ, posYCir = mc.player.posY;

                        if (BlockUtil.getBlock(posXCir, posYCir, posZCir) instanceof BlockAir) {
                            for(int i = 0; i < 5; i++) {
                                if (BlockUtil.getBlock(posXCir, posYCir - 1, posZCir) instanceof BlockAir) {
                                    posYCir--;
                                } else break;
                            }
                        } else {
                            for(int i = 0; i < 5; i++) {
                                if (!(BlockUtil.getBlock(posXCir, posYCir, posZCir) instanceof BlockAir)) {
                                    posYCir++;
                                } else break;
                            }
                        }

                        player.setPosition(
                                posXCir,
                                posYCir,
                                posZCir
                        );
                        rad += speed * 10;
                        break;
                    case "Random":
                        break;
                }
            }
        }

    }

    static class movingManager {
        private final ArrayList<movingPlayer> players = new ArrayList<>();

        void addPlayer(int id, String type, double speed, int direction, double range, boolean follow) {
            players.add(new movingPlayer(id, type, speed, direction, range, follow));
        }

        void update() {
            this.players.forEach(movingPlayer::move);
        }

        void remove() {
            players.clear();
        }
    }

    movingManager manager = new movingManager();

    public void onDisable() {
        if (mc.world != null) {
            for(int i = 0; i < incr; i++) {
                mc.world.removeEntityFromWorld((-1234 + i));
            }
        }
        listPlayers.clear();
        manager.remove();
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Receive> packetReceiveListener = new Listener<>(event -> {
        // Simple crystal damage
        if (simulateDamage.getValue()) {
            Packet<?> packet = event.getPacket();
            if (packet instanceof SPacketSoundEffect) {
                final SPacketSoundEffect packetSoundEffect = (SPacketSoundEffect) packet;
                if (packetSoundEffect.getCategory() == SoundCategory.BLOCKS && packetSoundEffect.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                    for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {
                        if (entity instanceof EntityEnderCrystal) {
                            if (entity.getDistanceSq(packetSoundEffect.getX(), packetSoundEffect.getY(), packetSoundEffect.getZ()) <= 36.0f) {
                                for (EntityPlayer entityPlayer : mc.world.playerEntities) {
                                    // If the player is like we want to be
                                    if (entityPlayer.getName().split(nameFakePlayer.getText()).length == 2) {

                                        Optional<playerInfo> temp = listPlayers.stream().filter(
                                                e -> e.name.equals(entityPlayer.getName())
                                        ).findAny();
                                        // If he is in wait, continue
                                        if (!temp.isPresent() || !temp.get().canPop())
                                            continue;

                                        // Calculate damage
                                        float damage = DamageUtil.calculateDamage(packetSoundEffect.getX(), packetSoundEffect.getY(), packetSoundEffect.getZ(), entityPlayer);
                                        if (damage > entityPlayer.getHealth()) {
                                            // If higher, new health and pop
                                            entityPlayer.setHealth(resetHealth.getValue());
                                            mc.effectRenderer.emitParticleAtEntity(entityPlayer, EnumParticleTypes.TOTEM, 30);
                                            mc.world.playSound(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0F, 1.0F, false);
                                            GameSense.EVENT_BUS.post(new TotemPopEvent(entityPlayer));
                                        // Else, setHealth
                                        } else entityPlayer.setHealth(entityPlayer.getHealth() - damage);

                                        // Add vulnerability
                                        temp.get().tickPop = 0;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    });
}
