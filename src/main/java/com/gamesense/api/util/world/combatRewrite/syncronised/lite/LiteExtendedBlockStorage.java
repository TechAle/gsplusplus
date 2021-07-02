package com.gamesense.api.util.world.combatRewrite.syncronised.lite;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.BlockStateContainer;

public class LiteExtendedBlockStorage {
    private final int yBase;
    private int blockRefCount;

    private final BlockStateContainer data;

    public LiteExtendedBlockStorage(int y) {
        this.yBase = y;
        this.data = new BlockStateContainer();
    }

    public IBlockState get(int x, int y, int z) {
        return this.data.get(x, y, z);
    }

    public void set(int x, int y, int z, IBlockState state) {
        IBlockState iblockstate = this.get(x, y, z);
        Block block = iblockstate.getBlock();
        Block block1 = state.getBlock();

        if (block != Blocks.AIR) {
            --this.blockRefCount;
        }

        if (block1 != Blocks.AIR) {
            ++this.blockRefCount;
        }

        this.data.set(x, y, z, state);
    }

    public boolean isEmpty() {
        return this.blockRefCount == 0;
    }

    public int getYLocation() {
        return this.yBase;
    }

    public void recalculateRefCounts() {
        this.blockRefCount = 0;

        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    Block block = this.get(i, j, k).getBlock();

                    if (block != Blocks.AIR) {
                        ++this.blockRefCount;
                    }
                }
            }
        }
    }

    public void setBlockRefCount(int blockRefCount) {
        this.blockRefCount = blockRefCount;
    }

    public BlockStateContainer getData() {
        return this.data;
    }
}
