package com.gamesense.api.util.world.combatRewrite.ac.entityData.mobs;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityLivingInfo;
import com.gamesense.mixin.mixins.accessor.IEntityShulker;
import com.google.common.base.Optional;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ShulkerInfo extends EntityLivingInfo {

    private static final DataParameter<EnumFacing> ATTACHED_FACE = IEntityShulker.getATTACHED_FACE();
    private static final DataParameter<Optional<BlockPos>> ATTACHED_BLOCK_POS = IEntityShulker.getATTACHED_BLOCK_POS();
    private static final DataParameter<Byte> PEEK_TICK = IEntityShulker.getPEEK_TICK();
    
    public ShulkerInfo(SPacketSpawnMob mob) {
        super(mob.getEntityID());

        this.dataManager.setEntryValues(mob.getDataManagerEntries());

        this.width = 1.0D;
        this.height = 1.0D;

        this.updatePosition(mob.getX(), mob.getY(), mob.getZ());
    }
    
    protected void setupDataManager() {
        super.setupDataManager();

        this.dataManager.setEntry(ATTACHED_FACE, EnumFacing.DOWN);
        this.dataManager.setEntry(ATTACHED_BLOCK_POS, Optional.absent());
        this.dataManager.setEntry(PEEK_TICK, (byte)0);
    }

    @Override
    public void updateSize() {
        BlockPos blockpos = (this.dataManager.getEntryData(ATTACHED_BLOCK_POS)).orNull();
        if (blockpos != null) {
            position = new Vec3d(blockpos.getX() + 0.5D, blockpos.getY(), blockpos.getZ() + 0.5D);
            double peekAmount = this.getPeekTick() * 0.01F;
            peekAmount = Math.min(Math.max(peekAmount, 0.0D), 1.0D);
            double offset = 0.5D - (double)MathHelper.sin((0.5F + (float) peekAmount) * (float)Math.PI) * 0.5D;
            EnumFacing enumfacing2 = this.getAttachmentFacing();

            switch (enumfacing2) {
                case DOWN:
                    this.aabb = new AxisAlignedBB(position.x - 0.5D, position.y, position.z - 0.5D, position.x + 0.5D, position.y + 1.0D + offset, position.z + 0.5D);
                    break;
                case UP:
                    this.aabb = new AxisAlignedBB(position.x - 0.5D, position.y - offset, position.z - 0.5D, position.x + 0.5D, position.y + 1.0D, position.z + 0.5D);
                    break;
                case NORTH:
                    this.aabb = new AxisAlignedBB(position.x - 0.5D, position.y, position.z - 0.5D, position.x + 0.5D, position.y + 1.0D, position.z + 0.5D + offset);
                    break;
                case SOUTH:
                    this.aabb = new AxisAlignedBB(position.x - 0.5D, position.y, position.z - 0.5D - offset, position.x + 0.5D, position.y + 1.0D, position.z + 0.5D);
                    break;
                case WEST:
                    this.aabb = new AxisAlignedBB(position.x - 0.5D, position.y, position.z - 0.5D, position.x + 0.5D + offset, position.y + 1.0D, position.z + 0.5D);
                    break;
                case EAST:
                    this.aabb = new AxisAlignedBB(position.x - 0.5D - offset, position.y, position.z - 0.5D, position.x + 0.5D, position.y + 1.0D, position.z + 0.5D);
            }
        }
    }

    public int getPeekTick() {
        return this.dataManager.getEntryData(PEEK_TICK);
    }

    public EnumFacing getAttachmentFacing() {
        return this.dataManager.getEntryData(ATTACHED_FACE);
    }
}
