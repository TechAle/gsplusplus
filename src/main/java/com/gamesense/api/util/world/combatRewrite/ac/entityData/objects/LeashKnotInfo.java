package com.gamesense.api.util.world.combatRewrite.ac.entityData.objects;

import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class LeashKnotInfo extends HangingInfo {

    public LeashKnotInfo(SPacketSpawnObject entity) {
        super(entity);

        this.position = new Vec3d(entity.getX() + 0.5D, entity.getY() + 0.5D, entity.getZ() + 0.5D);
        this.aabb = new AxisAlignedBB(position.x - 0.1875D, position.y - 0.25D + 0.125D, position.z - 0.1875D, position.x + 0.1875D, position.y + 0.25D + 0.125D, position.z + 0.1875D);
    }

    protected void updateBoundingBox() {
    }

    public int getWidthPixels() {
        return 9;
    }

    public int getHeightPixels() {
        return 9;
    }
}
