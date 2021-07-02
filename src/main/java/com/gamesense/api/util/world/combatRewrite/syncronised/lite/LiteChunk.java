package com.gamesense.api.util.world.combatRewrite.syncronised.lite;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Arrays;

public class LiteChunk {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final LiteExtendedBlockStorage NULL_BLOCK_STORAGE = null;

    private final byte[] buffer = new byte[2048];
    private final LiteExtendedBlockStorage[] storageArrays;
    private final byte[] blockBiomeArray;

    private boolean loaded;
    public final int x;
    public final int z;
    private boolean isTerrainPopulated;

    public LiteChunk(int x, int z) {
        this.storageArrays = new LiteExtendedBlockStorage[16];
        this.blockBiomeArray = new byte[256];
        this.x = x;
        this.z = z;

        Arrays.fill(this.blockBiomeArray, (byte) - 1);
    }

    public boolean isAtLocation(int x, int z) {
        return x == this.x && z == this.z;
    }

    @Nullable
    public LiteExtendedBlockStorage getLastExtendedBlockStorage() {
        for (int i = this.storageArrays.length - 1; i >= 0; --i) {
            if (this.storageArrays[i] != NULL_BLOCK_STORAGE) {
                return this.storageArrays[i];
            }
        }

        return null;
    }

    public int getTopFilledSegment() {
        LiteExtendedBlockStorage extendedBlockStorage = this.getLastExtendedBlockStorage();
        return extendedBlockStorage == null ? 0 : extendedBlockStorage.getYLocation();
    }

    public LiteExtendedBlockStorage[] getBlockStorageArray() {
        return this.storageArrays;
    }

    public IBlockState getBlockState(BlockPos pos) {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    public IBlockState getBlockState(final int x, final int y, final int z) {
        try {
            if (y >= 0 && y >> 4 < this.storageArrays.length) {
                LiteExtendedBlockStorage extendedblockstorage = this.storageArrays[y >> 4];

                if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                    return extendedblockstorage.get(x & 15, y & 15, z & 15);
                }
            }

            return Blocks.AIR.getDefaultState();
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting block state");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being got");
            crashreportcategory.addDetail("Location", () -> CrashReportCategory.getCoordinateInfo(x, y, z));
            throw new ReportedException(crashreport);
        }
    }

    @Nullable
    public IBlockState setBlockState(BlockPos pos, IBlockState state) {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;

        IBlockState iblockstate = this.getBlockState(pos);

        if (iblockstate == state) {
            return null;
        } else {
            Block block = state.getBlock();
            // Relocate old light value lookup here, so that it is called before TE is removed.
            LiteExtendedBlockStorage extendedBlockStorage = this.storageArrays[j >> 4];

            if (extendedBlockStorage == NULL_BLOCK_STORAGE) {
                if (block == Blocks.AIR) {
                    return null;
                }

                extendedBlockStorage = new LiteExtendedBlockStorage(j >> 4 << 4);
                this.storageArrays[j >> 4] = extendedBlockStorage;
            }

            extendedBlockStorage.set(i, j & 15, k, state);

            if (extendedBlockStorage.get(i, j & 15, k).getBlock() != block) {
                return null;
            } else {
                return iblockstate;
            }
        }
    }

    public void onLoad() {
        this.loaded = true;
    }

    public void onUnload() {
        this.loaded = false;
    }

    public boolean isEmpty() {
        return false;
    }

    public ChunkPos getPos() {
        return new ChunkPos(this.x, this.z);
    }

    public boolean isEmptyBetween(int startY, int endY) {
        if (startY < 0) {
            startY = 0;
        }

        if (endY >= 256) {
            endY = 255;
        }

        for (int i = startY; i <= endY; i += 16) {
            LiteExtendedBlockStorage extendedBlockStorage = this.storageArrays[i >> 4];

            if (extendedBlockStorage != NULL_BLOCK_STORAGE && !extendedBlockStorage.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public void setStorageArrays(LiteExtendedBlockStorage[] newStorageArrays) {
        if (this.storageArrays.length != newStorageArrays.length) {
            LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", newStorageArrays.length, this.storageArrays.length);
        } else {
            System.arraycopy(newStorageArrays, 0, this.storageArrays, 0, this.storageArrays.length);
        }
    }

    public void read(PacketBuffer buf, int availableSections, boolean groundUpContinuous, int dimension) {
        for (int i = 0; i < this.storageArrays.length; ++i) {
            LiteExtendedBlockStorage extendedBlockStorage = this.storageArrays[i];

            if ((availableSections & 1 << i) == 0) {
                if (groundUpContinuous && extendedBlockStorage != NULL_BLOCK_STORAGE) {
                    this.storageArrays[i] = NULL_BLOCK_STORAGE;
                }
            } else {
                if (extendedBlockStorage == NULL_BLOCK_STORAGE) {
                    extendedBlockStorage = new LiteExtendedBlockStorage(i << 4);
                    this.storageArrays[i] = extendedBlockStorage;
                }

                extendedBlockStorage.getData().read(buf);
                buf.readBytes(buffer);

                if (dimension == 0) {
                    buf.readBytes(buffer);
                }
            }
        }

        if (groundUpContinuous) {
            buf.readBytes(this.blockBiomeArray);
        }

        for (int j = 0; j < this.storageArrays.length; ++j) {
            if (this.storageArrays[j] != NULL_BLOCK_STORAGE && (availableSections & 1 << j) != 0) {
                this.storageArrays[j].recalculateRefCounts();
            }
        }

        this.isTerrainPopulated = true;
    }

    public Biome getBiome(BlockPos pos, BiomeProvider provider)
    {
        int i = pos.getX() & 15;
        int j = pos.getZ() & 15;
        int k = this.blockBiomeArray[j << 4 | i] & 255;

        if (k == 255) {
            k = Biome.getIdForBiome(provider.getBiome(pos, Biomes.PLAINS));
            this.blockBiomeArray[j << 4 | i] = (byte)(k & 255);
        }

        Biome biome = Biome.getBiome(k);
        return biome == null ? Biomes.PLAINS : biome;
    }

    public byte[] getBiomeArray() {
        return this.blockBiomeArray;
    }

    public void setBiomeArray(byte[] biomeArray) {
        if (this.blockBiomeArray.length != biomeArray.length) {
            LOGGER.warn("Could not set level chunk biomes, array length is {} instead of {}", biomeArray.length, this.blockBiomeArray.length);
        } else {
            System.arraycopy(biomeArray, 0, this.blockBiomeArray, 0, this.blockBiomeArray.length);
        }
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void markLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public boolean isTerrainPopulated() {
        return this.isTerrainPopulated;
    }

    public void setTerrainPopulated(boolean terrainPopulated) {
        this.isTerrainPopulated = terrainPopulated;
    }
}