package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.Arrays;

@Module.Declaration(name = "BoundsMove", category = Category.Movement)
public class BoundsMove extends Module {


    ModeSetting bound = registerMode("Bounds", Arrays.asList("Up", "Alternate", "Down", "Zero", "Min", "Forward"), "Up");

    @Override
    public void onUpdate() {
        if ((mc.player.moveForward != 0 || mc.player.moveStrafing != 0)
                && !(ModuleManager.getModule(Flight.class).isEnabled()
                && ModuleManager.getModule(Flight.class).mode.getValue().equalsIgnoreCase("Packet"))) {
            doBounds();
        }
    }

    private void doBounds() {
        switch (bound.getValue()) {

            case "Up":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                break;
            case "Down":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
            case "Zero":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, 0, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                break;
            case "Min":
                if (mc.player.ticksExisted % 2 == 0)
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 101, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                else
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 101, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                break;
            case "Alternate":
                if (mc.player.ticksExisted % 2 == 0)
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                else
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                break;

            case "Forward":
                double[] dir = MotionUtil.forward(66.8);
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + dir[0], mc.player.posY + 33.4, mc.player.posZ + dir[1], mc.player.rotationYaw, mc.player.rotationPitch, false));
                break;
        }
        ModuleManager.getModule(Flight.class).tpid++;
    }
}


