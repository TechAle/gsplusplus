package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "PassiveSpeed", category = Category.Movement)
public class PassiveSpeed extends Module {

    DoubleSetting speed = registerDouble("Speed", 1.1,1,2);

    @Override
    public void onUpdate() {
        if (!mc.player.onGround && MotionUtil.isMoving(mc.player)) {

            mc.player.jumpMovementFactor = 0.02f * speed.getValue().floatValue();

        }
    }
}
