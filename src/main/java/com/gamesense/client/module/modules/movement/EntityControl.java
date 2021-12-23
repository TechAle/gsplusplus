package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.ControlEvent;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.event.type.Cancellable;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

/**
 * @source https://github.com/bebeli555/CookieClient/blob/main/src/main/java/me/bebeli555/cookieclient/mods/movement/EntityControl.java
 * */

@Module.Declaration(name = "EntityControl", category = Category.Movement)
public class EntityControl extends Module {

    @EventHandler
    private final Listener<ControlEvent> packetSendListener = new Listener<>(Cancellable::cancel);

    @Override
    public void onUpdate() {
        if (mc.player.ridingEntity != null) {
            mc.player.ridingEntity.rotationYaw = mc.player.rotationYaw;
        }
    }
}
