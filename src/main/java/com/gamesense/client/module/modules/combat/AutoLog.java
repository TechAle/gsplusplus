package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

@Module.Declaration(name = "AutoLog", category = Category.Combat)
public class AutoLog extends Module {

    IntegerSetting tots = registerInteger("Totems", 1, 0, mc.player.inventory.getSizeInventory());
    IntegerSetting hp = registerInteger("Health", 12, 0, 36);

    @Override
    public void onUpdate() {
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > hp.getValue()
                && mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum() < tots.getValue()) {
            mc.player.connection.getNetworkManager().handleDisconnection();
        }
    }
}
