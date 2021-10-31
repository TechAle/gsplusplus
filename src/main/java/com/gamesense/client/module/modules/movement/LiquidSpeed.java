package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.combat.PistonCrystal;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketEntityAction;

import java.util.Arrays;

/**
 * @author TechAle
 */

@Module.Declaration(name = "Liquid Speed", category = Category.Movement)
public class LiquidSpeed extends Module {

    DoubleSetting timerVal = registerDouble("Timer Speed", 1, 1, 2);
    DoubleSetting XZWater = registerDouble("XZ Water", 1, 1, 5);
    DoubleSetting YPWater = registerDouble("Y+ Water", 1, 1, 5);
    DoubleSetting YMWater = registerDouble("Y- Water", 1, 0, 10);
    ModeSetting YWaterMotion = registerMode("Y Water motion", Arrays.asList("None", "Zero", "Bounding", "Min"), "None");
    IntegerSetting magnitudeMinWater = registerInteger("Magnitude Min Water", 0, 0, 6);
    DoubleSetting XZLava = registerDouble("XZ Lava", 1, 1, 5);
    DoubleSetting YPLava = registerDouble("Y+ Lava", 1, 1, 5);
    DoubleSetting YMLava = registerDouble("Y- Lava", 1, 0, 10);
    ModeSetting YLavaMotion = registerMode("Y Lava motion", Arrays.asList("None", "Zero", "Bounding", "Min"), "None");
    IntegerSetting magnitudeMinLava = registerInteger("Magnitude Min Lava", 0, 0, 6);
    BooleanSetting groundIgnore = registerBoolean("Ground Ignore", true);


    private boolean slowDown;
    private double playerSpeed;
    private final Timer timer = new Timer();
    boolean beforeUp = true;

    public void onEnable() {
        playerSpeed = MotionUtil.getBaseMoveSpeed();
    }

    public void onDisable() {
        timer.reset();
        EntityUtil.resetTimer();
    }


    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {
        if (mc.player == null || mc.world == null) {
            return;
        }

        // Get if we want to go up or down
        Boolean isMovingUp = mc.gameSettings.keyBindJump.isKeyDown();
        Boolean isMovingDown = mc.gameSettings.keyBindSneak.isKeyDown();

        // Get speed
        Double velX = event.getX();
        Double velY = event.getY(), memY = velY;
        Double velZ = event.getZ();

        // Some servers doesnt like speed while onGround lol
        if (groundIgnore.getValue() || !mc.player.onGround) {

            // If water
            if (mc.player.isInWater()) {
                // Set timer
                if (!ModuleManager.isModuleEnabled(TickShift.class) && timerVal.getValue() != 1) {
                    EntityUtil.setTimer(timerVal.getValue().floatValue());
                }
                // Add vel
                velX *= XZWater.getValue();
                // We split goUp and goDown
                velY *= isMovingUp ? YPWater.getValue() : YMWater.getValue();
                velZ *= XZWater.getValue();
                // If we are moving up or down
                if (!isMovingUp && !isMovingDown)
                    // Iterate for YWaterMotion
                    switch (YWaterMotion.getValue()) {
                        case "Zero":
                            // Set to 0
                            velY = 0.0;
                            break;
                        case "Bounding":
                            // First up then down
                            velY = memY;
                            if (beforeUp)
                                velY *= -1;
                            beforeUp = !beforeUp;
                            break;
                        case "Min":
                            // Like before but u can decide the y difference
                            velY = getMagnitude(magnitudeMinWater.getValue());
                            if (beforeUp)
                                velY *= -1;
                            beforeUp = !beforeUp;
                            break;
                    }
            }

            // Same as water
            if (mc.player.isInLava()) {
                if (!ModuleManager.isModuleEnabled(TickShift.class) && timerVal.getValue() != 1) {
                    EntityUtil.setTimer(timerVal.getValue().floatValue());
                }
                velX *= XZLava.getValue();
                velY *= isMovingUp ? YPLava.getValue() : YMLava.getValue();
                velZ *= XZLava.getValue();
                if (!isMovingUp && !isMovingDown)
                    switch (YLavaMotion.getValue()) {
                        case "Zero":
                            velY = 0.0;
                            break;
                        case "Bounding":
                            velY = memY;
                            if (beforeUp)
                                velY *= -1;
                            beforeUp = !beforeUp;
                            break;
                        case "Min":
                            velY = getMagnitude(magnitudeMinLava.getValue());
                            if (beforeUp)
                                velY *= -1;
                            beforeUp = !beforeUp;
                            break;
                    }
            }

        }

        // Set everything
        event.setX(velX);
        event.setY(velY);
        event.setZ(velZ);


    });

    double getMagnitude(int level) {
        return 1/(Math.pow(10, (int) level / 2));
    }


}