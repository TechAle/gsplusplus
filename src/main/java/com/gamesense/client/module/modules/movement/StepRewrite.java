package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Arrays;

@Module.Declaration(name = "StepRewrite", category = Category.Movement)
public class StepRewrite extends Module {

    DoubleSetting height = registerDouble("Height", 2.5, 0.5, 2.5);
    ModeSetting mode = registerMode("Mode", Arrays.asList("NCP", "Vanilla", "Beta"), "NCP");
    BooleanSetting instant = registerBoolean("Instant",false,() -> mode.getValue().equalsIgnoreCase("Beta"));
    BooleanSetting onGround = registerBoolean("On Ground", false);

    int jump;

    double[] one = new double[]{0.41999998688698D, 0.7531999805212D};
    double[] oneFive = new double[]{0.42D, 0.753D, 1.001D, 1.084D, 1.006D};
    double[] two = new double[]{0.425D, 0.821D, 0.699D, 0.599D, 1.022D, 1.372D, 1.652D, 1.869D};
    double[] twoFive = new double[]{0.425D, 0.821D, 0.699D, 0.599D, 1.022D, 1.372D, 1.652D, 1.869D, 2.019D, 1.907D};

    double[] betaShared = {.419999986887,.7531999805212,1.0013359791121,1.1661092609382,1.249187078744682,1.176759275064238};
    double[] betaOneFive = {1.5};
    double[] betaTwo = {1.596759261951216,1.929959255585439,2};

    @Override
    public void onUpdate() {
        if (mode.getValue().equalsIgnoreCase("Vanilla")) {
            mc.player.stepHeight = height.getValue().floatValue();
        } else
            mc.player.stepHeight = 0.5f;


        if ((canStep() || jump != 69) && (mode.getValue().equalsIgnoreCase("NCP") || mode.getValue().equalsIgnoreCase("Beta")) && (mc.player.onGround || !onGround.getValue())) {

            double step = getCurrentStepHeight();

            if (mode.getValue().equalsIgnoreCase("Beta"))
                mc.player.stepHeight = 1;

            if (mode.getValue().equalsIgnoreCase("NCP"))
                switch ((int) (step * 10)) {
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
            else if (mc.player.onGround && !instant.getValue() && mode.getValue().equalsIgnoreCase("Beta"))
                switch ((int) (step*10)) {

                    case 20:
                    case 15: {

                        jump = 0;

                    }

                }

            if (mode.getValue().equalsIgnoreCase("Beta") && !instant.getValue() && height.getValue() > 1) {
                if (jump != 7)
                    jump++;
                else {
                    mc.player.motionY = .42;
                    jump = 69;
                }
            }

            if (mode.getValue().equalsIgnoreCase("Beta") && instant.getValue()) {

                // we fakejump, then fall a bit, then jump again, this is basically double jump

                /*
                Client	Position	645	2255668	x: 77.69999998807907 y: 3.419999986886978 z: 31.619644581562387 onGround: false
                Client	Position	693	48	x: 77.69999998807907 y: 3.753199980521202 z: 31.619644581562387 onGround: false
                Client	Position	744	51	x: 77.69999998807907 y: 4.001335979112148 z: 31.619644581562387 onGround: false
                Client	Position	793	49	x: 77.69999998807907 y: 4.166109260938215 z: 31.619644581562387 onGround: false
                Client	Position	845	52	x: 77.69999998807907 y: 4.249187078744682 z: 31.619644581562387 onGround: false
                Client	Position	944	99	x: 77.69999998807907 y: 4.176759275064238 z: 31.619644581562387 onGround: false
                Client	Position	994	50	x: 77.69999998807907 y: 4.596759261951216 z: 31.619644581562387 onGround: false
                Client	Position	1044	50	x: 77.69999998807907 y: 4.929959255585439 z: 31.619644581562387 onGround: false
                Client	Position	1094	50	x: 77.69999998807907 y: 5.178095254176385 z: 31.619644581562387 onGround: false <- we stop it here and just move up to y 5 (+ 2 from origional pos)
                Client	Position	1144	50	x: 77.69999998807907 y: 5.3428685360024515 z: 31.619644581562387 onGround: false
                Client	Position	1194	50	x: 77.69999998807907 y: 5.425946353808919 z: 31.619644581562387 onGround: false
                Client	Position	1295	101	x: 77.69999998807907 y: 5.3535185501284746 z: 31.619644581562387 onGround: false
                Client	Position	1346	51	x: 77.69999998807907 y: 5.201183363277917 z: 31.619644581562387 onGround: false
                Client	Position	1392	46	x: 77.69999998807907 y: 4.973494875732929 z: 31.619644581562387 onGround: false
                Client	Position	1444	52	x: 77.69999998807907 y: 4.671960152070149 z: 31.619644581562387 onGround: false
                Client	Position	1493	49	x: 77.69999998807907 y: 4.298056115603426 z: 31.619644581562387 onGround: false
                 */

                doStep(betaShared);

                if (step == 1.5 && height.getValue() >= 1.5)
                    doStep(betaOneFive);
                else if (step == 2 && height.getValue() >= 2)
                    doStep(betaTwo);
            }

            if (mode.getValue().equalsIgnoreCase("NCP") || instant.getValue())
                mc.player.setPosition(mc.player.posX + mc.player.motionX, mc.player.posY + step, mc.player.posZ + mc.player.motionZ);

        }
    }

    void doStep(double[] offsets) {

        for (double i : offsets) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + i, mc.player.posZ, false));
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

}
