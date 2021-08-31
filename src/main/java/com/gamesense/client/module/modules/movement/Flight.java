package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

@Module.Declaration(name = "Flight", category = Category.Movement)
public class Flight extends Module {

    float flyspeed;

    public boolean velo;

    ModeSetting mode = registerMode("Mode", Arrays.asList("Vanilla", "Static", "Packet", "Bypass"), "Static");
    BooleanSetting antiKick = registerBoolean("Anti Kick", true, () -> mode.getValue().equalsIgnoreCase("Packet"));
    BooleanSetting jump = registerBoolean("Jump", true, () -> mode.getValue().equalsIgnoreCase("Bypass"));
    DoubleSetting jumpHeight = registerDouble("Jump Height", 1,0,10, () -> mode.getValue().equalsIgnoreCase("Bypass"));
    BooleanSetting allowY = registerBoolean("Allow Positive Y Velocity", true, () -> mode.getValue().equalsIgnoreCase("Bypass"));
    DoubleSetting speed = registerDouble("Speed", 2,0,10);
    DoubleSetting ySpeed = registerDouble("Y Speed", 1,0,10);
    DoubleSetting glideSpeed = registerDouble("Glide Speed", 0,-10,10);

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (mode.getValue().equalsIgnoreCase("Static")){
            if (mc.gameSettings.keyBindJump.isKeyDown()) {

                event.setY(ySpeed.getValue());

            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {

                event.setY(-ySpeed.getValue());

            } else {

                event.setY(-glideSpeed.getValue());

            }

            if (MotionUtil.isMoving(mc.player)) {
                MotionUtil.setSpeed(mc.player, speed.getValue());
            } else {

                event.setX(0);
                event.setZ(0);

            }
        } else if (mode.getValue().equalsIgnoreCase("Vanilla")) {

            mc.player.capabilities.setFlySpeed(flyspeed * speed.getValue().floatValue());
            mc.player.capabilities.isFlying = true;

        } else if (mode.getValue().equalsIgnoreCase("Packet")) {

            mc.player.setVelocity(0, 0, 0);

            if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + mc.player.motionX, mc.player.posY - 0.0624, mc.player.posZ + mc.player.motionZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX,mc.player.posY + 69420, mc.player.posZ,mc.player.rotationYaw,mc.player.rotationPitch, false));
            }
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + mc.player.motionX, mc.player.posY + 0.0624, mc.player.posZ + mc.player.motionZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX,mc.player.posY + 69420, mc.player.posZ,mc.player.rotationYaw,mc.player.rotationPitch, false));

            }
            if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {
                double[] dir = MotionUtil.forward(0.0624);
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + (dir[0]), mc.player.posY, mc.player.posZ + (dir[1]), false));
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX,mc.player.posY + 69420, mc.player.posZ,mc.player.rotationYaw,mc.player.rotationPitch, false));

            }
            if (antiKick.getValue() && mc.player.ticksExisted % 4 == 0) {
                event.setY(-0.01);
            } else {

                event.setY(0);

            }
        }

    });

    @Override
    protected void onEnable() {
        flyspeed = mc.player.capabilities.getFlySpeed();
    }

    @Override
    protected void onDisable() {
        mc.player.capabilities.setFlySpeed(flyspeed);
        mc.player.capabilities.isFlying = false;
        velo = true;
    }

    @EventHandler
    private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {

        if (mode.getValue().equals("Bypass")){

            if (jump.getValue() && mc.player.onGround) {

                mc.player.jumpMovementFactor = jumpHeight.getValue().floatValue();
                mc.player.jump();

            }

            velo = false;

            double[] dir = MotionUtil.forward(((((SPacketEntityVelocity) event.getPacket()).motionX) + ((SPacketEntityVelocity) event.getPacket()).motionZ) / 2d);

            if (event.getPacket() instanceof SPacketEntityVelocity) {
                if (((SPacketEntityVelocity) event.getPacket()).getEntityID() == mc.player.getEntityId()) {


                    ((SPacketEntityVelocity) event.getPacket()).motionX = ((int) dir[0]);
                    ((SPacketEntityVelocity) event.getPacket()).motionZ = ((int) dir[1]);

                    if (allowY.getValue() && ((SPacketEntityVelocity) event.getPacket()).motionY > 0) {

                        ((SPacketEntityVelocity) event.getPacket()).motionY = ((SPacketEntityVelocity) event.getPacket()).motionY;

                    } else {

                        ((SPacketEntityVelocity) event.getPacket()).motionY = 0;

                    }

                }

            }
            if (event.getPacket() instanceof SPacketExplosion) {
                ((SPacketExplosion) event.getPacket()).motionX = ((int) dir[0]);
                ((SPacketExplosion) event.getPacket()).motionZ = ((int) dir[1]);
            }

            if (allowY.getValue() && ((SPacketEntityVelocity) event.getPacket()).motionY > 0) {

                ((SPacketExplosion) event.getPacket()).motionY = ((SPacketExplosion) event.getPacket()).motionY;

            } else {

                ((SPacketExplosion) event.getPacket()).motionY = 0;


            }

        } else {

            velo = true;

        }
    });

}
