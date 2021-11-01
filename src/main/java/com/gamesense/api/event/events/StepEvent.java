package com.gamesense.api.event.events;

import com.gamesense.api.event.GameSenseEvent;

public class StepEvent extends GameSenseEvent {

    private double y;

    public StepEvent(double y) {
        super();
        this.y = y;
    }

    public float getY() {
        return (float) this.y;
    }

}
