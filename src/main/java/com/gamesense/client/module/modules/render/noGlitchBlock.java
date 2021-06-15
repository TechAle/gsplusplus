package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.BlockChangeEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.Arrays;

@Module.Declaration(name = "NoGlitchBlock", category = Category.Render)
public class noGlitchBlock extends Module {

    public BooleanSetting breakBlock = registerBoolean("Break", true);
    public BooleanSetting placeBlock = registerBoolean("Place", true);

}