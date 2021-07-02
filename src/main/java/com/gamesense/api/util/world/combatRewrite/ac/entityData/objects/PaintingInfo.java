package com.gamesense.api.util.world.combatRewrite.ac.entityData.objects;

import net.minecraft.entity.item.EntityPainting;
import net.minecraft.network.play.server.SPacketSpawnPainting;

public class PaintingInfo extends HangingInfo {

    private final EntityPainting.EnumArt art;

    public PaintingInfo(SPacketSpawnPainting painting) {
        super(painting.getEntityID(), painting.getPosition(), painting.getFacing());

        String title = painting.getTitle();
        EntityPainting.EnumArt art = EntityPainting.EnumArt.KEBAB;
        for (EntityPainting.EnumArt entityPaintings : EntityPainting.EnumArt.values()) {
            if (entityPaintings.title.equals(title)) {
                art = entityPaintings;
                break;
            }
        }

        this.art = art;
        this.updateSize();
    }

    public int getWidthPixels() {
        return this.art.sizeX;
    }

    public int getHeightPixels() {
        return this.art.sizeY;
    }
}
