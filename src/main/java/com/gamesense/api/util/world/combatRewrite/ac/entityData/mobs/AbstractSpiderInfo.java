package com.gamesense.api.util.world.combatRewrite.ac.entityData.mobs;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityLivingInfo;
import net.minecraft.network.play.server.SPacketSpawnMob;

public class AbstractSpiderInfo extends EntityLivingInfo {

    public AbstractSpiderInfo(SPacketSpawnMob mob) {
        super(mob.getEntityID());

        this.dataManager.setEntryValues(mob.getDataManagerEntries());

        switch (mob.getEntityType()) {
            case 59:
                this.width = 0.7D;
                this.height = 0.5D;
                break;
            default:
                this.width = 1.4D;
                this.height = 0.9D;
        }

        this.updatePosition(mob.getX(), mob.getY(), mob.getZ());
    }

    public double getMountedYOffset() {
        return this.height * 0.5F;
    }
}
