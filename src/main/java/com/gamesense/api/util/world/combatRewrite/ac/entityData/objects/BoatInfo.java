package com.gamesense.api.util.world.combatRewrite.ac.entityData.objects;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityInfo;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.mobs.EntityAnimalInfo;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.atomic.AtomicInteger;

public class BoatInfo extends EntityInfo {

    public BoatInfo(SPacketSpawnObject entity) {
        super(entity.getEntityID());

        this.width = 1.375D;
        this.height = 0.5625D;

        this.updatePosition(entity.getX(), entity.getY(), entity.getZ());
    }

    // TODO yaw fix
    public void updatePassengers() {
        AtomicInteger i = new AtomicInteger(0);
        this.getPassengers().forEach(passenger -> {
            double positionOffset = 0.0D;
            double f1 = this.getMountedYOffset() + passenger.getYOffset();

            if (i.getAndIncrement() == 0) {
                positionOffset = 0.2D;
            } else {
                positionOffset = -0.6D;
            }

            if (passenger instanceof EntityAnimalInfo) {
                positionOffset = (positionOffset + 0.2D);
            }

            Vec3d vec3d = (new Vec3d(positionOffset, 0.0D, 0.0D));
            passenger.updatePosition(position.x + vec3d.x, position.y + f1, position.z + vec3d.z);
        });
    }

    public double getMountedYOffset() {
        return -0.1D;
    }
}
