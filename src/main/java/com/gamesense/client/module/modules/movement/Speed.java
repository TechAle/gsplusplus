package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
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
import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Crystallinqq
 * @author Auto
 * @author Hoosiers
 * @author Doogie13
 * @Source https://github.com/Crystallinqq/Mercury-Client/blob/master/src/main/java/fail/mercury/client/client/modules/movement/Speed.java (for yport and strafe)
 */

@Module.Declaration(name = "Speed", category = Category.Movement)
public class Speed extends Module {

    private final Timer timer = new Timer();
    public int yl;
    ModeSetting mode = registerMode("Mode", Arrays.asList("Strafe", "OnGround", "Fake", "YPort", "Custom"), "Strafe");
    DoubleSetting speed = registerDouble("Speed", 2.15, 0, 10, () -> mode.getValue().equals("Strafe"));
    DoubleSetting yPortSpeed = registerDouble("Speed YPort", 0.06, 0.01, 0.15, () -> mode.getValue().equals("YPort"));
    DoubleSetting onGroundSpeed = registerDouble("Speed OnGround", 0.13, 0.01, 0.3, () -> mode.getValue().equalsIgnoreCase("OnGround"));
    DoubleSetting speedCustom = registerDouble("Speed Custom", 2, 0, 10, () -> mode.getValue().equalsIgnoreCase("Custom"));
    BooleanSetting customHop = registerBoolean("Custom Jump", false, () -> mode.getValue().equalsIgnoreCase("Custom"));
    DoubleSetting customHeight = registerDouble("Custom Height", 0.42, 0, 1, () -> mode.getValue().equalsIgnoreCase("Custom"));
    DoubleSetting jumpHeight = registerDouble("Jump Speed", 0.41, 0, 1);
    IntegerSetting jumpDelay = registerInteger("Jump Delay", 300, 0, 1000);

    int og;
    Double speedF;

    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {

        if (mode.getValue().equalsIgnoreCase("OnGround") && event.getPacket() instanceof CPacketPlayer) {

            if (mc.player.onGround) {
                double[] dir = MotionUtil.forward(onGroundSpeed.getValue());
                switch (og) {

                    case 0: {
                        ((CPacketPlayer) event.getPacket()).y = 0.42;
                        ((CPacketPlayer) event.getPacket()).onGround = false;
                        mc.player.motionX += dir[0];
                        mc.player.motionZ += dir[1];
                        og++;

                    }
                    case 1: {
                        og++;

                    }
                    case 2: {

                        og = 0;

                    }
                }
            }
        }
    });
    private boolean slowDown;
    private double playerSpeed;
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {
        if (mc.player.isInLava() || mc.player.isInWater() || mc.player.isOnLadder() || mc.player.isInWeb || Anchor.active) {
            return;
        }

        if (mode.getValue().equalsIgnoreCase("Strafe")) {
            double speedY = jumpHeight.getValue();

            if (mc.player.onGround) mc.player.jump();

            if (mc.player.onGround && MotionUtil.isMoving(mc.player) && timer.hasReached(jumpDelay.getValue())) {
                if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                    speedY += (Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST)).getAmplifier() + 1) * 0.1f;
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
                    playerSpeed -= playerSpeed / speed.getValue();
                }
            }
            playerSpeed = Math.max(playerSpeed, MotionUtil.getBaseMoveSpeed());
            double[] dir = MotionUtil.forward(playerSpeed);
            event.setX(dir[0]);
            event.setZ(dir[1]);
        } else if (mode.getValue().equalsIgnoreCase("Custom")) {

            double[] dir = MotionUtil.forward(speedCustom.getValue());
            event.setX(dir[0]);
            event.setZ(dir[1]);

            if (customHop.getValue() && mc.player.onGround) {

                mc.player.motionY = customHeight.getValue();

            }

        }

    });

    public void onEnable() {
        playerSpeed = MotionUtil.getBaseMoveSpeed();
        yl = ((int) mc.player.posY);
    }

    public void onDisable() {
        timer.reset();
    }

    public void onUpdate() {
        if (mc.player == null || mc.world == null) {
            disable();
            return;
        }

        mc.player.rotationPitch += 0.00001; // literally just get new packet that we cant even see for keepRotation to consistantly have rotation packets to use


        if (Anchor.active)
            return;

        if (mode.getValue().equalsIgnoreCase("YPort")) {
            handleYPortSpeed();
        }
    }

    private void handleYPortSpeed() {
        if (!MotionUtil.isMoving(mc.player) || mc.player.isInWater() && mc.player.isInLava() || mc.player.collidedHorizontally) {
            return;
        }

        if (mc.player.onGround) {
            mc.player.jump(); // motion = 0.42
            MotionUtil.setSpeed(mc.player, MotionUtil.getBaseMoveSpeed() + yPortSpeed.getValue()); // set speed
        } else {
            mc.player.motionY = -1; // return to ground instantly }
        }
    }

    public String getHudInfo() {
        return "[" + ChatFormatting.WHITE + mode.getValue() + ChatFormatting.GRAY + "]";
    }

}