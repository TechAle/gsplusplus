package com.gamesense.api.event.events;

import com.gamesense.api.event.GameSenseEvent;

public class SendMessageEvent extends GameSenseEvent {

    final String message;
    public SendMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}