package com.gamesense.api.util.world.combatRewrite.ac.entityData.mobs;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityLivingInfo;
import net.minecraft.network.play.server.SPacketSpawnMob;

public class SmallThingInfo extends EntityLivingInfo {
    public SmallThingInfo(SPacketSpawnMob mob) {
        super(mob.getEntityID());

        this.dataManager.setEntryValues(mob.getDataManagerEntries());

        this.width = 0.4D;
        this.height = 0.3D;

        this.updatePosition(mob.getX(), mob.getY(), mob.getZ());
    }

    public double getYOffset() {
        return 0.1D;
    }
}
