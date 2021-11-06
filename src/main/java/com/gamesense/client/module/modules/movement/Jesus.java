package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.BoundingBoxEvent;
import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "Jesus", category = Category.Movement)
public class Jesus extends Module {

    @EventHandler
    private final Listener<BoundingBoxEvent> boundingBoxEventListener = new Listener<>(event -> {

        if (event.getBlock().equals(Blocks.WATER) && !mc.gameSettings.keyBindSneak.isKeyDown())
            event.setbb(Block.FULL_BLOCK_AABB);

    });

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (mc.world.getBlockState(new BlockPos(mc.player.getPositionVector())).getBlock().equals(Blocks.WATER) && !mc.gameSettings.keyBindSneak.isKeyDown()) {

            mc.player.motionY = 0.1;

        }

    });

}
