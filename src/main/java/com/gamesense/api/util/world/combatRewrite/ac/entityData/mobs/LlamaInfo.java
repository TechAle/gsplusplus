package com.gamesense.api.util.world.combatRewrite.ac.entityData.mobs;

import net.minecraft.network.play.server.SPacketSpawnMob;

public class LlamaInfo extends AbstractChestHorseInfo {

    public LlamaInfo(SPacketSpawnMob mob) {
        super(mob.getEntityID());

        this.dataManager.setEntryValues(mob.getDataManagerEntries());

        this.width = 0.9D;
        this.height = 1.87D;

        this.updatePosition(mob.getX(), mob.getY(), mob.getZ());
    }

    public double getMountedYOffset() {
        return this.height * 0.67D;
    }
}
