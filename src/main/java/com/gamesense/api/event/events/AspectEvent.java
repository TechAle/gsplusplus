package com.gamesense.api.event.events;

import com.gamesense.api.event.GameSenseEvent;

public class AspectEvent extends GameSenseEvent {
    private float aspect;

    public AspectEvent(float aspect) {
        this.aspect = aspect;
    }

    public float getAspect() {
        return this.aspect;
    }

    public void setAspect(float aspect) {
        this.aspect = aspect;
    }
}