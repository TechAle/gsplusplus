package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;

import java.util.List;

/**
 * @author Hoosiers
 * @author 0b00101010
 * @author Madmegsox1
 * @author Doogie13
 * @since 12/14/2020
 * @since 24/01/2021
 * @since 17/03/2021
 * @since 09/09/2021
 */

@Module.Declaration(name = "PacketXP", category = Category.Combat)
public class PacketXP extends Module {

    public boolean pause;
    BooleanSetting sneakOnly = registerBoolean("Sneak Only", true);
    BooleanSetting noEntityCollision = registerBoolean("No Collision", true);
    BooleanSetting silentSwitch = registerBoolean("Silent Switch", true);
    IntegerSetting minDamage = registerInteger("Min Damage", 50, 1, 100);
    IntegerSetting maxHeal = registerInteger("Repair To", 90, 1, 100);
    BooleanSetting predict = registerBoolean("Predict", false);
    /*
     * each armour slot is represented by a bit
     * 1 for needs healing
     * only the lower 4 bits are used
     * it is stored like this as toMend > 0
     * can be used to check to see if we need to mend
     * simplifying the logic
     */
    char toMend = 0;

    public void onUpdate() {
        if (mc.player == null || mc.world == null || mc.player.ticksExisted < 10) {
            return;
        }


        int sumOfDamage = 0;

        List<ItemStack> armour = mc.player.inventory.armorInventory;
        for (int i = 0; i < armour.size(); i++) {
            ItemStack itemStack = armour.get(i);
            if (itemStack.isEmpty) {
                continue;
            }


            //this works better than my calculation for some reason, thank you ArmorHUD.java
            float damageOnArmor = (float) (itemStack.getMaxDamage() - itemStack.getItemDamage());
            float damagePercent = 100 - (100 * (1 - damageOnArmor / itemStack.getMaxDamage()));

            if (damagePercent <= maxHeal.getValue()) {
                if (damagePercent <= minDamage.getValue()) {
                    toMend |= 1 << i;
                }
                if (predict.getValue()) {
                    sumOfDamage += (itemStack.getMaxDamage() * maxHeal.getValue() / 100f) - (itemStack.getMaxDamage() - itemStack.getItemDamage());
                }
            } else {
                toMend &= ~(1 << i);
            }
        }

        if (toMend > 0) {
            pause = true;
            if (predict.getValue()) {
                // get all the xp orbs on top of us
                int totalXp = mc.world.loadedEntityList.stream()
                        .filter(entity -> entity instanceof EntityXPOrb)
                        .filter(entity -> entity.getDistanceSq(mc.player) <= 1)
                        .mapToInt(entity -> ((EntityXPOrb) entity).xpValue).sum();

                // see EntityXpOrbxpToDurability(int xp)
                if ((totalXp * 2) < sumOfDamage) {
                    mendArmor(mc.player.inventory.currentItem);
                }
            } else {
                mendArmor(mc.player.inventory.currentItem);
            }
        } else
            disable();
    }

    private void mendArmor(int oldSlot) {
        if (noEntityCollision.getValue()) {
            for (EntityPlayer entityPlayer : mc.world.playerEntities) {
                if (entityPlayer.getDistance(mc.player) < 1 && entityPlayer != mc.player) {
                    return;
                }
            }
        }

        if (sneakOnly.getValue() && !mc.player.isSneaking()) {
            return;
        }

        int newSlot = findXPSlot();

        if (newSlot == -1) {
            return;
        }

        if (oldSlot != newSlot) {
            if (silentSwitch.getValue()) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));
            } else {
                mc.player.inventory.currentItem = newSlot;
            }
            mc.playerController.syncCurrentPlayItem();
        }

        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(0, 90, true));
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        if (silentSwitch.getValue()) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
        } else {
            mc.player.inventory.currentItem = oldSlot;
        }
        mc.playerController.syncCurrentPlayItem();

    }

    private int findXPSlot() {
        int slot = -1;

        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.EXPERIENCE_BOTTLE) {
                slot = i;
                break;
            }
        }

        return slot;
    }

    void handleArmour(EntityPlayer e) {

        for (int i = 5; i <= 9; i++) {
            if (percDmg(e.inventory.getStackInSlot(i)))
                rem(i);
        }
    }

    boolean percDmg(ItemStack it) {

        if (it.getItemDamage() != 0)
            return (it.getItemDamage() / it.getMaxDamage()) * 100 >= maxHeal.getValue();
        else
            return true;

    }

    void rem(int i) {

        InventoryUtil.swap(i, i-4);

    }

}