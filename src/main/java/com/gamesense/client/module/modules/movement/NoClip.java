package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.BoundingBoxEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

@Module.Declaration(name = "NoClip", category = Category.Movement)
public class NoClip extends Module {

    BooleanSetting h = registerBoolean("Keep Floor", false);

    @EventHandler
    private final Listener<BoundingBoxEvent> boundingBoxEventListener = new Listener<>(event -> {

        if (event.getPos().y >= mc.player.getPositionVector().y || !h.getValue()) {
            event.setbb(Block.NULL_AABB);
        }

    });

}
