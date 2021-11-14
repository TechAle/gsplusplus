package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.PhaseUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;

import java.util.*;

@Module.Declaration(name = "Flight", category = Category.Movement)
public class Flight extends Module {

    int tpid;
    float flyspeed;
    ArrayList<CPacketPlayer> packetlist = new ArrayList<>();

    // Normal settings
    public ModeSetting mode = registerMode("Mode", Arrays.asList("Vanilla", "Static", "Packet"), "Static");
    DoubleSetting speed = registerDouble("Speed", 2, 0, 10, () -> !mode.getValue().equalsIgnoreCase("Packet"));
    DoubleSetting ySpeed = registerDouble("Y Speed", 1, 0, 10, () -> !mode.getValue().equalsIgnoreCase("Packet"));
    DoubleSetting glideSpeed = registerDouble("Glide Speed", 0, -10, 10, () -> !mode.getValue().equalsIgnoreCase("Packet"));
    BooleanSetting antiKickFlight = registerBoolean("AntiKick", false, () -> !mode.getValue().equalsIgnoreCase("Packet"));

    // Packet settings
    DoubleSetting packetSpeed = registerDouble("Packet Speed", 1, 0, 10, () -> mode.getValue().equalsIgnoreCase("Packet"));
    DoubleSetting packetY = registerDouble("Packet Y Speed", 1, 0, 5, () -> mode.getValue().equalsIgnoreCase("Packet"));
    ModeSetting bound = registerMode("Bounds", PhaseUtil.bound, PhaseUtil.normal, () -> mode.getValue().equalsIgnoreCase("Packet"));
    BooleanSetting wait = registerBoolean("Freeze", false, () -> mode.getValue().equalsIgnoreCase("Packet"));
    DoubleSetting reduction = registerDouble("Reduction", 0.5, 0, 1, () -> mode.getValue().equalsIgnoreCase("Packet"));
    ModeSetting antiKick = registerMode("AntiKick", Arrays.asList("None", "Down", "Bounce"), "Bounce", () -> mode.getValue().equalsIgnoreCase("Packet"));
    IntegerSetting antiKickFreq = registerInteger("AntiKick Frequency", 4, 2, 8, () -> mode.getValue().equalsIgnoreCase("Packet"));
    IntegerSetting packets = registerInteger("Packets", 1, 1, 25, () -> mode.getValue().equalsIgnoreCase("Packet"));
    BooleanSetting confirm = registerBoolean("Confirm IDs", false, () -> mode.getValue().equalsIgnoreCase("Packet"));
    BooleanSetting debug = registerBoolean("Debug IDs", false, () -> mode.getValue().equalsIgnoreCase("Packet") && confirm.getValue());

    @SuppressWarnings("Unused")
    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {

        /* TPID HANDLING */
        if ((event.getPacket() instanceof CPacketPlayer.Position) || (event.getPacket() instanceof CPacketPlayer.PositionRotation))
            tpid++;

        if (event.getPacket() instanceof CPacketPlayer && mode.getValue().equalsIgnoreCase("Packet")) {

            CPacketPlayer packet = (CPacketPlayer) event.getPacket();

            if (packetlist.contains(packet)) {

                packetlist.remove(packet);
                ((CPacketPlayer) event.getPacket()).pitch = 0;
                ((CPacketPlayer) event.getPacket()).yaw = 0;

            }

            else event.cancel();

        }


    });

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (!PlayerUtil.nullCheck())
            return;

        if (mode.getValue().equalsIgnoreCase("Vanilla")) {

            mc.player.capabilities.setFlySpeed(flyspeed * speed.getValue().floatValue());
            mc.player.capabilities.isFlying = true;

            if (antiKickFlight.getValue())
                mc.player.motionY = -0.00001;

        } else if (mode.getValue().equalsIgnoreCase("Static")) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {

                event.setY(ySpeed.getValue());

            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {

                event.setY(-ySpeed.getValue());

            } else {

                event.setY(-glideSpeed.getValue());

            }

            if (MotionUtil.isMoving(mc.player)) {

                double[] dir = MotionUtil.forward(speed.getValue());

                event.setX(dir[0]);
                event.setZ(dir[1]);

            } else {
                event.setX(0);
                event.setZ(0);
            }

            if (antiKickFlight.getValue())
                event.setY(event.getY() - 0.00001);

        } else if (mode.getValue().equalsIgnoreCase("Packet")) {

            event.setY(0);

            if (wait.getValue()) {
                event.setX(0);
                event.setZ(0);
            }

            double x = 0;
            double y = 0;
            double z = 0;

            if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {

                y -= PlayerUtil.isPlayerClipped() ? 0.0624 : 0.0624 * packetY.getValue();

            }
            if (mc.gameSettings.keyBindJump.isKeyDown() && !MotionUtil.isMoving(mc.player)) {

                y += PlayerUtil.isPlayerClipped() ? 0.0624 : 0.0624 * packetY.getValue();

            }
            if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {

                double[] dir = MotionUtil.forward(PlayerUtil.isPlayerClipped() ? 0.0624 : packetSpeed.getValue() == 0 ? 0.624 : 0.0624 * packetSpeed.getValue());

                if (PlayerUtil.isPlayerClipped()) {

                    mc.player.motionX = MotionUtil.forward(.0624)[0];
                    mc.player.motionZ = MotionUtil.forward(.0624)[1];

                }

                x += dir[0];
                z += dir[1];

            }


            if (mc.world.isAirBlock(new BlockPos(mc.player.getPositionVector()).add(0, -0.1, 0))) { // prevent the antikick not working when it should
                if (!antiKick.getValue().equalsIgnoreCase("None") && mc.player.ticksExisted % antiKickFreq.getValue() == 0
                        && !mc.player.onGround) {

                    y -= 0.01;

                } else if (antiKick.getValue().equalsIgnoreCase("Bounce") && mc.player.ticksExisted % antiKickFreq.getValue() == 1
                        && !mc.player.onGround && !MotionUtil.isMoving(mc.player)) {

                    y += 0.01;

                }
            }

            for (int i = 0; i < packets.getValue(); i++) {

                CPacketPlayer packet = new CPacketPlayer.PositionRotation(x + mc.player.posX, y + mc.player.posY, z + mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false);
                packetlist.add(packet);
                mc.player.connection.sendPacket(packet);

                // confirm all
                if (confirm.getValue()) {
                    mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid - 1));
                    mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid));
                    mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid + 1));
                }

                mc.player.setVelocity(x * reduction.getValue(), y * reduction.getValue(), z * reduction.getValue());

                CPacketPlayer bounds = PhaseUtil.doBounds(bound.getValue(), false);
                packetlist.add(bounds);
                mc.player.connection.sendPacket(bounds);

            }

        }

    });
    @SuppressWarnings("Unused")
    @EventHandler
    private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {

        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            if (confirm.getValue() && debug.getValue())
                MessageBus.sendClientPrefixMessageWithID(tpid - ((SPacketPlayerPosLook) event.getPacket()).teleportId + "", 69420);
            tpid = ((SPacketPlayerPosLook) event.getPacket()).teleportId;

            ((SPacketPlayerPosLook) event.getPacket()).yaw = mc.player.rotationYaw;
            ((SPacketPlayerPosLook) event.getPacket()).pitch = mc.player.rotationPitch;

        }

    });

    /* END OF PACKET */

    @Override
    protected void onEnable() {

        // This does not fix but, avoid a spam in the console -TechAle
        if (mc.world == null || mc.player == null)
            return;

        flyspeed = mc.player.capabilities.getFlySpeed();

    }

    @Override
    protected void onDisable() {
        mc.player.capabilities.setFlySpeed(flyspeed);
        mc.player.capabilities.isFlying = false;
        mc.player.motionX = mc.player.motionY = mc.player.motionZ = 0;
        mc.player.noClip = false;
    }

}
