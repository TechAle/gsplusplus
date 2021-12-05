package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

@Module.Declaration(name = "WallClimb", category = Category.Movement)
public class WallClimb extends Module {

    DoubleSetting speed = registerDouble("Speed", 0.42, 0, 1);

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (mc.player.collidedHorizontally && (mc.player.movementInput.moveForward != 0 || mc.player.movementInput.moveStrafe != 0))
            event.setY(speed.getValue());

    });

}
