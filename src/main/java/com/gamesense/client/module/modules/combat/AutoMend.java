package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;

import java.util.Collections;
import java.util.List;

@Module.Declaration(name = "AutoMend", category = Category.Combat)
public class AutoMend extends Module {

    List<Boolean> list = Collections.emptyList();

    IntegerSetting damage = registerInteger("Damage", 75, 1, 100);
    DoubleSetting range = registerDouble("Nearby Cancel", 0, 0, 10);
    BooleanSetting silentSwitch = registerBoolean("Silent Switch", false);

    @Override
    protected void onEnable() {
        if (craftFull()) {
            MessageBus.sendClientPrefixMessageWithID("Disabled due to non empty craft slots", true);
            disable();
        }
    }

    @Override
    public void onUpdate() {

        if (PlayerUtil.findClosestTarget(range.getValue(), null) != null) {
            MessageBus.sendClientPrefixMessageWithID("Disabled due to nearby player", true);
            disable();
            return;
        }

        for (int i = 1; i < 4; i++) {
            list.set(i, mc.player.inventory.getStackInSlot(i).getItemDamage() > damage.getValue());
        }

        for (int i = 1; i < 4; i++) {
            if (list.get(i).equals(true) && mc.player.inventory.getStackInSlot(i).getItem() != Items.AIR)
                InventoryUtil.swap(i, i - 4); // -4 gets us to corresponding crafting slot }
        }

        int slot = InventoryUtil.findFirstItemSlot(Items.EXPERIENCE_BOTTLE.getClass(), 0, 8);

        if (slot == -1) {
            MessageBus.sendClientPrefixMessageWithID("Disabled due to no XP", true);
            disable();
            return;
        }

        int oldSlot = mc.player.inventory.currentItem;

        if (silentSwitch.getValue())
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
        else
            mc.player.inventory.currentItem = slot;

        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, 90, mc.player.onGround));
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));

        if (silentSwitch.getValue())
            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

    }

    boolean craftFull() {
        for (int i = 1; i < 4; i++)
            if (mc.player.inventory.getStackInSlot(i).getItem() != Items.AIR)
                return true;

        return false;
    }


}
