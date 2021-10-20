package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.init.Blocks;

@Module.Declaration(name = "IceSpeed", category = Category.Movement)
public class IceSpeed extends Module {

    DoubleSetting slipperiness = registerDouble("Slipperiness", 0.4,0,1);

    @Override
    public void onUpdate() {

        Blocks.ICE.setDefaultSlipperiness(slipperiness.getValue().floatValue());
        Blocks.PACKED_ICE.setDefaultSlipperiness(slipperiness.getValue().floatValue());
        Blocks.FROSTED_ICE.setDefaultSlipperiness(slipperiness.getValue().floatValue());

    }

    @Override
    protected void onDisable() {

        Blocks.ICE.setDefaultSlipperiness(0.98f);
        Blocks.PACKED_ICE.setDefaultSlipperiness(0.98f);
        Blocks.FROSTED_ICE.setDefaultSlipperiness(0.98f);

    }
}
