package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.entity.Entity;

import java.util.Arrays;

@Module.Declaration(name = "Pursue", category = Category.Movement)
public class Pursue extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("Closest", "Moving"), "Closest");

    @Override
    public void onUpdate() {

        Entity target = null;

        if (mode.getValue().equalsIgnoreCase("Closest")) {
            target = PlayerUtil.findClosestTarget(6969, null, true);
        } else {
            target = PlayerUtil.findLookingPlayer(6969);
        }

        if (target != null) {
            if (mc.player.collidedHorizontally && mc.player.onGround)
                mc.player.jump();

            float rot = RotationUtil.getRotationTo(target.getPositionVector()).x;

            double[] dir = MotionUtil.forward(Math.min(MotionUtil.getBaseMoveSpeed(), mc.player.getDistance(target)), rot);

            mc.player.setVelocity(dir[0], mc.player.motionY, dir[1]);
        }
    }
}
