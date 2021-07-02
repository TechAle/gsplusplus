package com.gamesense.api.util.world.combatRewrite.ac.entityData.mobs;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityLivingInfo;
import net.minecraft.network.play.server.SPacketSpawnMob;

public class AbstractSkeletonInfo extends EntityLivingInfo {

    public AbstractSkeletonInfo(SPacketSpawnMob mob) {
        super(mob.getEntityID());

        this.dataManager.setEntryValues(mob.getDataManagerEntries());

        switch (mob.getEntityType()) {
            case 5:
                this.width = 0.7D;
                this.height = 2.4D;
                break;
            default:
                this.width = 0.6D;
                this.height = 1.99D;
        }

        this.updatePosition(mob.getX(), mob.getY(), mob.getZ());
    }

    public double getYOffset() {
        return -0.6D;
    }
}
