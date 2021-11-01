package com.gamesense.client.module.modules.movement;

import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "NoClip", category = Category.Movement)
public class NoClip extends Module {

    @Override
    public void onUpdate() {
        mc.player.noClip = true;
    }

    @Override
    protected void onDisable() {
        mc.player.noClip = false;
    }
}
