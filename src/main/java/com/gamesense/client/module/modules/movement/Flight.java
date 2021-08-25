package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

@Module.Declaration(name = "Flight", category = Category.Movement)
public class Flight extends Module {

    DoubleSetting speed = registerDouble("Speed", 2,0,10);
    DoubleSetting ySpeed = registerDouble("Y Speed", 1,0,10);
    DoubleSetting glideSpeed = registerDouble("Glide Speed", 0,-10,10);

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (mc.gameSettings.keyBindJump.isKeyDown()) {

            event.setY(ySpeed.getValue());

        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {

            event.setY(-ySpeed.getValue());

        } else {

            event.setY(-glideSpeed.getValue());

        }

        if (MotionUtil.isMoving(mc.player)){
            MotionUtil.setSpeed(mc.player, speed.getValue());
        } else {

            event.setX(0);
            event.setZ(0);

        }

    });

}
