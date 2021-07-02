package com.gamesense.api.util.world.combatRewrite.ac.entityData.objects;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityInfo;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class HangingInfo extends EntityInfo {

    protected final BlockPos hangingPosition;
    protected final EnumFacing facingDirection;

    public HangingInfo(SPacketSpawnObject entity) {
        this(entity.getEntityID(), new BlockPos(entity.getX(), entity.getY(), entity.getZ()), EnumFacing.byHorizontalIndex(entity.getData()));
    }

    public HangingInfo(int entityID, BlockPos hangingPosition, EnumFacing facingDirection) {
        super(entityID);

        this.hangingPosition = hangingPosition;
        this.facingDirection = facingDirection;

        this.width = 0.5D;
        this.height = 0.5D;

        // called in child constructors for compatibility with EntityPainting
        // this.updateSize();
    }

    public void updateSize() {
        if (hangingPosition == null || facingDirection == null) {
            super.updateSize();
        } else {
            updateBoundingBox();
        }
    }

    protected void updateBoundingBox() {
        double xPos = (double)this.hangingPosition.getX() + 0.5D;
        double yPos = (double)this.hangingPosition.getY() + 0.5D;
        double zPos = (double)this.hangingPosition.getZ() + 0.5D;
        double widthOffset = this.offs(this.getWidthPixels());
        double heightOffset = this.offs(this.getHeightPixels());
        xPos = xPos - (double)this.facingDirection.getXOffset() * 0.46875D;
        zPos = zPos - (double)this.facingDirection.getZOffset() * 0.46875D;
        yPos = yPos + heightOffset;
        EnumFacing enumfacing = this.facingDirection.rotateYCCW();
        xPos = xPos + widthOffset * (double)enumfacing.getXOffset();
        zPos = zPos + widthOffset * (double)enumfacing.getZOffset();

        this.position = new Vec3d(xPos, yPos, zPos);

        double x = this.getWidthPixels();
        double y = this.getHeightPixels();
        double z = this.getWidthPixels();

        if (this.facingDirection.getAxis() == EnumFacing.Axis.Z) {
            z = 1.0D;
        } else {
            x = 1.0D;
        }

        x = x / 32.0D;
        y = y / 32.0D;
        z = z / 32.0D;
        this.aabb = new AxisAlignedBB(xPos - x, yPos - y, zPos - z, xPos + x, yPos + y, zPos + z);
    }

    // not sure what it does but needed to get it to work
    private double offs(int integer) {
        return integer % 32 == 0 ? 0.5D : 0.0D;
    }

    public abstract int getWidthPixels();

    public abstract int getHeightPixels();
}
