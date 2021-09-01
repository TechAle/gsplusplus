package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.MobEffects;

import java.util.Arrays;

@Module.Declaration(name = "Long Jump", category = Category.Movement)
public class LongJump extends Module {

    ModeSetting mode = registerMode("mode", Arrays.asList("Strafe", "Far"), "Strafe");
    DoubleSetting speed = registerDouble("strafeSpeed", 2.15, 0, 10, () -> mode.getValue().equalsIgnoreCase("Strafe"));
    DoubleSetting farSpeed = registerDouble("farSpeed", 1, 0, 10, () -> mode.getValue().equalsIgnoreCase("Far"));
    IntegerSetting farAccel = registerInteger("farAccelerate", 0, 1, 5, () -> mode.getValue().equalsIgnoreCase("Far"));
    DoubleSetting initialFar = registerDouble("initialFarSpeed", 1, 0, 10, () -> mode.getValue().equalsIgnoreCase("Far"));
    DoubleSetting jumpHeight = registerDouble("jumpHeight", 0.41, 0, 1, () -> mode.getValue().equalsIgnoreCase("Strafe"));

    Double playerSpeed;

    boolean slowDown;

    int i;

    private final Timer timer = new Timer();
    private final Timer farTimer = new Timer();

    public void onEnable() {
        playerSpeed = MotionUtil.getBaseMoveSpeed();
    }

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {
        if (mode.getValue().equals("Strafe")) {
            if (mc.player.isInLava() || mc.player.isInWater() || mc.player.isOnLadder() || mc.player.isInWeb || Anchor.active) {
                return;
            }
            double speedY = jumpHeight.getValue();

            if (mc.player.onGround && MotionUtil.isMoving(mc.player) && timer.hasReached(300)) {
                if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                    speedY += (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f;
                }

                event.setY(mc.player.motionY = speedY);
                playerSpeed = MotionUtil.getBaseMoveSpeed() * (EntityUtil.isColliding(0, -0.5, 0) instanceof BlockLiquid && !EntityUtil.isInLiquid() ? 0.9 : speed.getValue());
                slowDown = true;
                timer.reset();
            } else {
                if (slowDown || mc.player.collidedHorizontally) {
                    playerSpeed -= (EntityUtil.isColliding(0, -0.8, 0) instanceof BlockLiquid && !EntityUtil.isInLiquid()) ? 0.4 : 0.7 * (playerSpeed = MotionUtil.getBaseMoveSpeed());
                    slowDown = false;
                } else {
                    playerSpeed -= playerSpeed / 159.0;
                }
            }
            playerSpeed = Math.max(playerSpeed, MotionUtil.getBaseMoveSpeed());
            double[] dir = MotionUtil.forward(playerSpeed);
            event.setX(dir[0]);
            event.setZ(dir[1]);
        }
    });

    @Override
    public void onUpdate() {
        double[] dir = MotionUtil.forward(playerSpeed);
        if (mode.getValue().equalsIgnoreCase("Far")) {
            if (mc.player.onGround && mc.gameSettings.keyBindForward.isKeyDown()) {
                mc.player.motionX = dir[0] * initialFar.getValue().floatValue();
                mc.player.motionZ = dir[1] * initialFar.getValue().floatValue();
                mc.player.jump();
                i = 0;
            }
            if (mc.player.motionY == 0.0030162615090425808) {
                if (farAccel.getValue().equals(0)) {
                    mc.player.jumpMovementFactor = farSpeed.getValue().floatValue();
                } else {
                    i++;
                    mc.player.jumpMovementFactor = i * (farSpeed.getValue().floatValue() / farAccel.getValue());
                }


            }
        }
    }


    @Override
    public void onDisable() {
        timer.reset();
    }
}



