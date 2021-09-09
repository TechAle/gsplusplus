package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.google.common.collect.Sets;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.*;

@Module.Declaration(name = "HoleSnap", category = Category.Movement)
public class HoleSnap extends Module {

    DoubleSetting range = registerDouble("Range", 0, 4, 10);
    DoubleSetting sens = registerDouble("Sens", 0.5, 0.5, 1);

    BlockPos hole;

    double yawRad,
            speed;

    double lastDist = -1;
    int distPos = -1;

    private ConcurrentHashMap<AxisAlignedBB, Integer> holes;

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

        if (!HoleUtil.isHole(new BlockPos(mc.player.getPositionVector()), true, true).getType().equals(HoleUtil.HoleType.NONE) || mc.gameSettings.keyBindSneak.isKeyDown()) {
            disable();
        } else {


            yawRad = RotationUtil.getRotationTo(mc.player.getPositionVector().add(-0.5, 0, -0.5), new Vec3d(hole)).x * PI / 180;

            if (mc.player.onGround)
                speed = 0.2805;
            else
                speed = (mc.player.motionX * mc.player.motionZ) / 2;

            mc.player.motionX = -sin(yawRad) * speed;
            mc.player.motionZ = cos(yawRad) * speed;

            if (mc.player.getDistance(hole.getX(), mc.player.posY, hole.getZ()) < 0.5) {
                mc.player.setPositionAndUpdate(Math.floor(hole.x) + 0.5, mc.player.posY, Math.floor(hole.z) + 0.5);
                mc.player.setVelocity(0, 0, 0);
                disable();
            }

        }

    }

    private BlockPos findHoles() {

        if (holes == null) {
            holes = new ConcurrentHashMap<>();
        } else {
            holes.clear();
        }

        int range = (int) Math.ceil(this.range.getValue());

        HashSet<BlockPos> possibleHoles = Sets.newHashSet();
        List<BlockPos> blockPosList = EntityUtil.getSphere(PlayerUtil.getPlayerPos(), range, range, false, true, 0);

        for (BlockPos pos : blockPosList) {

            if (!mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            if (mc.world.getBlockState(pos.add(0, -1, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }
            if (!mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
                continue;
            }

            if (mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR)) {
                possibleHoles.add(pos);
            }
        }

        possibleHoles.forEach(pos -> {
            HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(pos, false, false);
            HoleUtil.HoleType holeType = holeInfo.getType();
            if (holeType != HoleUtil.HoleType.NONE) {
                HoleUtil.BlockSafety holeSafety = holeInfo.getSafety();
                AxisAlignedBB centreBlocks = holeInfo.getCentre();

                if (centreBlocks == null)
                    return;

                int typeHole;
                if (holeSafety == HoleUtil.BlockSafety.UNBREAKABLE) {
                    typeHole = 0;
                } else {
                    typeHole = 1;
                }

                if (holeType == HoleUtil.HoleType.SINGLE) {
                    holes.put(centreBlocks, typeHole);
                }

            }

            for (int i = 0; i < holes.size(); i++) {

                if (mc.player.getDistanceSq(BlockPos.fromLong(holes.get(i))) > lastDist) {
                    distPos = i;
                    lastDist = mc.player.getDistanceSq(BlockPos.fromLong(holes.get(i)));
                }

            }
        });
        return (BlockPos.fromLong(holes.get(distPos)));
    }
}
