package com.gamesense.client.module.modules.movement;

import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "AutoJump", category = Category.Movement)
public class AutoJump extends Module {

    public void onUpdate() {

        if (mc.player.onGround) {

            mc.player.jump();

        }

    }

}
