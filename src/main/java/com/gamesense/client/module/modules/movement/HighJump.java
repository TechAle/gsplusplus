package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

@Module.Declaration(name = "HighJump", category = Category.Movement)
public class HighJump extends Module {

       public DoubleSetting height = registerDouble("Height", 1, 0, 25);

       // Skidders, see MixinEntityPlayerSP
}
