package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

@Module.Declaration(name = "HoleSnap", category = Category.Movement)
public class HoleSnap extends Module {

    DoubleSetting range = registerDouble("Range", 0,4,10);

    @Override
    public void onUpdate() {

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
        int holePos;

        for (int i = 0; i < holes.size(); i++) {

            BlockPos current = new BlockPos(holes.get(i));
            holeDist = ((float) current.getDistance(((int) mc.player.posX), ((int) mc.player.posY), ((int) mc.player.posZ)));

            if (holeDist < lastHoleDist)
                lastHoleDist = holeDist;
            holePos = i;

        }

    }
}
