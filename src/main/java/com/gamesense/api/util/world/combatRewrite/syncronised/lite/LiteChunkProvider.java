package com.gamesense.api.util.world.combatRewrite.syncronised.lite;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;

public class LiteChunkProvider {
    private final LiteChunk blankChunk;

    private final Long2ObjectOpenHashMap<LiteChunk> loadedChunks = new Long2ObjectOpenHashMap<LiteChunk>(8192)
    {
        protected void rehash(int p_rehash_1_) {
            if (p_rehash_1_ > this.key.length) {
                super.rehash(p_rehash_1_);
            }
        }
    };

    public LiteChunkProvider() {
        this.blankChunk = new EmptyLiteChunk( 0, 0);
    }

    public void unloadChunk(int x, int z) {
        LiteChunk chunk = this.provideChunk(x, z);

        if (!chunk.isEmpty()) {
            chunk.onUnload();
        }

        this.loadedChunks.remove(ChunkPos.asLong(x, z));
    }

    @Nullable
    public LiteChunk getLoadedChunk(int x, int z) {
        return this.loadedChunks.get(ChunkPos.asLong(x, z));
    }

    public LiteChunk loadChunk(int chunkX, int chunkZ) {
        LiteChunk chunk = new LiteChunk(chunkX, chunkZ);
        this.loadedChunks.put(ChunkPos.asLong(chunkX, chunkZ), chunk);
        chunk.markLoaded(true);
        return chunk;
    }

    public LiteChunk provideChunk(int x, int z) {
        return MoreObjects.firstNonNull(this.getLoadedChunk(x, z), this.blankChunk);
    }

    public Long2ObjectOpenHashMap<LiteChunk> getLoadedChunks() {
        return loadedChunks.clone();
    }

    public String makeString() {
        return "MultiplayerChunkCache: " + this.loadedChunks.size() + ", " + this.loadedChunks.size();
    }

    public boolean isChunkGeneratedAt(int x, int z) {
        return this.loadedChunks.containsKey(ChunkPos.asLong(x, z));
    }
}
