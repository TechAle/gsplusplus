package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

import java.util.Arrays;

@Module.Declaration(name = "Flight", category = Category.Movement)
public class Flight extends Module {

    float flyspeed;
    boolean bounded;
    public int tpid = 0;

    ModeSetting mode = registerMode("Mode", Arrays.asList("Vanilla", "Static", "Packet", "Damage", "AirJump"), "Static");

    ModeSetting damage = registerMode("Damage Mode", Arrays.asList("LB", "WI", "PF"), "WI", () -> mode.getValue().equalsIgnoreCase("Damage"));

    DoubleSetting packetFactor = registerDouble("Packet Factor", 1, 0, 5, () -> mode.getValue().equalsIgnoreCase("Packet"));
    ModeSetting bound = registerMode("Bounds", Arrays.asList("Up", "Alternate", "Down", "Zero", "Min"), "Up", () -> mode.getValue().equalsIgnoreCase("Packet"));
    ModeSetting antiKick = registerMode("AntiKick", Arrays.asList("None", "Down", "Bounce"), "Bounce", () -> mode.getValue().equalsIgnoreCase("Packet"));
    BooleanSetting confirm = registerBoolean("Confirm", false, () -> mode.getValue().equalsIgnoreCase("Packet"));
    BooleanSetting debug = registerBoolean("Debug ID", false, () -> mode.getValue().equalsIgnoreCase("Packet") && confirm.getValue());

    DoubleSetting minStr = registerDouble("Min Blast Strength", 0,0,1);
    DoubleSetting jspeed = registerDouble("Speed", 0, 0, 5, () -> mode.getValue().equalsIgnoreCase("AirJump"));
    DoubleSetting height = registerDouble("Jump Height", 0.42, 0, 1, () -> mode.getValue().equalsIgnoreCase("AirJump"));

    DoubleSetting speed = registerDouble("Speed", 2, 0, 10, () -> !mode.getValue().equalsIgnoreCase("Packet") && !mode.getValue().equalsIgnoreCase("AirJump"));
    DoubleSetting ySpeed = registerDouble("Y Speed", 1, 0, 10, () -> !mode.getValue().equalsIgnoreCase("Packet") && !mode.getValue().equalsIgnoreCase("AirJump"));

    DoubleSetting glideSpeed = registerDouble("Glide Speed", 0, -10, 10, () -> !mode.getValue().equalsIgnoreCase("Packet") && !mode.getValue().equalsIgnoreCase("AirJump"));

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (!PlayerUtil.nullCheck())
            return;

        if (mode.getValue().equalsIgnoreCase("Static")) {
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

            /* PACKET */

            event.setX(0);
            event.setY(0);
            event.setZ(0);

            double x = mc.player.posX;
            double y = mc.player.posY;
            double z = mc.player.posZ;

            if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {

                y -= 0.0624;

                bounded = true;

            }
            if (mc.gameSettings.keyBindJump.isKeyDown()) {

                y += 0.0624;

                bounded = true;

            }
            if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {

                double[] dir = MotionUtil.forward(mc.player.collided ? 0.0624 : 0.0624 * packetFactor.getValue());

                x += dir[0];
                z += dir[1];

                bounded = true;

            }


            if (!antiKick.getValue().equalsIgnoreCase("None") && mc.player.ticksExisted % 4 == 0) {

                y -= 0.01;
                bounded = true;

            } else if (antiKick.getValue().equalsIgnoreCase("Bounce") && mc.player.ticksExisted % 4 == 2) {

                y += 0.01;
                bounded = true;

            } else if (antiKick.getValue().equalsIgnoreCase("None")) {

                event.setY(0);
                bounded = true;

            }

            mc.player.connection.sendPacket(new CPacketPlayer.Position(x,y,z,false));
            tpid++;
            if (confirm.getValue())
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid));
            doBounds();

            /* END OF PACKET */

        } else if (mode.getValue().equalsIgnoreCase("Damage")) {

            if (MotionUtil.isMoving(mc.player)) {
                MotionUtil.setSpeed(mc.player, speed.getValue());
            } else {

                event.setX(0);
                event.setZ(0);

            }

            event.setY(-0.001);

        }

    });
    @EventHandler
    private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {

        // Wtf, how the fuck this break the connection between you and the server
        if (mc.world == null || mc.player == null)
            return;

        double[] dir = MotionUtil.forward(jspeed.getValue());

        if (event.getPacket() instanceof SPacketExplosion) {

            float motion = (Math.abs(((SPacketExplosion) event.getPacket()).motionX) + Math.abs(((SPacketExplosion) event.getPacket()).motionY) + Math.abs(((SPacketExplosion) event.getPacket()).motionZ));

            if (motion >= minStr.getValue()){
                mc.player.motionY = height.getValue();

                mc.player.motionX = dir[0];
                mc.player.motionZ = dir[1];
            }

        }

        if (event.getPacket() instanceof SPacketEntityVelocity) {

            float motion = (
                    Math.abs(((SPacketEntityVelocity) event.getPacket()).motionX)
                    + Math.abs(((SPacketEntityVelocity) event.getPacket()).motionY)
                    + Math.abs(((SPacketEntityVelocity) event.getPacket()).motionZ)
            );

            if (motion >= minStr.getValue()){
                mc.player.motionY = height.getValue();

                mc.player.motionX = dir[0];
                mc.player.motionZ = dir[1];
            }

        }

        if (event.getPacket() instanceof SPacketPlayerPosLook) {

            if (debug.getValue() && mode.getValue().equalsIgnoreCase("Packet"))
                if (!(((SPacketPlayerPosLook) event.getPacket()).getTeleportId() == tpid + 1)) // if our TPID is not the same as the server TPID
                    MessageBus.sendClientPrefixMessage(String.valueOf(((SPacketPlayerPosLook) event.getPacket()).teleportId - (tpid + 1))); // Print the difference

            tpid = ((SPacketPlayerPosLook) event.getPacket()).teleportId;

        }

    });

    private void doBounds() {
        if (bounded) {
            switch (bound.getValue()) {

                case "Up":
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                case "Down":
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                case "Zero":
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, 0, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));

                case "Min":
                    if (mc.player.ticksExisted % 2 == 0)
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 101, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                    else
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 101, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));

                default:
                    if (mc.player.ticksExisted % 2 == 0)
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                    else
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
            }
            tpid++;

            if (confirm.getValue())
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid));
        }
    }

    @Override
    protected void onEnable() {

        // This does not fix but, avoid a spam in the console -TechAle
        if (mc.world == null || mc.player == null)
            return;

        flyspeed = mc.player.capabilities.getFlySpeed();

        if (mode.getValue().equalsIgnoreCase("Damage")) {

            damage();
            mc.player.jump();

        }
        tpid = 0;
    }

    @Override
    protected void onDisable() {
        mc.player.capabilities.setFlySpeed(flyspeed);
        mc.player.capabilities.isFlying = false;
        mc.player.motionX = mc.player.motionY = mc.player.motionZ = 0;
    }

    public void damage() {

        if (damage.getValue().equalsIgnoreCase("WI")) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 3.1, mc.player.posZ, false)); // send the player up
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.05, mc.player.posZ, false));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true)); // set onground to true and deal damage

            mc.player.motionY = -5; // go down fast (idk if will help at all)}

        } else if (damage.getValue().equalsIgnoreCase("LB")) {

            for (int i = 0; i < 64; i++) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.049, mc.player.posZ, false));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
            }

            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1, mc.player.posZ, true));

        } else if (damage.getValue().equalsIgnoreCase("PF")) { // try to exploit packetfly bounds to tp up 3.1

            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 3.1, mc.player.posZ, false));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 69420, mc.player.posZ, false));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true));

        }

    }

}
