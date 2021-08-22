package com.gamesense.client.module.modules.movement;

import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "ViewLock", category = Category.Movement)
public class ViewLock extends Module {

    @Override
    public void onUpdate() {
        float yawRounded;

        float yaw = Math.abs(Math.round(mc.player.rotationYaw) % 360);
        float division = (int) Math.floor(yaw / 45);
        float remainder = (int) (yaw % 45);
        if (remainder < 45 / 2) {
            yawRounded = 45 * division;
        } else {
            yawRounded = 45 * (division + 1);
        }

        mc.player.rotationYaw = yawRounded;

    }
}
