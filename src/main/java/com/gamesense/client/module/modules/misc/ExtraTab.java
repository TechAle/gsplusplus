package com.gamesense.client.module.modules.misc;

import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "ExtraTab", category = Category.Misc)
public class ExtraTab extends Module {
    public IntegerSetting players = registerInteger("Players", 255, 1, 500);
}
