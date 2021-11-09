package com.gamesense.api.event.events;

import com.gamesense.api.event.GameSenseEvent;
import net.minecraft.block.Block;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BoundingBoxEvent extends GameSenseEvent {

    public BoundingBoxEvent(Block block, Vec3d pos) {

        super();
        this.block = block;
        this.pos = pos;

    }

    Block block;
    AxisAlignedBB bb;
    Vec3d pos;
    public boolean changed;

    public void setbb(AxisAlignedBB BoundingBox) {
        this.bb = BoundingBox;
        changed = true;
    }
    
    public Block getBlock() {
        return block;
    }

    public Vec3d getPos() {return pos;}

    public AxisAlignedBB getbb() {
        return bb;
    }
    
}
