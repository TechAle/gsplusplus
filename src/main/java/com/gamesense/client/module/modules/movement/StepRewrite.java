package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.GameSenseEvent;
import com.gamesense.api.event.events.StepEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Arrays;

@Module.Declaration(name = "StepRewrite", category = Category.Movement)
public class StepRewrite extends Module {

    DoubleSetting height = registerDouble("Height", 2.5, 0.5, 2.5);
    ModeSetting mode = registerMode("Mode", Arrays.asList("NCP", "Vanilla", "Beta"), "NCP");
    BooleanSetting onGround = registerBoolean("On Ground", false);

    double[] one = new double[]{0.41999998688698D, 0.7531999805212D};
    double[] oneFive = new double[]{0.42D, 0.753D, 1.001D, 1.084D, 1.006D};
    double[] two = new double[]{0.425D, 0.821D, 0.699D, 0.599D, 1.022D, 1.372D, 1.652D, 1.869D};
    double[] twoFive = new double[]{0.425D, 0.821D, 0.699D, 0.599D, 1.022D, 1.372D, 1.652D, 1.869D, 2.019D, 1.907D};

    double[] betaShared = {.419999986887, .7531999805212, 1.0013359791121, 1.1661092609382, 1.249187078744682, 1.176759275064238};
    double[] betaOneFive = {1.5};
    double[] betaTwo = {1.596759261951216, 1.929959255585439, 2};

    @Override
    public void onUpdate() {
        mc.player.stepHeight = height.getValue().floatValue();
    }

    @Override
    protected void onDisable() {
        mc.player.stepHeight = 0.5f;
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<StepEvent> stepEventListener = new Listener<>(event -> {

        double step = mc.player.getEntityBoundingBox().minY - mc.player.posY;

        if (step > height.getValue())
            return;

        if ((mode.getValue().equalsIgnoreCase("NCP") || mode.getValue().equalsIgnoreCase("Beta")) && (mc.player.onGround || !onGround.getValue())) {

            if (mode.getValue().equalsIgnoreCase("Beta"))
                mc.player.stepHeight = 1;

            if (mode.getValue().equalsIgnoreCase("NCP"))
                switch ((int) (step * 10)) {
                    case 10: {
                        sendOffsets(one);
                        break;
                    }
                    case 15: {
                        sendOffsets(oneFive);
                        break;
                    }
                    case 20: {
                        sendOffsets(two);
                        break;
                    }
                    case 25: {
                        sendOffsets(twoFive);
                        break;
                    }
                }

            if (mode.getValue().equalsIgnoreCase("Beta")) {

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

                sendOffsets(betaShared);

                if (step == 1.5)
                    sendOffsets(betaOneFive);
                else if (step == 2)
                    sendOffsets(betaTwo);
            }

        }
    });

    void sendOffsets(double[] offsets) {

        for (double i : offsets) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + i, mc.player.posZ, false));
        }

    }

}
