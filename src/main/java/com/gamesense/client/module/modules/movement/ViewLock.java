package com.gamesense.client.module.modules.movement;

import com.gamesense.api.util.misc.Timer;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

import java.util.Objects;

@Module.Declaration(name = "ViewLock", category = Category.Movement)
public class ViewLock extends Module {

    boolean dontChange;

    Timer timer = new Timer();

    @Override
    public void onUpdate() {
        final int angle = 360 / 8;
        float yaw = mc.player.rotationYaw;

        if (org.lwjgl.input.Keyboard.isKeyDown(205) && !dontChange) {

            timer.reset();
            dontChange = true;
            yaw += 45;

        } else if (org.lwjgl.input.Keyboard.isKeyDown(203) && !dontChange) {

            timer.reset();
            dontChange = true;
            yaw -= 45;

        }

        if (dontChange) {

            if (timer.hasReached(250)) {

                dontChange = false;

            }

        }

        yaw = (float)(Math.round(yaw / angle) * angle);
        mc.player.rotationYaw = yaw;
        if (mc.player.isRiding()) {
            Objects.requireNonNull(mc.player.getRidingEntity()).rotationYaw = yaw;
        }
    }
}
