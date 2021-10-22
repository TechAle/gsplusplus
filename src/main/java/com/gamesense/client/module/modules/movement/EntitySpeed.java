package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.entity.EntityLivingBase;

@Module.Declaration(name = "EntitySpeed", category = Category.Movement)
public class EntitySpeed extends Module {

    DoubleSetting speed = registerDouble("Speed", 1,0,10);

    @Override
    public void onUpdate() {
        if (mc.player.ridingEntity != null) {
            if (MotionUtil.isMoving((EntityLivingBase) mc.player.ridingEntity)) {

                double[] dir = MotionUtil.forward(speed.getValue());

                mc.player.ridingEntity.motionX = dir[0];
                mc.player.ridingEntity.motionX = dir[1];

            }
        }
    }
}
