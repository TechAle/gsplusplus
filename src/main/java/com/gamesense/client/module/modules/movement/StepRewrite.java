package com.gamesense.client.module.modules.movement;

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
        } else
            mc.player.stepHeight = 0.5f;



        if (canStep() && mode.getValue().equalsIgnoreCase("NCP") && (mc.player.onGround || !onGround.getValue())) {

            double step = getCurrentStepHeight();

            switch ((int)(step * 10)) {
                case 10: {
                    doStep(one);
                    break;
                }
                case 15: {
                    doStep(oneFive);
                    break;
                }
                case 20: {
                    doStep(two);
                    break;
                }
                case 25: {
                    doStep(twoFive);
                    break;
                }
            }

            mc.player.setPosition(mc.player.posX + mc.player.motionX,mc.player.posY + step, mc.player.posZ + mc.player.motionZ);

        }
    }

    void doStep(double[] offsets) {

        for (double i : offsets) {
            MessageBus.sendClientPrefixMessage(new CPacketPlayer.Position(mc.player.posX,mc.player.posY + i, mc.player.posZ, false).toString());
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
        return mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(x, 1, z)).isEmpty();
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

    double[] one = new double[]{0.41999998688698D, 0.7531999805212D};
    double[] oneFive = new double[]{0.42D, 0.753D, 1.001D, 1.084D, 1.006D};
    double[] two = new double[]{0.425D, 0.821D, 0.699D, 0.599D, 1.022D, 1.372D, 1.652D, 1.869D};
    double[] twoFive = new double[]{0.425D, 0.821D, 0.699D, 0.599D, 1.022D, 1.372D, 1.652D, 1.869D, 2.019D, 1.907D};

}
