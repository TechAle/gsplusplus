package com.gamesense.client.module.modules.render;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "HandThing", category = Category.Render)
public class HandThing extends Module {
    public DoubleSetting offX = registerDouble("OffhandX", 0.0, -1.0, 1.0);
    public DoubleSetting offY = registerDouble("OffhandY", 0.0, -1.0, 1.0);
    public DoubleSetting mainX = registerDouble("MainhandX", 0.0, -1.0, 1.0);
    public DoubleSetting mainY = registerDouble("MainhandY", 0.0, -1.0, 1.0);
}
