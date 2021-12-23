package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

import static java.lang.Math.*;

@Module.Declaration(name = "HoleSnap", category = Category.Movement)
public class HoleSnap extends Module {

    DoubleSetting speedA = registerDouble("Speed", 0, 0, 2);
    DoubleSetting range = registerDouble("Range", 4, 0, 10);

    BlockPos hole;

    double yawRad,
            speed;

    double lastDist;
    BlockPos distPos;

    @Override
    protected void onEnable() {

        hole = null;

        hole = findHoles();

        if (hole == null)
            disable();

    }

    @Override
    protected void onDisable() {
        hole = null;
    }

    @Override
    public void onUpdate() {

        if (mc.gameSettings.keyBindSneak.isKeyDown() || HoleUtil.isInHole(mc.player, true, false)) {
            PlayerUtil.centerPlayer(mc.player.getPositionVector());
            disable();
            return;
        }

        yawRad = RotationUtil.getRotationTo(mc.player.getPositionVector().add(-0.5, 0, -0.5), new Vec3d(hole)).x * PI / 180;
        double dist = mc.player.getPositionVector().distanceTo(new Vec3d(hole.getX(), hole.getY(), hole.getZ()));

        if (mc.player.onGround)
            speed = Math.min(MotionUtil.getBaseMoveSpeed(), Math.abs(dist) / 2); // divide by 2 because motion
        else
            speed = Math.min((Math.abs(mc.player.motionX) + Math.abs(mc.player.motionZ)), Math.abs(dist) / 2);

        speed *= speedA.getValue();

        mc.player.motionX = -sin(yawRad) * speed;
        mc.player.motionZ = cos(yawRad) * speed;

    }


    private BlockPos findHoles() {

        NonNullList<BlockPos> holes = NonNullList.create();

        //from old HoleFill module, really good way to do this
        List<BlockPos> blockPosList = EntityUtil.getSphere(PlayerUtil.getPlayerPos(), range.getValue().floatValue(), range.getValue().intValue(), false, true, 0);

        blockPosList.forEach(pos -> {
            HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(pos, true, false);
            HoleUtil.HoleType holeType = holeInfo.getType();
            if (holeType != HoleUtil.HoleType.NONE) {
                AxisAlignedBB centreBlocks = holeInfo.getCentre();

                if (centreBlocks == null)
                    return;

                if (holeType == HoleUtil.HoleType.SINGLE && mc.world.isAirBlock(pos) && mc.world.isAirBlock(pos.add(0, 1, 0)) && mc.world.isAirBlock(pos.add(0, 2, 0)) && pos.getY() <= mc.player.posY) {
                    holes.add(pos);
                }
            }
        });

        distPos = new BlockPos(Double.POSITIVE_INFINITY, 69, 429);
        lastDist = (int) Double.POSITIVE_INFINITY;

        for (BlockPos blockPos : holes) {

            if (mc.player.getDistanceSq(blockPos) < lastDist) {
                distPos = blockPos;
                lastDist = mc.player.getDistanceSq(blockPos);
            }

        }

        if (!distPos.equals(new BlockPos(Double.POSITIVE_INFINITY, 69, 429))) {
            return distPos;
        } else {
            return null;
        }
    }
}
/*            for (int i = 0; i < holes.size(); i++) {

                if (mc.player.getDistanceSq(BlockPos.fromLong(holes.get(i))) > lastDist) {
                    distPos = i;
                    lastDist = mc.player.getDistanceSq(BlockPos.fromLong(holes.get(i)));
                }

            }*/