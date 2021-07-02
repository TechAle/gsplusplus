package com.gamesense.api.util.world.combatRewrite.ac.entityData.objects;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityInfo;
import net.minecraft.network.play.server.SPacketSpawnObject;

public class MinecartInfo extends EntityInfo {

    public MinecartInfo(SPacketSpawnObject entity) {
        super(entity.getEntityID());

        this.width = 0.98D;
        this.height = 0.7D;

        this.updatePosition(entity.getX(), entity.getY(), entity.getZ());
    }

    public double getMountedYOffset() {
        return 0.0D;
    }
}
