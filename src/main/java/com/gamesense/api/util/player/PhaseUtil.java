package com.gamesense.api.util.player;

import com.gamesense.api.util.world.MotionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.Arrays;
import java.util.List;

public class PhaseUtil {

    public static List<String> bound = Arrays.asList("Up", "Alternate", "Down", "Zero", "Min", "Forward", "Flat");

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void doBounds(String mode) {

        double[] dir;

        switch (mode) {

            case "Up":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                break;
            case "Down":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                break;
            case "Zero":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, 0, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));

            case "Min":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 100, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                break;
            case "Alternate":
                if (mc.player.ticksExisted % 2 == 0)
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                else
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                break;
            case "Forward":
                dir = MotionUtil.forward(67);
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + dir[0], mc.player.posY + 33.4, mc.player.posZ + dir[1], mc.player.rotationYaw, mc.player.rotationPitch, false));
                break;
            case "Flat":
                dir = MotionUtil.forward(100);
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + dir[0], mc.player.posY, mc.player.posZ + dir[1], mc.player.rotationYaw, mc.player.rotationPitch, false));
                break;
        }
    }
}
