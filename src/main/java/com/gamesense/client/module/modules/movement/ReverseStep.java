package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;

import java.util.Arrays;

@Module.Declaration(name = "ReverseStep", category = Category.Movement)
public class ReverseStep extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("Normal", "Vanilla"), "Normal");
    DoubleSetting height = registerDouble("Height", 2.5, 0.5, 10);

    public void onUpdate() {
        if (mc.world == null || mc.player == null || mc.player.isInWater() || mc.player.isInLava() || mc.player.isOnLadder()
                || mc.gameSettings.keyBindJump.isKeyDown()) {
            return;
        }

        if (ModuleManager.isModuleEnabled(Speed.class)) return;

        if (mc.player != null && mc.player.onGround && !mc.player.isInWater() && !mc.player.isOnLadder()) {

            float dist = 69696969;

            switch (mode.getValue()) {
                case "Vanilla": {
                    for (double y = 0.0; y < this.height.getValue() + 0.5; y += 0.01) {
                        if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, -y, 0.0)).isEmpty()) {
                            mc.player.motionY = -10.0;
                            break;
                        }
                    }
                }
                case "Normal": {


                    for (double y = 0.0; y < this.height.getValue() + 1; y += 0.01) {
                        if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, -y, 0.0)).isEmpty())
                            return;
                        else
                            dist = (float) y;

                        boolean doIt =
                                !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, -dist + 0.1, 0.0)).isEmpty();

                        if (dist < height.getValue() && doIt) {
                            PlayerUtil.fall((int) dist);
                        }
                    }


                }
            }
        }
    }
}