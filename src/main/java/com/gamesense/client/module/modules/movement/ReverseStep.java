package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

import java.util.Arrays;

@Module.Declaration(name = "ReverseStep", category = Category.Movement)
public class ReverseStep extends Module {

    ModeSetting reverse = registerMode("Reverse Mode", Arrays.asList("Strict", "Vanilla"), "Vanilla");
    DoubleSetting height = registerDouble("Height", 2.5, 0.5, 2.5);
    
    boolean doIt;

    @EventHandler
    final Listener<PlayerMoveEvent> eventListener = new Listener<>(event -> {

        if (!(mc.player != null && mc.player.onGround && !mc.player.isInWater() && !mc.player.isOnLadder()))
            return;

        float dist = 69696969;

        if (reverse.getValue().equalsIgnoreCase("Vanilla")) {
            for (double y = 0.0; y < height.getValue() + 0.5; y += 0.01) {
                if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, -y, 0.0)).isEmpty()) {
                    mc.player.motionY = -10.0;
                }
            }
        } else {

            for (double y = 0.0; y < height.getValue() + 1; y += 0.01) {
                if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, -y, 0.0)).isEmpty())
                    dist = (float) y;

                doIt =
                        !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, -dist - 0.1, 0.0)).isEmpty();

            }

            if (dist <= height.getValue() && doIt) {
                PlayerUtil.fall((int) (dist + 0.1));
                MessageBus.sendClientRawMessage(dist + "");
                event.setVelocity(0,0,0);
                mc.player.setVelocity(mc.player.motionX,mc.player.motionY,mc.player.motionZ);
            }

        }
    });
}
