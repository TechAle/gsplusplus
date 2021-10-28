package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

@Module.Declaration(name = "SlowFall", category = Category.Movement)
public class SlowFall extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("Timer", "Motion"), "Motion");
    DoubleSetting timer = registerDouble("Timer", 0.1,0.1,1, () -> mode.getValue().equalsIgnoreCase("Timer"));
    DoubleSetting motion = registerDouble("Motion", 1, 0, 100);

    @Override
    public void onUpdate() {
        if (mc.gameSettings.keyBindJump.isKeyDown())
            if (mode.getValue().equalsIgnoreCase("Timer"))
                mc.timer.tickLength = 50 / timer.getValue().floatValue();
            else
                mc.player.motionY = motion.getValue() / 100;
    }
}
