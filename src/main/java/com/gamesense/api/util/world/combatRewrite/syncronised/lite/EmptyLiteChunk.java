package com.gamesense.api.util.world.combatRewrite.syncronised.lite;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class EmptyLiteChunk extends LiteChunk {
    public EmptyLiteChunk(int x, int z) {
        super(x, z);
    }

    public boolean isAtLocation(int x, int z) {
        return x == this.x && z == this.z;
    }

    public IBlockState getBlockState(BlockPos pos) {
        return Blocks.AIR.getDefaultState();
    }

    public IBlockState setBlockState(BlockPos pos, IBlockState state) {
        return Blocks.AIR.getDefaultState();
    }

    public void onLoad() {
    }

    public void onUnload() {
    }

    public boolean isEmpty() {
        return true;
    }

    public boolean isEmptyBetween(int startY, int endY) {
        return true;
    }
}