package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.StepEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.Arrays;

@Module.Declaration(name = "StepRewrite", category = Category.Movement)
public class StepRewrite extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("NCP", "Vanilla", "Beta"), "NCP");
    ModeSetting height = registerMode("Height", Arrays.asList("1", "1.5", "2", "2.5"), "2.5", () -> !mode.getValue().equalsIgnoreCase("Beta"));
    ModeSetting bHeight = registerMode("Height", Arrays.asList("1", "1.5", "2"), "2", () -> mode.getValue().equalsIgnoreCase("Beta"));
    BooleanSetting onGround = registerBoolean("On Ground", false);
    BooleanSetting timer = registerBoolean("Timer", false, () -> !mode.getValue().equalsIgnoreCase("VANILLA"));
    DoubleSetting multiplier = registerDouble("Multiplier", 1, 0.1, 3, () -> timer.getValue() && timer.isVisible());
    BooleanSetting debug = registerBoolean("Debug Height", false);

    double[] one = new double[]{0.41999998688698D, 0.7531999805212D};
    double[] oneFive = new double[]{0.42D, 0.753D, 1.001D, 1.084D, 1.006D};
    double[] two = new double[]{0.425D, 0.821D, 0.699D, 0.599D, 1.022D, 1.372D, 1.652D, 1.869D};
    double[] twoFive = new double[]{0.425D, 0.821D, 0.699D, 0.599D, 1.022D, 1.372D, 1.652D, 1.869D, 2.019D, 1.907D};

    double[] betaShared = {.419999986887, .7531999805212, 1.0013359791121, 1.1661092609382, 1.249187078744682, 1.176759275064238};
    double[] betaOneFive = {1.5};
    double[] betaTwo = {1.596759261951216, 1.929959255585439, 2};
    double[] betaTwoFive = {1.596759261951216, 1.929959255585439, 2.178095254176385, 2.3428685360024515, 2.425946353808919};

    /*
    Client	Position	645	2255668	x: 77.69999998807907 y: 3.419999986886978 z: 31.619644581562387 onGround: false
    Client	Position	693	48	x: 77.69999998807907 y: 3.753199980521202 z: 31.619644581562387 onGround: false
    Client	Position	744	51	x: 77.69999998807907 y: 4.001335979112148 z: 31.619644581562387 onGround: false
    Client	Position	793	49	x: 77.69999998807907 y: 4.166109260938215 z: 31.619644581562387 onGround: false
    Client	Position	845	52	x: 77.69999998807907 y: 4.249187078744682 z: 31.619644581562387 onGround: false
    Client	Position	944	99	x: 77.69999998807907 y: 4.176759275064238 z: 31.619644581562387 onGround: false
    Client	Position	994	50	x: 77.69999998807907 y: 4.596759261951216 z: 31.619644581562387 onGround: false
    Client	Position	1044	50	x: 77.69999998807907 y: 4.929959255585439 z: 31.619644581562387 onGround: false
    Client	Position	1094	50	x: 77.69999998807907 y: 5.178095254176385 z: 31.619644581562387 onGround: false
    Client	Position	1144	50	x: 77.69999998807907 y: 5.3428685360024515 z: 31.619644581562387 onGround: false
    Client	Position	1194	50	x: 77.69999998807907 y: 5.425946353808919 z: 31.619644581562387 onGround: false
    Client	Position	1295	101	x: 77.69999998807907 y: 5.3535185501284746 z: 31.619644581562387 onGround: false
    Client	Position	1346	51	x: 77.69999998807907 y: 5.201183363277917 z: 31.619644581562387 onGround: false
    Client	Position	1392	46	x: 77.69999998807907 y: 4.973494875732929 z: 31.619644581562387 onGround: false
    Client	Position	1444	52	x: 77.69999998807907 y: 4.671960152070149 z: 31.619644581562387 onGround: false
    Client	Position	1493	49	x: 77.69999998807907 y: 4.298056115603426 z: 31.619644581562387 onGround: false
    */

    boolean prevTickTimer;
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<StepEvent> stepEventListener = new Listener<>(event -> {

        double step = (event.getBB().minY - mc.player.posY);

        if (debug.getValue())
            MessageBus.sendClientPrefixMessageWithID("Stepping " + step + " blocks", Module.getIdFromString("Stepping ... Blocks"));

        if (mode.getValue().equalsIgnoreCase("Vanilla"))
            return;

        if (mode.getValue().equalsIgnoreCase("NCP")) {
            if (step == 1) {
                sendOffsets(one);
                if (timer.getValue()) {
                    mc.timer.tickLength = 50f * (one.length + 1) * multiplier.getValue().floatValue();
                    prevTickTimer = true;
                }
            } else if (step == 1.5) {
                sendOffsets(oneFive);
                if (timer.getValue()) {
                    mc.timer.tickLength = 50f * (oneFive.length + 1) * multiplier.getValue().floatValue();
                    prevTickTimer = true;
                }
            } else if (step == 2) {
                sendOffsets(two);
                if (timer.getValue()) {
                    mc.timer.tickLength = 50f * (two.length + 1) * multiplier.getValue().floatValue();
                    prevTickTimer = true;
                }
            } else if (step == 2.5) {
                sendOffsets(twoFive);
                if (timer.getValue()) {
                    mc.timer.tickLength = 50f * (twoFive.length + 1) * multiplier.getValue().floatValue();
                    prevTickTimer = true;
                }
            } else event.cancel();

        } else if (mode.getValue().equalsIgnoreCase("Beta")) {
            if (step == 1.5) {
                sendOffsets(betaShared);
                sendOffsets(betaOneFive);
                if (timer.getValue()) {
                    mc.timer.tickLength = 50f * (betaShared.length + betaOneFive.length + 1) * multiplier.getValue().floatValue();
                    prevTickTimer = true;
                }
            } else if (step == 2) {
                sendOffsets(betaShared);
                sendOffsets(betaTwo);
                if (timer.getValue()) {
                    mc.timer.tickLength = 50f * (betaShared.length + betaTwo.length + 1) * multiplier.getValue().floatValue();
                    prevTickTimer = true;
                }
            } else if (step == 2.5) {
                sendOffsets(betaShared);
                sendOffsets(betaTwoFive);
                if (timer.getValue()) {
                    mc.timer.tickLength = 50f * (betaShared.length + betaTwoFive.length + 1) * multiplier.getValue().floatValue();
                    prevTickTimer = true;
                }
            } else event.cancel();
        }

    });

    @Override
    public void onUpdate() {
        mc.player.stepHeight = onGround.getValue() && !mc.player.onGround ? 0.5f : getHeight(mode.getValue()); // 0.5 height if not on ground so it doesnt flag
        if (prevTickTimer) {
            prevTickTimer = false;
            mc.timer.tickLength = 50f;
        }
    }

    float getHeight(String mode) {

        return Float.parseFloat(mode.equals("Beta") ? bHeight.getValue() : height.getValue());

    }

    @Override
    protected void onDisable() {
        mc.player.stepHeight = 0.5f;
        mc.timer.tickLength = 50;
    }

    void sendOffsets(double[] offsets, int add) {
        for (double i : offsets) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + i + add, mc.player.posZ, false));
        }
    }

    void sendOffsets(double[] offsets) {
        sendOffsets(offsets, 0);
    }

}
