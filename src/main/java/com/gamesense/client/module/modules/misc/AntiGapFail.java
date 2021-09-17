package com.gamesense.client.module.modules.misc;

import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketHeldItemChange;

@Module.Declaration(name = "AntiGapFail", category = Category.Misc)
public class AntiGapFail extends Module {

    @Override
    public void onUpdate() {
        if (mc.player.canEat(true) && mc.player.isSwingInProgress && mc.player.getHeldItemMainhand().getItem().equals(Items.GOLDEN_APPLE)) {

            mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));

        }
    }
}
