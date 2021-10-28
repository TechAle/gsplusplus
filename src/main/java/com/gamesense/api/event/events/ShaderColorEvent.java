package com.gamesense.api.event.events;

import com.gamesense.api.event.GameSenseEvent;
import com.gamesense.api.util.render.GSColor;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.awt.*;

@Cancelable
public class ShaderColorEvent extends GameSenseEvent {

    private final Entity entity;
    private Color color;

    public ShaderColorEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color in) {
        color = in;
    }
}