package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.server.SPacketTimeUpdate;

@Module.Declaration(name = "ClientTime", category = Category.Render)
public class ClientTime extends Module{

    IntegerSetting time = registerInteger("Time", 1000,0, 23000);

    @Override
    public void onUpdate() {
        mc.world.setWorldTime(time.getValue());
    }

    @EventHandler
    private final Listener<PacketEvent.Receive> noTimeUpdates = new Listener<>(event -> {

       if (event.getPacket() instanceof SPacketTimeUpdate) {

           event.cancel();

       }

    });

}
