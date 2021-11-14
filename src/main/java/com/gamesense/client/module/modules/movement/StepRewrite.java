package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Arrays;

@Module.Declaration(name = "StepRewrite", category = Category.Movement)
public class StepRewrite extends Module {

    DoubleSetting height = registerDouble("Height", 2.5, 0.5, 2.5);
    ModeSetting mode = registerMode("Mode", Arrays.asList("NCP", "Vanilla"), "NCP");
    BooleanSetting onGround = registerBoolean("On Ground", false);
    BooleanSetting timer = registerBoolean("Timer", false, () -> mode.getValue().equalsIgnoreCase("NCP"));

    @Override
    public void onUpdate() {
        if (mode.getValue().equalsIgnoreCase("Vanilla")) {
            mc.player.stepHeight = height.getValue().floatValue();
        }

        if (canStep() && mode.getValue().equalsIgnoreCase("NCP") && (mc.player.onGround || !onGround.getValue())) {

            double step = getCurrentStepHeight();

            switch (String.valueOf(step)) {
                case "1": {
                    doStep(one);
                    break;
                }
                case "1.5": {
                    doStep(oneFive);
                    break;
                }
                case "2": {
                    doStep(two);
                    break;
                }
                case "2.5": {
                    doStep(twoFive);
                    break;
                }
            }

            mc.player.setPositionAndUpdate(mc.player.posX,mc.player.posY + step, mc.player.posZ);

        }
    }

    void doStep(double[] offsets) {

        for (double i : offsets) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX,mc.player.posY + i, mc.player.posZ, false));
        }
    }

    @Override
    protected void onDisable() {
        mc.player.stepHeight = 0.5f;
    }


    private boolean canStep() {
        float rotationYaw = mc.player.rotationYaw;
        if (mc.player.moveForward < 0.0F)
            rotationYaw += 180.0F;
        float forward = 1.0F;
        if (mc.player.moveForward < 0.0F) {
            forward = -0.5F;
        } else if (mc.player.moveForward > 0.0F) {
            forward = 0.5F;
        }
        if (mc.player.moveStrafing > 0.0F)
            rotationYaw -= 90.0F * forward;
        if (mc.player.moveStrafing < 0.0F)
            rotationYaw += 90.0F * forward;

        float yaw = (float) Math.toRadians(rotationYaw);

        double x = -Math.sin(yaw) * 0.4D;
        double z = Math.cos(yaw) * 0.4D;
        return mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(x, 1.001335979112147D, z)).isEmpty();
    }

    private double getCurrentStepHeight() {
        boolean collided = (mc.player.onGround && mc.player.collidedHorizontally);

        if (!collided) {
            return 0.0D;
        }

        double maximumY = -1.0D;

        float rotationYaw = mc.player.rotationYaw;
        if (mc.player.moveForward < 0.0F)
            rotationYaw += 180.0F;
        float forward = 1.0F;
        if (mc.player.moveForward < 0.0F) {
            forward = -0.5F;
        } else if (mc.player.moveForward > 0.0F) {
            forward = 0.5F;
        }
        if (mc.player.moveStrafing > 0.0F)
            rotationYaw -= 90.0F * forward;
        if (mc.player.moveStrafing < 0.0F)
            rotationYaw += 90.0F * forward;

        float yaw = (float) Math.toRadians(rotationYaw);

        double x = -Math.sin(yaw) * 0.4D;
        double z = Math.cos(yaw) * 0.4D;

        AxisAlignedBB expandedBB = mc.player.getEntityBoundingBox().offset(0.0D, 0.05D, 0.0D).grow(0.05D);
        expandedBB = expandedBB.setMaxY(expandedBB.maxY + (height.getValue()));

        for (AxisAlignedBB axisAlignedBB : mc.world.getCollisionBoxes(mc.player, expandedBB)) {
            if (axisAlignedBB.maxY > maximumY)
                maximumY = axisAlignedBB.maxY;
        }

        maximumY -= mc.player.posY;
        return (maximumY > 0.0D && maximumY <= height.getValue()) ? maximumY : 0.0D;
    }

    public static final double[] one = {0.42, 0.753};
    public static final double[] oneFive = {0.42, 0.75, 1.0, 1.16, 1.23, 1.2};
    public static final double[] two = {0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
    public static final double[] twoFive = {0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};

}
