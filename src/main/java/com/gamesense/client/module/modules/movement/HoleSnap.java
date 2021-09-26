package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import it.unimi.dsi.fastutil.booleans.BooleanSet;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.*;

@Module.Declaration(name = "HoleSnap", category = Category.Movement)
public class HoleSnap extends Module {

    DoubleSetting range = registerDouble("Range", 4, 0, 10);
    BooleanSetting render = registerBoolean("Render", true);
    IntegerSetting width = registerInteger("Width", 2, 0, 10, () -> render.getValue());
    ColorSetting colour = registerColor("Colour", new GSColor(0, 255, 0), () -> render.getValue());

    BlockPos hole;

    double yawRad,
            speed;

    double lastDist;
    BlockPos distPos;

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
            double dist = mc.player.getPositionVector().distanceTo(new Vec3d(hole.getX(), hole.getY(), hole.getZ()));

            if (mc.player.onGround)
                speed = Math.min(0.2805, dist / 2.0);
            else
                speed = (mc.player.motionX * mc.player.motionZ) / 2;

            mc.player.motionX = -sin(yawRad) * speed;
            mc.player.motionZ = cos(yawRad) * speed;

            if (render.getValue())
                RenderUtil.drawLine(mc.player.posX, Math.floor(mc.player.posY), mc.player.posZ, hole.x, Math.floor(hole.y), hole.z, colour.getColor(), width.getValue());

            if (mc.player.getDistance(hole.getX(), mc.player.posY, hole.getZ()) < 0) {
                mc.player.setPositionAndUpdate(Math.floor(hole.x) + 0.5, mc.player.posY, Math.floor(hole.z) + 0.5);
                mc.player.setVelocity(0, 0, 0);
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