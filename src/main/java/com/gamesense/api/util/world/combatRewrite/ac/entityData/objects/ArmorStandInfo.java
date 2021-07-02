package com.gamesense.api.util.world.combatRewrite.ac.entityData.objects;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityLivingInfo;
import net.minecraft.network.play.server.SPacketSpawnObject;

import static net.minecraft.entity.item.EntityArmorStand.STATUS;

public class ArmorStandInfo extends EntityLivingInfo {

    public ArmorStandInfo(SPacketSpawnObject mob) {
        super(mob.getEntityID());

        this.width = 0.5D;
        this.height = 1.975F;

        this.updatePosition(mob.getX(), mob.getY(), mob.getZ());
    }

    protected void setupDataManager() {
        super.setupDataManager();

        this.dataManager.setEntry(STATUS, (byte) 0);
    }

    @Override
    public void updateSize() {
        double tempWidth = this.width;
        double tempHeight = this.height;
        float f = this.hasMarker() ? 0.0F : (this.isSmall() ? 0.5F : 1.0F);
        this.width = tempWidth * f;
        this.height = tempHeight * f;

        super.updateSize();

        this.width = tempWidth;
        this.height = tempHeight;
    }

    public boolean isSmall() {
        return (this.dataManager.getEntryData(STATUS) & 1) != 0;
    }

    public boolean hasMarker() {
        return (this.dataManager.getEntryData(STATUS) & 16) != 0;
    }
}
