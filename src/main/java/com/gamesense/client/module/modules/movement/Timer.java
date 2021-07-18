package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;

@Module.Declaration(name = "Timer", category = Category.Movement)
public class Timer extends Module {

    String arraylistSpeed;

    DoubleSetting speed = registerDouble("speed", 2, 0.1, 50);

    float speedDouble;

    public void onDisable() {
        Minecraft.getMinecraft().timer.tickLength = 50;
    }

    @Override
    public void onUpdate() {
        speedDouble = speed.getValue().floatValue();
        Minecraft.getMinecraft().timer.tickLength = 50.0f / speedDouble;
    }
    public String getHudInfo() {
        arraylistSpeed = "";

            arraylistSpeed = "[" + ChatFormatting.WHITE + speed.getValue() + ChatFormatting.GRAY + "]";

        return arraylistSpeed;
    }
}