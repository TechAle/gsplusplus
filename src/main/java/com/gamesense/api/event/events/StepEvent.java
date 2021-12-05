package com.gamesense.api.event.events;

import com.gamesense.api.event.GameSenseEvent;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * @see com.gamesense.mixin.mixins.MixinEntity
 * */

public class StepEvent extends GameSenseEvent {

    AxisAlignedBB BB;

    public StepEvent(AxisAlignedBB bb) {
        super();
        this.BB = bb;
    }

    public AxisAlignedBB getBB() {
        return BB;
    }
}
