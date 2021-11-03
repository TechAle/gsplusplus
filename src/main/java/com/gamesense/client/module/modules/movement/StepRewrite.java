package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
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

    DoubleSetting height = registerDouble("Height", 2.5, 0.5, 2.5);
    ModeSetting mode = registerMode("Mode", Arrays.asList("NCP", "Vanilla"), "NCP");
    BooleanSetting onGround = registerBoolean("On Ground", false);
    BooleanSetting timer = registerBoolean("Timer", false, () -> mode.getValue().equalsIgnoreCase("NCP"));

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
    private final Listener<StepEvent> playerMoveEventListener = new Listener<>(event -> {

        if (!(event.getY() == 1 || event.getY() == 1.5 || event.getY() == 2 || event.getY() == 2.5))
            return;

        if (timer.getValue()) mc.timer.tickLength = 300f;

        if (mc.player.onGround || !onGround.getValue()) {
            float h = event.getY();

            if (mode.getValue().equalsIgnoreCase("NCP")) {
                if (h == 1) {
                    final double[] oneOffset = {0.42, 0.753};
                    for (double v : oneOffset) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + v, mc.player.posZ, mc.player.onGround));
                    }
                    MessageBus.sendClientPrefixMessage("1");
                } else if (h == 1.5 && height.getValue().floatValue() >= 1.5) {
                    final double[] oneFiveOffset = {0.42, 0.75, 1.0, 1.16, 1.23, 1.2};
                    for (double v : oneFiveOffset) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + v, mc.player.posZ, mc.player.onGround));
                    }
                    MessageBus.sendClientPrefixMessage("1.5");
                } else if (h == 2&& height.getValue().floatValue() >= 2) {
                    final double[] twoOffset = {0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
                    for (double v : twoOffset) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + v, mc.player.posZ, mc.player.onGround));
                    }
                    MessageBus.sendClientPrefixMessage("2");
                } else if (h == 2.5&& height.getValue().floatValue() >= 2.5) {
                    final double[] twoFiveOffset = {0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};
                    for (double v : twoFiveOffset) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + v, mc.player.posZ, mc.player.onGround));
                    }
                    MessageBus.sendClientPrefixMessage("2.5");
                } else return;
            }

            mc.player.posY += h;
        }
        if (timer.getValue()) mc.timer.tickLength = 50f;
    });

}
