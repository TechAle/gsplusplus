package com.gamesense.api.event.events;

import com.gamesense.api.event.GameSenseEvent;

public class RenderHand extends GameSenseEvent {
    private final float ticks;

    public RenderHand(float ticks) {
        this.ticks = ticks;
    }

    public float getPartialTicks() {
        return ticks;
    }


    public static class PostOutline extends RenderHand {
        public PostOutline(float ticks) {
            super(ticks);
        }
    }

    public static class PreOutline extends RenderHand {
        public PreOutline(float ticks) {
            super(ticks);
        }
    }

    public static class PostFill extends RenderHand {
        public PostFill(float ticks) {
            super(ticks);
        }
    }

    public static class PreFill extends RenderHand {
        public PreFill(float ticks) {
            super(ticks);
        }
    }



}