package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.GameSenseEvent;
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
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Arrays;

@Module.Declaration(name = "StepRewrite", category = Category.Movement)
public class StepRewrite extends Module {

    DoubleSetting height = registerDouble("Height", 2.5, 0.5, 2.5);
    ModeSetting mode = registerMode("Mode", Arrays.asList("NCP", "Vanilla", "Beta"), "NCP");
    BooleanSetting onGround = registerBoolean("On Ground", false);
    BooleanSetting timer = registerBoolean("Timer",false, () -> !mode.getValue().equalsIgnoreCase("VANILLA"));
    DoubleSetting multiplier = registerDouble("Multiplier",1,0.1,3,() -> timer.getValue() && timer.isVisible());
    BooleanSetting debug = registerBoolean("Debug Height", false);

    double[] one = new double[]{0.41999998688698D, 0.7531999805212D};
    double[] oneFive = new double[]{0.42D, 0.753D, 1.001D, 1.084D, 1.006D};
    double[] two = new double[]{0.425D, 0.821D, 0.699D, 0.599D, 1.022D, 1.372D, 1.652D, 1.869D};
    double[] twoFive = new double[]{0.425D, 0.821D, 0.699D, 0.599D, 1.022D, 1.372D, 1.652D, 1.869D, 2.019D, 1.907D};

    double[] betaShared = {.419999986887, .7531999805212, 1.0013359791121, 1.1661092609382, 1.249187078744682, 1.176759275064238};
    double[] betaOneFive = {1.5};
    double[] betaTwo = {1.596759261951216, 1.929959255585439, 2};

    boolean prevTickTimer;

    @Override
    public void onUpdate() {
        mc.player.stepHeight = onGround.getValue() && !mc.player.onGround ? 0.5f : height.getValue().floatValue(); // 0.5 height if not on ground so it doesnt flag
        if (prevTickTimer) {
            prevTickTimer = false;
            mc.timer.tickLength = 50f;
        }
    }

    @Override
    protected void onDisable() {
        mc.player.stepHeight = 0.5f;
        mc.timer.tickLength = 50;
    }

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
                    mc.timer.tickLength = 50f * one.length * multiplier.getValue().floatValue();
                    prevTickTimer = true;
                }
            }
            if (step == 1.5) {
                sendOffsets(oneFive);
                if (timer.getValue()) {
                    mc.timer.tickLength = 50f * oneFive.length * multiplier.getValue().floatValue();
                    prevTickTimer = true;
                }
            }
            if (step == 2) {
                sendOffsets(two);
                if (timer.getValue()) {
                    mc.timer.tickLength = 50f * two.length * multiplier.getValue().floatValue();
                    prevTickTimer = true;
                }
            }
            if (step == 2.5) {
                sendOffsets(twoFive);
                if (timer.getValue()) {
                    mc.timer.tickLength = 50f * twoFive.length * multiplier.getValue().floatValue();
                    prevTickTimer = true;
                }
            }
        } else if (mode.getValue().equalsIgnoreCase("Beta")) {
            if (step == 1.5) {
                sendOffsets(betaShared);
                sendOffsets(betaOneFive);
                if (timer.getValue()) {
                    mc.timer.tickLength = 50f * (betaShared.length + betaOneFive.length) * multiplier.getValue().floatValue();
                    prevTickTimer = true;
                }
            } else if (step == 2) {
                sendOffsets(betaShared);
                sendOffsets(betaTwo);
                if (timer.getValue()) {
                    mc.timer.tickLength = 50f * (betaShared.length + betaTwo.length) * multiplier.getValue().floatValue();
                    prevTickTimer = true;
                }
            }
        }

    });

    void sendOffsets(double[] offsets) {

        for (double i : offsets) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + i, mc.player.posZ, false));
        }

    }

}
