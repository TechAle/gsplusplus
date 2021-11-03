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

import java.util.Arrays;

@Module.Declaration(name = "Flight", category = Category.Movement)
public class Flight extends Module {

    public int tpid;
    float flyspeed;
    boolean bounded;

    public ModeSetting mode = registerMode("Mode", Arrays.asList("Vanilla", "Static", "Packet"), "Static");
    // Normal settings
    DoubleSetting speed = registerDouble("Speed", 2, 0, 10, () -> !mode.getValue().equalsIgnoreCase("Packet"));
    DoubleSetting ySpeed = registerDouble("Y Speed", 1, 0, 10, () -> !mode.getValue().equalsIgnoreCase("Packet"));
    DoubleSetting glideSpeed = registerDouble("Glide Speed", 0, -10, 10, () -> !mode.getValue().equalsIgnoreCase("Packet"));
    // Packet settings
    DoubleSetting packetFactor = registerDouble("Packet Factor", 1, 0, 5, () -> mode.getValue().equalsIgnoreCase("Packet"));
    ModeSetting bound = registerMode("Bounds", PhaseUtil.bound, "Up", () -> mode.getValue().equalsIgnoreCase("Packet"));
    BooleanSetting wait = registerBoolean("Freeze", false, () -> mode.getValue().equalsIgnoreCase("Packet"));
    BooleanSetting move = registerBoolean("Update", false, () -> mode.getValue().equalsIgnoreCase("Packet"));
    ModeSetting antiKick = registerMode("AntiKick", Arrays.asList("None", "Down", "Bounce"), "Bounce", () -> mode.getValue().equalsIgnoreCase("Packet"));
    IntegerSetting packets = registerInteger("Packets", 1, 1, 25, () -> mode.getValue().equalsIgnoreCase("Packet"));
    BooleanSetting confirm = registerBoolean("Confirm IDs", false, () -> mode.getValue().equalsIgnoreCase("Packet"));
    BooleanSetting debug = registerBoolean("Debug IDs", false, () -> mode.getValue().equalsIgnoreCase("Packet") && confirm.getValue());

    @SuppressWarnings("Unused")
    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {

        /* TPID HANDLING */
        if ((event.getPacket() instanceof CPacketPlayer.Position) || (event.getPacket() instanceof CPacketPlayer.PositionRotation))
            tpid++;

    });

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (!PlayerUtil.nullCheck())
            return;

        if (mode.getValue().equalsIgnoreCase("Vanilla")) {

            mc.player.capabilities.setFlySpeed(flyspeed * speed.getValue().floatValue());
            mc.player.capabilities.isFlying = true;

        } else if (mode.getValue().equalsIgnoreCase("Static")) {
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
        } else if (mode.getValue().equalsIgnoreCase("Packet")) {

            event.setY(0);

            if (wait.getValue()) {
                event.setX(0);
                event.setZ(0);
            }

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

                double[] dir = MotionUtil.forward(PlayerUtil.isPlayerClipped(false) ? 0.0624 : packetFactor.getValue() == 0 ? 0.624 : 0.0624 * packetFactor.getValue());

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

            for (int i = 0; i < packets.getValue(); i++) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, false));
                if (confirm.getValue())
                    mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid));
                PhaseUtil.doBounds(bound.getValue());
                if (move.getValue())
                    mc.player.setPosition(x,y,z);
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

            mc.player.setPosition(((SPacketPlayerPosLook) event.getPacket()).x,((SPacketPlayerPosLook) event.getPacket()).y,((SPacketPlayerPosLook) event.getPacket()).z);
            event.cancel();

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
