package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

import static java.lang.Float.POSITIVE_INFINITY;

@Module.Declaration(name = "ReverseStep", category = Category.Movement)
public class ReverseStep extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("Normal", "Vanilla"),"Normal");
    DoubleSetting height = registerDouble("Height", 2.5, 0.5, 10);

    public void onUpdate() {
        if (mc.world == null || mc.player == null || mc.player.isInWater() || mc.player.isInLava() || mc.player.isOnLadder()
            || mc.gameSettings.keyBindJump.isKeyDown()) {
            return;
        }

        if (ModuleManager.isModuleEnabled(Speed.class)) return;

        if (mc.player != null && mc.player.onGround && !mc.player.isInWater() && !mc.player.isOnLadder()) {

            float dist = 69696969;

            switch (mode.getValue()){
                case "Vanilla": {
                    for (double y = 0.0; y < this.height.getValue() + 0.5; y += 0.01) {
                        if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, -y, 0.0)).isEmpty()) {
                            mc.player.motionY = -10.0;
                            break;
                        }
                    }
                }
                case "Normal": {

                    for (int i = 0; i < height.getValue(); i++){
                        if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, -i, 0.0)).isEmpty())
                            return;
                        else
                            dist = i;

                        if (!(dist >= height.getValue())) {
                            PlayerUtil.fall((int) dist);
                        }
                    }

                }
            }
        }
    }
}