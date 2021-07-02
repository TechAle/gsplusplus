package com.gamesense.api.util.world.combatRewrite.ac.entityData.mobs;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityLivingInfo;
import com.gamesense.mixin.mixins.accessor.IEntitySlime;
import net.minecraft.network.play.server.SPacketSpawnMob;

public class SlimeInfo extends EntityLivingInfo {

    public SlimeInfo(SPacketSpawnMob mob) {
        super(mob.getEntityID());

        this.dataManager.setEntryValues(mob.getDataManagerEntries());

        this.width = 0.51000005F;
        this.height = 0.51000005F;

        this.updatePosition(mob.getX(), mob.getY(), mob.getZ());
    }

    protected void setupDataManager() {
        super.setupDataManager();

        this.dataManager.setEntry(IEntitySlime.getSLIME_SIZE(), 1);
    }

    @Override
    public void updateSize() {
        double tempWidth = this.width;
        double tempHeight = this.height;
        double f = getSlimeSize();
        this.width = tempWidth * f;
        this.height = tempHeight * f;

        super.updateSize();

        this.width = tempWidth;
        this.height = tempHeight;
    }

    public int getSlimeSize() {
        return this.dataManager.getEntryData(IEntitySlime.getSLIME_SIZE());
    }
}
