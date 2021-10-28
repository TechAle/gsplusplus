package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "FastFall", category = Category.Movement)
public class FastFall extends Module {

    DoubleSetting speed = registerDouble("Speed", 0.5,0,1);

    @Override
    public void onUpdate() {
        if (!mc.player.onGround)
            mc.player.motionY += speed.getValue();

    }
}
