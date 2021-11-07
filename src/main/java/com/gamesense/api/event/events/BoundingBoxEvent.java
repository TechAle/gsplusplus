package com.gamesense.api.event.events;

import com.gamesense.api.event.GameSenseEvent;
import net.minecraft.block.Block;
import net.minecraft.util.math.AxisAlignedBB;

public class BoundingBoxEvent extends GameSenseEvent {

    public BoundingBoxEvent(Block block) {

        super();
        this.block = block;

    }

    Block block;
    AxisAlignedBB bb;
    public boolean changed;

    public void setbb(AxisAlignedBB BoundingBox) {
        this.bb = BoundingBox;
        changed = true;
    }
    
    public Block getBlock() {
        return block;
    }
    
    public AxisAlignedBB getbb() {
        return this.bb;
    }
    
}
