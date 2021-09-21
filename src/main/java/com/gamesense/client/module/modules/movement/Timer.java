package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;

@Module.Declaration(name = "Timer", category = Category.Movement)
public class Timer extends Module {

    String arraylistSpeed;

    DoubleSetting speed = registerDouble("speed", 1.08, 0.1, 50);
    BooleanSetting onMove = registerBoolean("onMove",false);
    BooleanSetting onSpeedOnly = registerBoolean("onSpeedOnly", false);

    float speedDouble;

    public void onDisable() {
        Minecraft.getMinecraft().timer.tickLength = 50;
    }

    @Override
    public void onUpdate() {
        if ((!onMove.getValue() && !onSpeedOnly.getValue()|| MotionUtil.isMoving(mc.player) && onMove.getValue() && !onSpeedOnly.getValue()|| onSpeedOnly.getValue() && ModuleManager.isModuleEnabled(Speed.class) && !onMove.getValue() && MotionUtil.isMoving(mc.player)) && !(!(mc.player.onGround) && ModuleManager.getModule(PlayerTweaks.class).webT.getValue() && mc.player.isInWeb)) {
            doTimer();
        } else {
            if (!(mc.player.onGround) && ModuleManager.getModule(PlayerTweaks.class).webT.getValue() && mc.player.isInWeb)
                mc.timer.tickLength = 1;
            else{
                mc.timer.tickLength = 50;
            }
        }
    }

    public void doTimer() {
        speedDouble = speed.getValue().floatValue();
        Minecraft.getMinecraft().timer.tickLength = 50.0f / speedDouble;
    }
    public String getHudInfo() {
        arraylistSpeed = "";

            arraylistSpeed = "[" + ChatFormatting.WHITE + (Math.round(50 / mc.timer.tickLength * 100.0) / 100.0) + ChatFormatting.GRAY + "]";

        return arraylistSpeed;
    }
}