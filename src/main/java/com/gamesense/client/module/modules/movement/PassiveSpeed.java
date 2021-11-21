package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;

@Module.Declaration(name = "PassiveSpeed", category = Category.Movement)
public class PassiveSpeed extends Module {

    DoubleSetting speed = registerDouble("Speed", 1.1,1,2);

    @Override
    public void onUpdate() {
        if (!mc.player.onGround && MotionUtil.getMotion(mc.player) != 0 && !ModuleManager.isModuleEnabled(LongJump.class)) {

            mc.player.jumpMovementFactor = ((float) (0.02 * speed.getValue().floatValue()));

        }
    }
}
