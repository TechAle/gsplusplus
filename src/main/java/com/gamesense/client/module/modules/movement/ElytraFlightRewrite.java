package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

@Module.Declaration(name = "ElytraFlightRewrite", category = Category.Movement)
public class ElytraFlightRewrite extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("Control", "Boost"), "Boost");
    ModeSetting upMode = registerMode("Up Mode", Arrays.asList("Jump", "Aim"), "Jump", () -> mode.getValue().equals("Control"));
    DoubleSetting speed = registerDouble("Speed", 2.5, 0, 10);
    DoubleSetting ySpeed = registerDouble("Y Speed", 0, 1, 10);
    DoubleSetting glideSpeed = registerDouble("Glide Speed", 0, 0, 3);
    BooleanSetting setVelo = registerBoolean("Set Velocity", false, () -> mode.getValue().equals("Control"));

    boolean setAng;

    Timer upTimer = new Timer();

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (mc.player.isElytraFlying()) {

            mc.timer.tickLength = 50;

            if (mode.getValue().equals("Boost")) {
                if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindForward.isKeyDown()) {
                    float yaw = (float) Math.toRadians(mc.player.rotationYaw);
                    mc.player.motionX -= MathHelper.sin(yaw) * 0.05f;
                    mc.player.motionZ += MathHelper.cos(yaw) * 0.05f;
                }

            } else if (mode.getValue().equals("Control")) {

                if (upMode.getValue().equalsIgnoreCase("Jump")) {

                    if (setVelo.getValue()) {

                        mc.player.setVelocity(0,0,0);

                    }

                    if (mc.gameSettings.keyBindJump.isKeyDown()) {

                        event.setY(ySpeed.getValue());

                    } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {

                        event.setY(-ySpeed.getValue());

                    } else {

                        event.setY(-glideSpeed.getValue() - 0.0001);

                    }

                    if (!(MotionUtil.isMoving(mc.player))) {

                        event.setX(0);
                        event.setZ(0);

                    } else {

                        MotionUtil.setSpeed(mc.player, speed.getValue());

                    }


                } else if (upMode.getValue().equalsIgnoreCase("Aim")) {

                    if (mc.player.rotationPitch > 0 || (upTimer.getTimePassed() >= 1500)) {

                        upTimer.reset();

                        setAng = true;

                        if (mc.gameSettings.keyBindSneak.isKeyDown()) {

                            event.setY(-ySpeed.getValue());

                        } else {

                            event.setY(-glideSpeed.getValue() - 0.0001);

                        }


                        if (!(MotionUtil.isMoving(mc.player))) {

                            event.setX(0);
                            event.setZ(0);

                        } else {

                            MotionUtil.setSpeed(mc.player, speed.getValue());

                        }
                    } else {

                        setAng = false;

                        if (!MotionUtil.isMoving(mc.player)) {

                            event.setY(0);

                        }

                    }
                }
            }
        }
    });
    @EventHandler
    private final Listener<PacketEvent.Send> packetSendListener = new Listener<>(event -> {

        if (event.getPacket() instanceof CPacketPlayer && setAng) {

                ((CPacketPlayer) event.getPacket()).pitch = 0f; // spoof pitch

            if (setVelo.getValue()) {

                mc.player.setVelocity(0,0,0);

            }

        }

    });
}
