package com.gamesense.client.module.modules.movement;

import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "BoatFix", category = Category.Movement)
public class BoatFix extends Module {

    @Override
    public void onUpdate() {
        mc.player.ridingEntity.rotationYaw = mc.player.rotationYaw;
    }
}
