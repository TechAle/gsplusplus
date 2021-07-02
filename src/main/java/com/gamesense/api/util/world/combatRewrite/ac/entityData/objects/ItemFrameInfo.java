package com.gamesense.api.util.world.combatRewrite.ac.entityData.objects;

import net.minecraft.network.play.server.SPacketSpawnObject;

public class ItemFrameInfo extends HangingInfo {

    public ItemFrameInfo(SPacketSpawnObject entity) {
        super(entity);

        this.updateSize();
    }

    public int getWidthPixels() {
        return 12;
    }

    public int getHeightPixels() {
        return 12;
    }
}
