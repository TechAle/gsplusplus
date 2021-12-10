package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.GeometryMasks.Quad;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.BlockEvent;

import java.util.List;

@Module.Declaration(name = "StorageESP", category = Category.Render)
public class StorageESP extends Module {

    List<BlockPos> storage = new NonNullList<BlockPos>(){};

    @SuppressWarnings("unused")
    @EventHandler
    final Listener<BlockEvent> blockEventListener = new Listener<>(event -> {
        if (event.getState().getBlock() instanceof BlockChest || event.getState().getBlock() instanceof BlockShulkerBox || event.getState().getBlock() instanceof BlockEnderChest)
            storage.add(event.getPos());
        else
            storage.remove(event.getPos());
    });

    @Override
    public void onWorldRender(RenderEvent event) {
        for (BlockPos pos : storage) {

            Block block = mc.world.getBlockState(pos).getBlock();
            int colour = block.blockMapColor.colorValue;

            int r = colour >> 16;
            int g = colour >> 8 & 255;
            int b = colour & 255;

            RenderUtil.drawBox(pos, 1, new GSColor(r,g,b), Quad.ALL);
            RenderUtil.drawBoundingBox(pos,1,1, new GSColor(r,g,b));

        }
    }
}
