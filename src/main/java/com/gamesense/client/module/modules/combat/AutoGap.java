package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;

@Module.Declaration(name = "AutoGap", category = Category.Combat)
public class AutoGap extends Module {

    IntegerSetting hp = registerInteger("Health",  12,1,36);

    @Override
    public void onUpdate() {

        boolean offhand;

        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > hp.getValue()) {

            if (mc.player.inventory.offHandInventory.contains(Items.GOLDEN_APPLE))
                offhand = true;
            else {
                int item = InventoryUtil.findFirstItemSlot(Items.GOLDEN_APPLE.getClass(), 0,8);
                if (item != -1)
                mc.player.inventory.currentItem = item;
                offhand = false;
            }

            mc.playerController.processRightClick(mc.player,mc.world, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);

        }
    }
}
