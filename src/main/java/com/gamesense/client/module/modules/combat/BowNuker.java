package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

@Module.Declaration(name = "BowNuker", category = Category.Combat)
public class BowNuker extends Module {

    @EventHandler
    private final Listener<PacketEvent.Send> packetEventListener = new Listener<>(event -> {});

}
