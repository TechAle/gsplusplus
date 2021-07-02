package com.gamesense.api.util.world.combatRewrite.ac.entityData.objects;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityInfo;
import com.gamesense.mixin.mixins.accessor.IEntityAreaEffectCloud;
import net.minecraft.network.play.server.SPacketSpawnObject;

public class AreaEffectCloudInfo extends EntityInfo {

    public AreaEffectCloudInfo(SPacketSpawnObject entity) {
        super(entity.getEntityID());

        this.width = 0.5f;
        this.height = 0.5f;

        this.updatePosition(entity.getX(), entity.getY(), entity.getZ());
    }

    @Override
    protected void setupDataManager() {
        super.setupDataManager();

        this.dataManager.setEntry(IEntityAreaEffectCloud.getRADIUS(), 0.5F);
    }

    public void updateSize() {
        this.width = this.dataManager.getEntryData(IEntityAreaEffectCloud.getRADIUS()) * 2.0f;

        super.updateSize();
    }
}
