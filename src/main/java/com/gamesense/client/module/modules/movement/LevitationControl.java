package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;

import java.util.Objects;

@Module.Declaration(name = "LevitationControl",category = Category.Movement)
public class LevitationControl extends Module {

    DoubleSetting upAmplifier = registerDouble("Amplifier Up",1,1,3);
    DoubleSetting downAmplifier = registerDouble("Amplifier Down",1,1,3);

    @Override
    public void onUpdate() {

        if (mc.player.isPotionActive(MobEffects.LEVITATION)) {

            int amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(Objects.requireNonNull(Potion.getPotionById(25)))).getAmplifier();

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.motionY = ((0.05D * (double)(amplifier + 1) - mc.player.motionY) * 0.2D) * upAmplifier.getValue(); // reverse the levitation effect if not holding space
            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.motionY = -(((0.05D * (double)(amplifier + 1) - mc.player.motionY) * 0.2D) * downAmplifier.getValue());
            } else {
                mc.player.motionY = 0;
            }

        }
    }
}
