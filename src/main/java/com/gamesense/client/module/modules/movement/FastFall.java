package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "FastFall", category = Category.Movement)
public class FastFall extends Module {

    DoubleSetting dist = registerDouble("Min Distance", 3,0,25);
    DoubleSetting speed = registerDouble("Multiplier", 3,0,10);

    @Override
    public void onUpdate() {
        if (mc.player.onGround || mc.player.isElytraFlying() || mc.player.isInLava() || mc.player.isInWater() || mc.player.isInWeb || mc.player.fallDistance < dist.getValue() || mc.player.capabilities.isFlying)
            mc.player.motionY -= speed.getValue();

    }
}
