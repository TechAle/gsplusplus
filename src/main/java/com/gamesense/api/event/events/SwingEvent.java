package com.gamesense.api.event.events;

import com.gamesense.api.event.GameSenseEvent;
import net.minecraft.util.EnumHand;

public class SwingEvent extends GameSenseEvent {

    EnumHand hand;

    public SwingEvent(EnumHand enumHand) {

        this.hand = enumHand;

    }

    public void setHand(EnumHand hand) {
        this.hand = hand;
    }

    public EnumHand getHand() {
        return this.hand;
    }

}
