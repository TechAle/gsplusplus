package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@Module.Declaration(name = "HoleSnap", category = Category.Movement)
public class HoleSnap extends Module { // im bad at movement :pensive:

    BooleanSetting doubles = registerBoolean("Double Holes", false);
    DoubleSetting range = registerDouble("Range", 4, 0, 10);

    int i;
    double dist;
    int bestHole;

    @Override
    public void onUpdate() {

        PlayerUtil.centerPlayer(new Vec3d(findHoles().get(closestHole())));

    }

    private List<BlockPos> findHoles() {
        NonNullList<BlockPos> holes = NonNullList.create();

        //from HoleFill module, really good way to do this
        List<BlockPos> blockPosList = EntityUtil.getSphere(PlayerUtil.getPlayerPos(), range.getValue().floatValue(), range.getValue().intValue(), false, true, 0);

        blockPosList.forEach(pos -> {
            HoleUtil.HoleInfo holeInfo = HoleUtil.isHole(pos, false, false);
            HoleUtil.HoleType holeType = holeInfo.getType();
            if (holeType != HoleUtil.HoleType.NONE) {
                AxisAlignedBB centreBlocks = holeInfo.getCentre();

                if (centreBlocks == null)
                    return;

                if (holeType == HoleUtil.HoleType.DOUBLE && doubles.getValue()) {
                    holes.add(pos);
                } else if (holeType == HoleUtil.HoleType.SINGLE) {
                    holes.add(pos);
                }
            }
        });

        return holes;
    }

    private int closestHole() {

        bestHole = 0;

        // Get our closest hole
        while (i < findHoles().size()) {

            dist = findHoles().get(i).distanceSq(mc.player.getPosition());

            // if the current hole we are inspecting distance is less than current closest hole
            if (dist < findHoles().get(bestHole).distanceSq(mc.player.getPosition())) {

                bestHole = i; // closest hole = current hole

            }

            i++;

        }

        return bestHole;

    }

}
