package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.BoundingBoxEvent;
import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.PhaseUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

import java.util.Arrays;

@Module.Declaration(name = "Phase", category = Category.Movement)
public class Phase extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("NCP", "Vanilla", "Skip"), "NCP");

    DoubleSetting safety = registerDouble("Safety",0.15,0,1, () -> mode.getValue().equalsIgnoreCase("Skip"));
    BooleanSetting bounded = registerBoolean("Bounded",false,()->mode.getValue().equalsIgnoreCase("Skip"));

    BooleanSetting h = registerBoolean("Keep Floor", false, () -> mode.getValue().equalsIgnoreCase("Vanilla"));

    ModeSetting bound = registerMode("Bounds", PhaseUtil.bound, "Min", () -> mode.getValue().equalsIgnoreCase("NCP") || mode.getValue().equalsIgnoreCase("Skip") && bounded.getValue());
    BooleanSetting twoBeePvP = registerBoolean("2b2tpvp", false, () -> mode.getValue().equalsIgnoreCase("NCP"));
    BooleanSetting update = registerBoolean("Update Pos", false, () -> mode.getValue().equalsIgnoreCase("NCP"));

    BooleanSetting clipCheck = registerBoolean("Clipped Check", false, () -> !mode.getValue().equalsIgnoreCase("Skip"));
    BooleanSetting sprint = registerBoolean("Sprint Force Enable", true, () -> !mode.getValue().equalsIgnoreCase("Skip"));

    int tpid = 0;
    boolean clipped = false;

    @EventHandler
    private final Listener<BoundingBoxEvent> boundingBoxEventListener = new Listener<>(event -> {

        try {
                if (mode.getValue().equalsIgnoreCase("Vanilla")
                        && ((clipped || !clipCheck.getValue())
                        || (mc.gameSettings.keyBindSprint.isKeyDown() && sprint.getValue())))

                    if (event.getPos().y >= mc.player.getPositionVector().y || !h.getValue() || mc.gameSettings.keyBindSneak.isKeyDown())
                        event.setbb(Block.NULL_AABB);

        } catch (Exception e) {
            MessageBus.sendClientPrefixMessage(e.getMessage());
            disable();
        }

    });

    @SuppressWarnings("Unused")
    @EventHandler
    private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {

        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            tpid = ((SPacketPlayerPosLook) event.getPacket()).teleportId;
        }

    });
    @SuppressWarnings("Unused")
    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {

        if (event.getPacket() instanceof CPacketPlayer.PositionRotation || event.getPacket() instanceof CPacketPlayer.Position) {
            tpid++;
        }

    });

    @Override
    public void onUpdate() {

        clipped = PlayerUtil.isPlayerClipped();

        if ((((mc.player.collidedHorizontally || mc.gameSettings.keyBindSneak.isKeyDown()) && mode.getValue().equalsIgnoreCase("NCP"))
                && !ModuleManager.getModule(Flight.class).isEnabled()
                && mode.getValue().equalsIgnoreCase("NCP")
                && (clipped || !clipCheck.getValue())
                ||  (mc.gameSettings.keyBindSprint.isKeyDown() && sprint.getValue()) && mc.player.collidedHorizontally) && mode.getValue().equalsIgnoreCase("NCP"))
            packetFly();

        if (mode.getValue().equalsIgnoreCase("Skip"))
            skip();
    }

    void skip() {

        double[] dir = MotionUtil.forward(1,(Math.round(mc.player.rotationYaw / 8) * 8)); // 1 means we can multiply this

        if (!mc.player.collidedHorizontally || !(mc.player.motionX + mc.player.motionZ == 0))
            return;

        for (float i = 0.1f; i < 1; i += 0.1) {
            double dirX = dir[0] * i;
            double dirZ = dir[1] * i;
            if (!mc.world.collidesWithAnyBlock((mc.player.getEntityBoundingBox()).offset(dirX, 0, dirZ))) {
                double[] safetyDir = MotionUtil.forward(i+safety.getValue(),(Math.round((mc.player.rotationYaw + (mc.player.moveStrafing * 45) + (mc.player.moveForward < 0 ? 180 : 0)) / 8) * 8));
                mc.player.setPosition(mc.player.posX + safetyDir[0], mc.player.posY, mc.player.posZ + safetyDir[1]);
                if (bounded.getValue())
                    PhaseUtil.doBounds(bound.getValue(),true);

                mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid - 1));
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid));
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid + 1));

            } else {
                MessageBus.sendClientPrefixMessageWithID("Pos: " + dirX + " " + dirZ,70);
            }
        }

    }

    void packetFly() {

        double[] clip = MotionUtil.forward(0.0624);

        if (mc.gameSettings.keyBindSneak.isKeyDown() && mc.player.onGround)
            tp(0d,-0.0624,0d,false);
        else
            tp(clip[0],0,clip[1], true);


    }

    void tp(double x, double y, double z, boolean onGround) {

        double[] dir = MotionUtil.forward(-0.0312);

        if (twoBeePvP.getValue())
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir[0], mc.player.posY, mc.player.posZ + dir[1], onGround)); // move back a bit

        mc.player.connection.sendPacket(new CPacketPlayer.Position((twoBeePvP.getValue() ? (x/2) : x) + mc.player.posX, y + mc.player.posY, (twoBeePvP.getValue() ? (z/2) : z) + mc.player.posZ, onGround));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid - 1));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid + 1));
        PhaseUtil.doBounds(bound.getValue(), true);

        if (update.getValue())
            mc.player.setPosition(x, y, z);

    }

}
