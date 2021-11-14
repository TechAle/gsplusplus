package com.gamesense.api.util.player;

import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    public static boolean nullCheck() {

        return !(mc.world == null || mc.player == null);

    }

    // Find closest target
    // 0b00101010: replaced getDistance with getDistanceSq as speeds up calculation
    public static EntityPlayer findClosestTarget(double rangeMax, EntityPlayer aimTarget) {

        return findClosestTarget(rangeMax,aimTarget,false);
    }

    public static EntityPlayer findClosestTarget(double rangeMax, EntityPlayer aimTarget, boolean moving) {
        rangeMax *= rangeMax;
        List<EntityPlayer> playerList = mc.world.playerEntities;

        EntityPlayer closestTarget = null;

        for (EntityPlayer entityPlayer : playerList) {

            if (EntityUtil.basicChecksEntity(entityPlayer))
                continue;

            if (entityPlayer.motionX + mc.player.motionZ == 0 && moving)
                continue;

            if (aimTarget == null && mc.player.getDistanceSq(entityPlayer) <= rangeMax) {
                closestTarget = entityPlayer;
                continue;
            }
            if (aimTarget != null && mc.player.getDistanceSq(entityPlayer) <= rangeMax && mc.player.getDistanceSq(entityPlayer) < mc.player.getDistanceSq(aimTarget)) {
                closestTarget = entityPlayer;
            }
        }
        return closestTarget;
    }

    // 0b00101010: replaced getDistance with getDistanceSq as speeds up calculation
    public static EntityPlayer findClosestTarget() {
        List<EntityPlayer> playerList = mc.world.playerEntities;

        EntityPlayer closestTarget = null;

        for (EntityPlayer entityPlayer : playerList) {
            if (EntityUtil.basicChecksEntity(entityPlayer))
                continue;

            if (closestTarget == null) {
                closestTarget = entityPlayer;
                continue;
            }
            if (mc.player.getDistanceSq(entityPlayer) < mc.player.getDistanceSq(closestTarget)) {
                closestTarget = entityPlayer;
            }
        }

        return closestTarget;
    }

    public static boolean isPlayerClipped() {

        return isPlayerClipped(false);

    }

    public static boolean isPlayerClipped(Entity e) {

        return !(mc.world.getCollisionBoxes(e, e.getEntityBoundingBox().contract(0, 0, 0)).isEmpty());

    }

    public static boolean isPlayerClipped(boolean ignoreTop) {

        return !(mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().contract(0, ignoreTop ? 1 : 0, 0)).isEmpty());

    }

    // Find player you are looking
    public static EntityPlayer findLookingPlayer(double rangeMax) {
        // Get player
        ArrayList<EntityPlayer> listPlayer = new ArrayList<>();
        // Only who is in a distance of enemyRange
        for (EntityPlayer playerSin : mc.world.playerEntities) {
            if (EntityUtil.basicChecksEntity(playerSin))
                continue;
            if (mc.player.getDistance(playerSin) <= rangeMax) {
                listPlayer.add(playerSin);
            }
        }

        EntityPlayer target = null;
        // Get coordinate eyes + rotation
        Vec3d positionEyes = mc.player.getPositionEyes(mc.getRenderPartialTicks());
        Vec3d rotationEyes = mc.player.getLook(mc.getRenderPartialTicks());
        // Precision
        int precision = 2;
        // Iterate for every blocks
        for (int i = 0; i < (int) rangeMax; i++) {
            // Iterate for the precision
            for (int j = precision; j > 0; j--) {
                // Iterate for all players
                for (EntityPlayer targetTemp : listPlayer) {
                    // Get box of the player
                    AxisAlignedBB playerBox = targetTemp.getEntityBoundingBox();
                    // Get coordinate of the vec3d
                    double xArray = positionEyes.x + (rotationEyes.x * i) + rotationEyes.x / j;
                    double yArray = positionEyes.y + (rotationEyes.y * i) + rotationEyes.y / j;
                    double zArray = positionEyes.z + (rotationEyes.z * i) + rotationEyes.z / j;
                    // If it's inside
                    if (playerBox.maxY >= yArray && playerBox.minY <= yArray
                        && playerBox.maxX >= xArray && playerBox.minX <= xArray
                        && playerBox.maxZ >= zArray && playerBox.minZ <= zArray) {
                        // Get target
                        target = targetTemp;
                    }
                }
            }
        }

        return target;
    }

    public static void fakeJump() {
        fakeJump(69); // always most packets
    }

    public static void fakeJump(boolean extra) {
        fakeJump(extra ? 3 : 4);
    }

    public static void fakeJump(int packets) {
        if (packets > 0)
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true));
        if (packets > 1)
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + .419999986887, mc.player.posZ, true));
        if (packets > 2)
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + .7531999805212, mc.player.posZ, true));
        if (packets > 3)
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.0013359791121, mc.player.posZ, true));
        if (packets > 4)
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.1661092609382, mc.player.posZ, true));
    }

    public static void fall(int distance) {

        if (distance >= 1) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - (256 - 255.67647087614426), mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - (256 - 255.16214142102697), mc.player.posZ, true));
        }
        if (distance >= 2) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - (256 - 254.5796985436761), mc.player.posZ, true));
        }
        if (distance >= 3) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - (256 - 253.93050451123716), mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - (256 - 253.21589434553871), mc.player.posZ, true));
        }
        if (distance >= 4) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - (256 - 252.43717636799826), mc.player.posZ, true));
        }
        if (distance >= 5) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - (256 - 251.59563273362986), mc.player.posZ, true));
        }
        if (distance >= 6) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - (256 - 250.6925199543718), mc.player.posZ, true));
        }
        if (distance >= 7) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - (256 - 249.72906941194748), mc.player.posZ, true));
        }
        if (distance >= 8) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - (256 - 248.70648786046942), mc.player.posZ, true));
        }
        if (distance >= 9) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - (256 - 247.62595791899085), mc.player.posZ, true));
        }
        if (distance >= 10) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - (256 - 246.4886385542065), mc.player.posZ, true));
        }
        if (distance >= 11) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - (256 - 245.2956655534993), mc.player.posZ, true));
        }

        mc.player.setPosition(mc.player.posX,mc.player.posY - distance, mc.player.posZ);

    }

    // TechAle: Return the health of the player
    public static float getHealth() {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }

    public static void centerPlayer(Vec3d centeredBlock) {

        double xDeviation = Math.abs(centeredBlock.x - mc.player.posX);
        double zDeviation = Math.abs(centeredBlock.z - mc.player.posZ);

        if (xDeviation <= 0.1 && zDeviation <= 0.1) {
            centeredBlock = Vec3d.ZERO;
        } else {
            double newX = -2;
            double newZ = -2;
            int xRel = (mc.player.posX < 0 ? -1 : 1);
            int zRel = (mc.player.posZ < 0 ? -1 : 1);
            if (BlockUtil.getBlock(mc.player.posX, mc.player.posY - 1, mc.player.posZ) instanceof BlockAir) {
                if (Math.abs((mc.player.posX % 1)) * 1E2 <= 30) {
                    newX = Math.round(mc.player.posX - (0.3 * xRel)) + 0.5 * -xRel;
                } else if (Math.abs((mc.player.posX % 1)) * 1E2 >= 70) {
                    newX = Math.round(mc.player.posX + (0.3 * xRel)) - 0.5 * -xRel;
                }
                if (Math.abs((mc.player.posZ % 1)) * 1E2 <= 30) {
                    newZ = Math.round(mc.player.posZ - (0.3 * zRel)) + 0.5 * -zRel;
                } else if (Math.abs((mc.player.posZ % 1)) * 1E2 >= 70) {
                    newZ = Math.round(mc.player.posZ + (0.3 * zRel)) - 0.5 * -zRel;
                }
            }

            if (newX == -2)
                if (mc.player.posX > Math.round(mc.player.posX)) {
                    newX = Math.round(mc.player.posX) + 0.5;
                }
                // (mc.player.posX % 1)*1E2 < 30
                else if (mc.player.posX < Math.round(mc.player.posX)) {
                    newX = Math.round(mc.player.posX) - 0.5;
                } else {
                    newX = mc.player.posX;
                }

            if (newZ == -2)
                if (mc.player.posZ > Math.round(mc.player.posZ)) {
                    newZ = Math.round(mc.player.posZ) + 0.5;
                } else if (mc.player.posZ < Math.round(mc.player.posZ)) {
                    newZ = Math.round(mc.player.posZ) - 0.5;
                } else {
                    newZ = mc.player.posZ;
                }

            mc.player.connection.sendPacket(new CPacketPlayer.Position(newX, mc.player.posY, newZ, true));
            mc.player.setPosition(newX, mc.player.posY, newZ);
        }
    }
}