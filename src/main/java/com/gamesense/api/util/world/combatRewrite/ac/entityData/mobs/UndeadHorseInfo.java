package com.gamesense.api.util.world.combatRewrite.ac.entityData.mobs;

import net.minecraft.network.play.server.SPacketSpawnMob;

public class UndeadHorseInfo extends EntityAnimalInfo {

    public UndeadHorseInfo(SPacketSpawnMob mob) {
        super(mob.getEntityID());

        this.dataManager.setEntryValues(mob.getDataManagerEntries());

        this.width = 1.3964844D;
        this.height = 1.6D;

        this.updatePosition(mob.getX(), mob.getY(), mob.getZ());
    }

    public double getMountedYOffset() {
        return super.getMountedYOffset() - 0.1875D;
    }
}
