package com.gamesense.client.manager.managers;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.util.world.combatRewrite.syncronised.lite.LiteChunk;
import com.gamesense.api.util.world.combatRewrite.syncronised.lite.LiteChunkProvider;
import com.gamesense.client.manager.Manager;
import com.gamesense.mixin.mixins.accessor.ISPacketMultiBlockChange;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public enum WorldCopyManager implements Manager, IBlockAccess {

    INSTANCE;

    private static final Block AIR = Blocks.AIR;

    public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final WorldUpdater updater = new WorldUpdater(this);
    private LiteChunkProvider chunkProvider = new LiteChunkProvider();
    private volatile int dimension;

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return lightValue;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if (this.isOutsideBuildHeight(pos)) {
            return Blocks.AIR.getDefaultState();
        } else {
            IBlockState iBlockState;
            lock.readLock().lock();
            try {
                LiteChunk chunk = this.getChunk(pos);
                iBlockState = chunk.getBlockState(pos);
            } finally {
                lock.readLock().unlock();
            }
            return iBlockState;
        }
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return Block.isEqualTo(this.getBlockState(pos).getBlock(), AIR);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return null;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return null;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        lock.readLock().lock();
        try {
            LiteChunk chunk = getChunk(pos);
            if (chunk == null || chunk.isEmpty()) return _default;
            return getBlockState(pos).isSideSolid(this, pos, side);
        } finally {
            lock.readLock().unlock();
        }
    }

    private LiteChunk getChunk(BlockPos pos) {
        return this.chunkProvider.provideChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private boolean isOutsideBuildHeight(BlockPos pos) {
        return pos.getY() < 0 || pos.getY() >= 256;
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.PostReceive> packetReceiveListener = new Listener<>(event -> {
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketRespawn) {
            dimension = ((SPacketRespawn) packet).getDimensionID();
            updater.queue.add(new WorldData.UnloadWorld());
        } else if (packet instanceof SPacketJoinGame) {
            dimension = ((SPacketJoinGame) packet).getDimension();
            updater.queue.add(new WorldData.UnloadWorld());
        } else if (packet instanceof SPacketChunkData) {
            SPacketChunkData chunkData = (SPacketChunkData) packet;
            updater.queue.add(new WorldData.ChunkData(chunkData.getChunkX(), chunkData.getChunkZ(), chunkData.getExtractedSize(), chunkData.isFullChunk(), chunkData.getReadBuffer()));
        } else if (packet instanceof SPacketBlockChange) {
            SPacketBlockChange packetBlockChange = (SPacketBlockChange) packet;
            updater.queue.add(new WorldData.BlockData(packetBlockChange.getBlockPosition().toImmutable(), packetBlockChange.getBlockState()));
        } else if (packet instanceof SPacketMultiBlockChange) {
            SPacketMultiBlockChange packetMultiBlockChange = (SPacketMultiBlockChange) packet;
            updater.queue.add(new WorldData.MultiBlockData(((ISPacketMultiBlockChange) packetMultiBlockChange).getChunkPos(), packetMultiBlockChange.getChangedBlocks()));
        } else if (packet instanceof SPacketUnloadChunk) {
            SPacketUnloadChunk packetUnloadChunk = (SPacketUnloadChunk) packet;
            updater.queue.add(new WorldData.RemoveData(packetUnloadChunk.getX(), packetUnloadChunk.getZ()));
        }
    });

    private static class WorldData {
        public static class ChunkData extends WorldData {
            public final int chunkX;
            public final int chunkZ;
            public final int sections;
            public final boolean fullChunk;

            public final PacketBuffer data;

            public ChunkData(int chunkX, int chunkZ, int sections, boolean fullChunk, PacketBuffer data) {
                this.chunkX = chunkX;
                this.chunkZ = chunkZ;
                this.sections = sections;
                this.fullChunk = fullChunk;

                this.data = data;
            }
        }

        public static class BlockData extends WorldData {
            public final BlockPos pos;
            public final IBlockState blockState;

            public BlockData(BlockPos pos, IBlockState blockState) {
                this.pos = pos;
                this.blockState = blockState;
            }
        }

        public static class MultiBlockData extends WorldData {
            public final ChunkPos pos;
            public final SPacketMultiBlockChange.BlockUpdateData[] data;

            public MultiBlockData(ChunkPos pos, SPacketMultiBlockChange.BlockUpdateData[] data) {
                this.pos = pos;
                this.data = data;
            }
        }

        public static class RemoveData extends WorldData {
            public final int x;
            public final int z;

            public RemoveData(int x, int z) {
                this.x = x;
                this.z = z;
            }
        }

        public static class UnloadWorld extends WorldData {
            public UnloadWorld() {
            }
        }
     }

    private static class WorldUpdater extends Thread {
        private final ConcurrentLinkedQueue<WorldData> queue = new ConcurrentLinkedQueue<>();
        private final WorldCopyManager parent;

        public WorldUpdater(WorldCopyManager parent) {
            this.parent = parent;
            this.setDaemon(true);
            this.start();
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                if (!(parent.chunkProvider == null || queue.isEmpty())) {
                    WorldData data = queue.poll();
                    parent.lock.writeLock().lock();
                    try {
                        if (data instanceof WorldData.ChunkData) {
                            handleChunkData((WorldData.ChunkData) data);
                        } else if (data instanceof WorldData.BlockData) {
                            handleBlockData((WorldData.BlockData) data);
                        } else if (data instanceof WorldData.MultiBlockData) {
                            handleMultiBlockData((WorldData.MultiBlockData) data);
                        } else if (data instanceof WorldData.RemoveData) {
                            handleRemoveChunkData((WorldData.RemoveData) data);
                        } else if (data instanceof WorldData.UnloadWorld) {
                            handleUnloadWorld();
                        }
                    } finally {
                        parent.lock.writeLock().unlock();
                    }
                } else {
                    Thread.yield();
                }
            }
        }

        private void handleChunkData(WorldData.ChunkData data) {
            LiteChunk chunk;
            if (!parent.chunkProvider.isChunkGeneratedAt(data.chunkX, data.chunkZ)) {
                chunk = parent.chunkProvider.loadChunk(data.chunkX, data.chunkZ);
            } else {
                chunk = parent.chunkProvider.getLoadedChunk(data.chunkX, data.chunkZ);
            }

            if (chunk != null) {
                chunk.read(data.data, data.sections, data.fullChunk, parent.dimension);
            }
        }

        private void handleBlockData(WorldData.BlockData data) {
            parent.chunkProvider.provideChunk(data.pos.getX() >> 4, data.pos.getZ() >> 4).setBlockState(data.pos, data.blockState);
        }

        private void handleMultiBlockData(WorldData.MultiBlockData data) {
            LiteChunk chunk = parent.chunkProvider.provideChunk(data.pos.x, data.pos.z);
            for (SPacketMultiBlockChange.BlockUpdateData blockUpdateData : data.data) {
                chunk.setBlockState(blockUpdateData.getPos(), blockUpdateData.getBlockState());
            }
        }

        private void handleRemoveChunkData(WorldData.RemoveData data) {
            parent.chunkProvider.unloadChunk(data.x, data.z);
        }

        private void handleUnloadWorld() {
            parent.chunkProvider = new LiteChunkProvider();
        }
    }
}
