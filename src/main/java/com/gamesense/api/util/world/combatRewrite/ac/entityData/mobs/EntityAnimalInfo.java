package com.gamesense.api.util.world.combatRewrite.ac.entityData.mobs;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityLivingInfo;
import com.gamesense.mixin.mixins.accessor.IEntityAgeable;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.util.math.AxisAlignedBB;

public class EntityAnimalInfo extends EntityLivingInfo {
    // this key is private
    protected static final DataParameter<Boolean> BABY = IEntityAgeable.getBABY();

    public EntityAnimalInfo(int entityID) {
        super(entityID);
    }

    public EntityAnimalInfo(SPacketSpawnMob mob) {
        super(mob.getEntityID());

        this.dataManager.setEntryValues(mob.getDataManagerEntries());

        switch (mob.getEntityType()) {
            case 101:
                this.width = 0.4D;
                this.height = 0.5D;
                break;
            case 93:
                this.width = 0.4D;
                this.height = 0.7D;
                break;
            case 105:
                this.width = 0.5D;
                this.height = 0.9D;
                break;
            case 98:
                this.width = 0.6D;
                this.height = 0.7D;
                break;
            case 95:
                this.width = 0.6D;
                this.height = 0.85D;
                break;
            case 120:
                this.width = 0.6D;
                this.height = 1.95D;
                break;
            case 94:
                this.width = 0.8D;
                this.height = 0.8D;
                break;
            case 90:
                this.width = 0.9D;
                this.height = 0.9D;
                break;
            case 91:
                this.width = 0.9D;
                this.height = 1.3D;
                break;
            case 92:
            case 96:
                this.width = 0.9D;
                this.height = 1.4D;
                break;
            case 102:
                this.width = 1.3D;
                this.height = 1.4D;
                break;
            case 100:
                this.width = 1.3964844D;
                this.height = 1.6D;
                break;
        }

        this.updatePosition(mob.getX(), mob.getY(), mob.getZ());
    }

    protected void setupDataManager() {
        super.setupDataManager();

        this.dataManager.setEntry(BABY, Boolean.FALSE);
    }

    public void updateSize() {
        boolean isChild = this.dataManager.getEntryData(BABY);
        double height = this.height / (isChild ? 2.0D : 1.0D);
        double halfWidth = this.width / (isChild ? 4.0D : 2.0D);
        this.aabb = new AxisAlignedBB(position.x - halfWidth, position.y, position.z - halfWidth, position.x + halfWidth, position.y + height, position.z + halfWidth);
    }

    public double getYOffset() {
        return 0.14D;
    }
}
