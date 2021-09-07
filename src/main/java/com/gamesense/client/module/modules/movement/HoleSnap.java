package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static java.lang.Math.*;

@Module.Declaration(name = "HoleSnap", category = Category.Movement)
public class HoleSnap extends Module {

    DoubleSetting range = registerDouble("Range", 0, 4, 10);

    BlockPos hole;

    double yawRad,
            dist,
            speed;

    @Override
    protected void onEnable() {

        hole = findHoles();

        if (hole == null)
            disable();
        else
            hole = new BlockPos(BlockUtil.getCenterOfBlock(hole.getX(), hole.getY(), hole.getZ()));
    }


    @Override
    public void onUpdate() {

        if (!HoleUtil.isHole(new BlockPos(mc.player.getPositionVector()), true, true).getType().equals(HoleUtil.HoleType.NONE) || mc.gameSettings.keyBindSneak.isKeyDown()) {
            disable();
        } else {


            yawRad = RotationUtil.getRotationTo(mc.player.getPositionVector(), new Vec3d(hole)).x * PI / 180;
            dist = mc.player.getPositionVector().distanceTo(new Vec3d(hole));

            if (mc.player.onGround)
                speed = Math.min(0.2805, dist / 2.0);
            else
                speed = (mc.player.motionX * mc.player.motionZ) / 2;

            mc.player.motionX = -sin(yawRad) * speed;
            mc.player.motionZ = cos(yawRad) * speed;


            if (mc.player.getDistance(hole.getX(), hole.getY(), hole.getZ()) < 0.5) {
                mc.player.setPosition(Math.floor(hole.x) + 0.5, mc.player.posY, Math.floor(hole.z) + 0.5);
                disable();
            }

        }

    }

    private BlockPos findHoles() {
        NonNullList<BlockPos> holes = NonNullList.create();

        //from old HoleFill module, really good way to do this
        List<BlockPos> blockPosList = EntityUtil.getSphere(PlayerUtil.getPlayerPos(), range.getValue().floatValue(), range.getValue().intValue(), false, true, 0);

        blockPosList.forEach(pos -> {
            HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(pos, false, false);
            HoleUtil.HoleType holeType = holeInfo.getType();
            if (holeType != HoleUtil.HoleType.NONE) {
                AxisAlignedBB centreBlocks = holeInfo.getCentre();

                if (centreBlocks == null)
                    return;


            }
            if (holeType == HoleUtil.HoleType.SINGLE) {
                holes.add(pos);
            }
        });

        float holeDist;
        float lastHoleDist = 99;
        int holePos = -1;

        for (int i = 0; i < holes.size(); i++) {

            BlockPos current = new BlockPos(holes.get(i));
            holeDist = ((float) current.distanceSq(mc.player.posX, mc.player.posY, mc.player.posZ));

            if (holeDist < lastHoleDist)
                lastHoleDist = holeDist;
            holePos = i;

        }

        try {
            return holes.get(holePos);
        } catch (ArrayIndexOutOfBoundsException ignored) {

            return null;

        }

    }

}
