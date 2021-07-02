package com.gamesense.client.module.modules.misc;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.PlayerInfo;
import com.gamesense.client.manager.managers.EntityTrackerManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameType;

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

    BooleanSetting playerStacked = registerBoolean("Player Stacked", false);
    BooleanSetting onShift = registerBoolean("On Shift", false);
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
        EntityOtherPlayerMP clonedPlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("fdee323e-7f0c-4c15-8d1c-0f277442342a"), "Fit" + incr));
        clonedPlayer.copyLocationAndAnglesFrom(mc.player);
        clonedPlayer.rotationYawHead = mc.player.rotationYawHead;
        clonedPlayer.rotationYaw = mc.player.rotationYaw;
        clonedPlayer.rotationPitch = mc.player.rotationPitch;
        clonedPlayer.setGameType(GameType.SURVIVAL);
        clonedPlayer.setHealth(20);
        mc.world.addEntityToWorld((-1234 - incr), clonedPlayer);
        incr++;
        // If enchants
        if (playerStacked.getValue()) {
            // ITerate
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
        clonedPlayer.onLivingUpdate();
        EntityTrackerManager.INSTANCE.addPlayer(new PlayerInfo(clonedPlayer, 0));
    }
    boolean beforePressed;
    @Override
    public void onUpdate() {
        if (onShift.getValue() && mc.gameSettings.keyBindSneak.isPressed() && !beforePressed) {
            beforePressed = true;
            spawnPlayer();
        } else beforePressed = false;
    }

    public void onDisable() {
        if (mc.world != null) {
            for(int i = 0; i < incr; i++) {
                mc.world.removeEntityFromWorld((-1234 - i));
                EntityTrackerManager.INSTANCE.removePlayer(-1234 - i);
            }
        }
    }
}
