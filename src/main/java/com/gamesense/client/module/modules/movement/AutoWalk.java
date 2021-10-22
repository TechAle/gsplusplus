package com.gamesense.client.module.modules.movement;

import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "AutoWalk", category = Category.Movement)
public class AutoWalk extends Module {

    @Override
    public void onUpdate() {
        if (PlayerUtil.nullCheck()) {
            mc.player.moveForward = 1;
        }
    }
}
