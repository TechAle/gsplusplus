package com.gamesense.client.module.modules.render;

import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "Ambience", category = Category.Render)
public class Ambience extends Module {

    public ColorSetting colorLight = registerColor("Color Light", new GSColor(255, 255, 255));


}