package com.gamesense.api.util.player;

import com.gamesense.api.util.world.MotionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PhaseUtil {

    public static List<String> bound = Arrays.asList("Up", "Alternate", "Down", "Zero", "Min", "Forward", "Flat", "LimitJitter", "None");
    public static String normal = "Forward";

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static CPacketPlayer doBounds(String mode, boolean send) {

        double[] dir;
        CPacketPlayer packet = new CPacketPlayer.PositionRotation(0,0,0,0,0,false);

        switch (mode) {

            case "Up":
                packet = new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false);
                break;
            case "Down":
                packet = new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false);
                break;
            case "Zero":
                packet = new CPacketPlayer.PositionRotation(mc.player.posX, 0, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false);
                break;
            case "Min":
                packet = new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 100, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false);
                break;
            case "Alternate":
                if (mc.player.ticksExisted % 2 == 0)
                    packet = new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false);
                else
                    packet = new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false);
                break;
            case "Forward":
                dir = MotionUtil.forward(67);
                packet = new CPacketPlayer.PositionRotation(mc.player.posX + dir[0], mc.player.posY + 33.4, mc.player.posZ + dir[1], mc.player.rotationYaw, mc.player.rotationPitch, false);
                break;
            case "Flat":
                dir = MotionUtil.forward(100);
                packet = new CPacketPlayer.PositionRotation(mc.player.posX + dir[0], mc.player.posY, mc.player.posZ + dir[1], mc.player.rotationYaw, mc.player.rotationPitch, false);
                break;
            case "LimitJitter":
                packet = new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + limit(), mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false);
                break;
        }
        mc.player.connection.sendPacket(packet);
        return packet;
    }

    public static double limit() {

        Random random = new Random();

        int randomValue = random.nextInt(22);
        randomValue += 70;
        if (random.nextBoolean()) {
            return randomValue;
        }
        return -randomValue;
    }

}
