package com.gamesense.api.event.events;

import com.gamesense.api.event.GameSenseEvent;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @see com.gamesense.mixin.mixins.MixinEntity
 * */

public class StepEvent extends GameSenseEvent {

    EntityPlayer player;

    public StepEvent(EntityPlayer player) {

        super();
        this.player = player;

    }

}
