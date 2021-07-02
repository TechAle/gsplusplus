package com.gamesense.api.util.world.combatRewrite.ac.entityData;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.entity.Entity.FLAGS;

public class EntityInfo {
    public final int entityID;

    public final List<EntityInfo> riddenByEntities = Collections.synchronizedList(new ArrayList<>());

    public volatile long serverX = 0;
    public volatile long serverY = 0;
    public volatile long serverZ = 0;
    public volatile Vec3d position = Vec3d.ZERO;
    public volatile Vec3d motion = Vec3d.ZERO;

    public volatile double width = 0.6D;
    public volatile double height = 1.8D;
    public volatile AxisAlignedBB aabb = Entity.ZERO_AABB;

    public final liteDataManager dataManager = new liteDataManager();
    public boolean isCrystal;

    public EntityInfo(int entityID) {
        this.entityID = entityID;

        this.setupDataManager();
    }

    public EntityInfo(SPacketSpawnObject entity) {
        this(entity.getEntityID());

        switch(entity.getType()) {
            case 2:
            case 61:
            case 62:
            case 65:
            case 68:
            case 72:
            case 73:
            case 75:
            case 76:
            case 90:
                this.width = 0.25D;
                this.height = 0.25D;
                break;
            case 64:
            case 66:
            case 67:
                this.width = 0.3125D;
                this.height = 0.3125D;
                break;
            case 60:
            case 91:
                this.width = 0.5D;
                this.height = 0.5D;
                break;
            case 79:
                this.width = 0.5D;
                this.height = 0.8D;
                break;
            case 50:
                this.width = 0.98D;
                this.height = 0.98D;
                break;
            case 63:
            case 93:
                this.width = 1.0D;
                this.height = 1.0D;
                break;
            case 51:
                this.width = 2.0D;
                this.height = 2.0D;
                isCrystal = true;
        }

        this.updatePosition(entity.getX(), entity.getY(), entity.getZ());
    }

    protected void setupDataManager() {
        dataManager.setEntry(FLAGS, (byte) 0);
    }

    public void onRemove() {
        this.removeAllPassengers();
    }

    /*
     * isBurning: 0
     * isSneaking: 1
     * isSprinting: 3
     * isInvisible: 5
     * isGlowing: 6
     * isElytraFlying: 7
     */
    public boolean getFlag(int flag) {
        return (this.dataManager.getEntryData(FLAGS) & 1 << flag) != 0;
    }

    // TODO
    public void updateMotion(double motionX, double motionY, double motionZ) {
        this.motion = new Vec3d(motionX, motionY, motionZ);
    }

    public void updateServerPosition(long x, long y, long z) {
        x += this.serverX;
        y += this.serverY;
        z += this.serverZ;
        this.serverX = x;
        this.serverY = y;
        this.serverZ = z;
        this.position = new Vec3d((double)x / 4096.0D, (double)y / 4096.0D, (double)z / 4096.0D);
        this.updateSize();
        this.updatePassengers();
    }

    public void updatePosition(double posX, double posY, double posZ) {
        this.setServerPosition(posX, posY, posZ);
        this.position = new Vec3d(posX, posY, posZ);
        this.updateSize();
        this.updatePassengers();
    }

    public void updateSize() {
        double height = this.height;
        double halfWidth = this.width / 2.0D;
        this.aabb = new AxisAlignedBB(position.x - halfWidth, position.y, position.z - halfWidth, position.x + halfWidth, position.y + height, position.z + halfWidth);
    }

    public void setServerPosition(double x, double y, double z) {
        this.serverX = EntityTracker.getPositionLong(x);
        this.serverY = EntityTracker.getPositionLong(y);
        this.serverZ = EntityTracker.getPositionLong(z);
    }

    public void removeAllPassengers() {
        riddenByEntities.forEach(EntityInfo::removeAllPassengers);
        riddenByEntities.clear();
    }

    public void addPassenger(EntityInfo passenger) {
        if (passenger != null) {
            this.riddenByEntities.add(passenger);
        }
    }

    public List<EntityInfo> getPassengers() {
        return this.riddenByEntities.isEmpty() ? Collections.emptyList() : Lists.newArrayList(this.riddenByEntities);
    }

    public void updatePassengers() {
        this.riddenByEntities.forEach(entity -> entity.updatePosition(position.x, position.y + this.getMountedYOffset() + entity.getYOffset(), position.z));
    }

    public double getYOffset() {
        return 0.0D;
    }

    public double getMountedYOffset() {
        return this.height * 0.75D;
    }

    public static class liteDataManager {
        private final ConcurrentHashMap<Integer, DataEntry<?>> entries = new ConcurrentHashMap<>();

        public <T> void setEntry(DataParameter<T> key, T value) {
            DataEntry<T> dataEntry = new DataEntry<>(key, value);
            this.entries.put(key.getId(), dataEntry);
        }

        public void setEntryValues(List< EntityDataManager.DataEntry<? >> entries) {
            if (entries == null) {
                return;
            }
            for (EntityDataManager.DataEntry<?> dataEntry : entries) {
                this.setEntryData(dataEntry);
            }
        }

        public <T> void setEntryData(EntityDataManager.DataEntry<T> newValue) {
            int id = newValue.getKey().getId();
            DataEntry<T> entry = (DataEntry<T>) this.entries.get(id);
            if (entry != null) {
                entry.setValue(newValue.getValue());
            }
        }

        public <T> T getEntryData(DataParameter<T> key) {
            return (T)this.entries.get(key.getId()).getValue();
        }
    }

    public static class DataEntry<T> {
        private final DataParameter<T> key;
        private volatile T value;

        public DataEntry(DataParameter<T> keyIn, T valueIn) {
            this.key = keyIn;
            this.value = valueIn;
        }

        public void setValue(T valueIn) {
            this.value = valueIn;
        }

        public T getValue() {
            return this.value;
        }
    }
}
