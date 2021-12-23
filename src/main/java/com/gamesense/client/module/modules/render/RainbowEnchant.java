package com.gamesense.client.module.modules.render;

import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;


@Module.Declaration(name = "RainbowEnchant", category = Category.Render)
public class RainbowEnchant extends Module {
    public ColorSetting color = registerColor("Color", new GSColor(255, 16, 19, 100));
}
