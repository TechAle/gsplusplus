package com.gamesense.client.module.modules.render;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "NoGlitchBlock", category = Category.Render)
public class noGlitchBlock extends Module {

    public BooleanSetting breakBlock = registerBoolean("Break", true);
    public BooleanSetting placeBlock = registerBoolean("Place", true);

}