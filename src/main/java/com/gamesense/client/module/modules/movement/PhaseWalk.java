package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.PhaseUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

import java.util.Arrays;

@Module.Declaration(name = "PhaseWalk", category = Category.Movement)
public class PhaseWalk extends Module {

    ModeSetting bound = registerMode("Bounds", PhaseUtil.bound, "Min");
    BooleanSetting clipCheck = registerBoolean("Clipped Check", false);
    BooleanSetting update = registerBoolean("Update Pos", false);
    BooleanSetting sprint = registerBoolean("Sprint Force Enable", true);

    int tpid = 0;

    @Override
    public void onUpdate() {
        if (mc.player.collidedHorizontally && !ModuleManager.getModule(Flight.class).isEnabled())
            packetFly();
    }

    void packetFly() {

        double[] clip = MotionUtil.forward(0.0624);

        if ((PlayerUtil.isPlayerClipped(false) || !clipCheck.getValue()) || (mc.gameSettings.keyBindSprint.isKeyDown() && sprint.getValue()) || mc.gameSettings.keyBindSneak.isKeyDown()) {

            if (mc.gameSettings.keyBindSneak.isKeyDown() && mc.player.onGround)
                tp(mc.player.posX + clip[0], mc.player.posY - 0.0624, mc.player.posZ + clip[1], false);
            else
                tp(mc.player.posX + clip[0], mc.player.posY, mc.player.posZ + clip[1], true);

        }

    }

    void tp(double x, double y, double z, boolean onGround) {

        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, onGround));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid));
        PhaseUtil.doBounds(bound.getValue(), true);
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpid));

        if (update.getValue())
            mc.player.setPosition(x, y, z);

    }

    private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event ->  {

        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            tpid = ((SPacketPlayerPosLook) event.getPacket()).teleportId;
        }

    });

    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event ->  {

        if (event.getPacket() instanceof CPacketPlayer.Position || event.getPacket() instanceof CPacketPlayer.PositionRotation)
            tpid++;

    });


}
