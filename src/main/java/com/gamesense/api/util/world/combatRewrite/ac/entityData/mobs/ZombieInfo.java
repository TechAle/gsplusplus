package com.gamesense.api.util.world.combatRewrite.ac.entityData.mobs;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityLivingInfo;
import com.gamesense.mixin.mixins.accessor.IEntityZombie;
import net.minecraft.network.play.server.SPacketSpawnMob;

public class ZombieInfo extends EntityLivingInfo {

    public ZombieInfo(SPacketSpawnMob mob) {
        super(mob.getEntityID());

        this.dataManager.setEntryValues(mob.getDataManagerEntries());

        this.width = 0.6D;
        this.height = 1.95D;

        this.updatePosition(mob.getX(), mob.getY(), mob.getZ());
    }

    protected void setupDataManager() {
        super.setupDataManager();

        this.dataManager.setEntry(IEntityZombie.getIS_CHILD(), Boolean.FALSE);
    }

    @Override
    public void updateSize() {
        double tempWidth = this.width;
        double tempHeight = this.height;
        double f = this.isChild() ? 0.5D : 1.0D;
        this.width = tempWidth * f;
        this.height = tempHeight * f;

        super.updateSize();

        this.width = tempWidth;
        this.height = tempHeight;
    }

    public double getYOffset() {
        return this.isChild() ? 0.0D : -0.45D;
    }

    public boolean isChild() {
        return this.dataManager.getEntryData(IEntityZombie.getIS_CHILD());
    }
}
