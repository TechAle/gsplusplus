package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.Arrays;

@Module.Declaration(name = "ElytraFly", category = Category.Movement)
public class ElytraFly extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("Control", "Boost"), "Boost");
    BooleanSetting packet = registerBoolean("Packet", false);
    ModeSetting toMode = registerMode("Takeoff", Arrays.asList("PacketFly", "Timer", "None"), "PacketFly");
    ModeSetting upMode = registerMode("Up Mode", Arrays.asList("Jump", "Aim"), "Jump", () -> !mode.getValue().equals("Boost"));
    DoubleSetting speed = registerDouble("Speed", 2.5, 0, 10, () -> mode.getValue().equalsIgnoreCase("Control"));
    DoubleSetting ySpeed = registerDouble("Y Speed", 0, 1, 10, () -> mode.getValue().equalsIgnoreCase("Control"));
    DoubleSetting glideSpeed = registerDouble("Glide Speed", 0, 0, 3, () -> mode.getValue().equalsIgnoreCase("Control"));
    BooleanSetting yawLock = registerBoolean("Yaw Lock", false, () -> mode.getValue().equalsIgnoreCase("Control"));

    boolean setAng;

    Timer upTimer = new Timer();

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (mc.player.isElytraFlying()) {

            mc.timer.tickLength = 50f;

            if (mode.getValue().equals("Boost")) {
                if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindForward.isKeyDown()) {
                    float yaw = (float) Math.toRadians(mc.player.rotationYaw);
                    mc.player.motionX -= Math.sin(yaw) * 0.05f;
                    mc.player.motionZ += Math.cos(yaw) * 0.05f;
                }

            } else if (mode.getValue().equals("Control")) {

                if (packet.getValue())
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));

                if (upMode.getValue().equalsIgnoreCase("Jump")) {

                    if (mc.gameSettings.keyBindJump.isKeyDown()) {

                        event.setY(ySpeed.getValue());

                    } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {

                        event.setY(-ySpeed.getValue());

                    } else {

                        event.setY(-0.000001 -glideSpeed.getValue());

                    }

                    if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {

                        double[] dir;

                        if (!yawLock.getValue()){

                            dir = MotionUtil.forward(speed.getValue());

                        } else {

                            final int angle = 360 / 8;
                            float yaw = mc.player.rotationYaw;
                            yaw = (float)(Math.round(yaw / angle) * angle);

                            dir = MotionUtil.forward(speed.getValue(), yaw);

                        }

                        event.setX(dir[0]);
                        event.setZ(dir[1]);

                    } else {

                        event.setX(0);
                        event.setZ(0);

                    }

                } else if (upMode.getValue().equalsIgnoreCase("Aim")) {

                    if (mc.player.rotationPitch > 0 || (upTimer.getTimePassed() >= 1500)) {

                        upTimer.reset();

                        if (mc.gameSettings.keyBindSneak.isKeyDown()) {

                            event.setY(-ySpeed.getValue());

                        } else {

                            event.setY(-glideSpeed.getValue());

                        }

                        if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {

                            double[] dir;

                            if (!yawLock.getValue()){

                                dir = MotionUtil.forward(speed.getValue());

                            } else {

                                final int angle = 360 / 8;
                                float yaw = mc.player.rotationYaw;
                                yaw = (float)(Math.round(yaw / angle) * angle);

                                dir = MotionUtil.forward(speed.getValue(), yaw);

                            }

                            event.setX(dir[0]);
                            event.setZ(dir[1]);

                        } else {

                            event.setX(0);
                            event.setZ(0);

                        }
                    } else {

                        setAng = false;

                    }
                }
            }
        } else {

            if (mc.gameSettings.keyBindJump.isKeyDown() && mc.player.inventory.armorInventory.get(2).getItem().equals(Items.ELYTRA)){
                switch (toMode.getValue()) {

                    case "PacketFly": {

                        if (mc.player.onGround) {

                            mc.player.jump();

                        } else if (mc.player.motionY < 0) {

                            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + mc.player.motionX, mc.player.posY - 0.0025, mc.player.posZ + mc.player.motionZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));

                        }

                    }
                    case "Timer": {

                        if (mc.player.onGround) {
                            mc.player.jump();
                        } else if (mc.player.motionY < 0) {
                            mc.timer.tickLength = 300f;
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                        }

                    }
                    default: {

                        if (!mc.player.onGround && mc.player.motionY < 0) {

                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));

                        }

                    }

                }
            }


        }

    });
    @EventHandler
    private final Listener<PacketEvent.Send> packetSendListener = new Listener<>(event -> {

        if (event.getPacket() instanceof CPacketPlayer && setAng && mode.getValue().equalsIgnoreCase("Control")) {

            ((CPacketPlayer) event.getPacket()).pitch = 0f; // spoof pitch

        }

    });

    @Override
    protected void onDisable() {
        mc.timer.tickLength = 50;
    }
}
