package com.gamesense.client.module.modules.misc;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.TotemPopEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.StringSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.combat.DamageUtil;
import com.gamesense.api.util.world.combat.ac.PlayerInfo;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.modules.combat.PistonCrystal;
import com.mojang.authlib.GameProfile;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
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
import net.minecraft.world.GameType;

import java.util.ArrayList;
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

    public void onDisable() {
        if (mc.world != null) {
            for(int i = 0; i < incr; i++) {
                mc.world.removeEntityFromWorld((-1234 + i));
            }
        }
        listPlayers.clear();
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
