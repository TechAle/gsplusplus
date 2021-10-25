package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "Avoid", category = Category.Movement)
public class Avoid extends Module {

    public static Avoid INSTANCE;

    public Avoid() {
        INSTANCE = this;
    }

    public BooleanSetting theVoid = registerBoolean("Void",false);
    public BooleanSetting cactus = registerBoolean("Cactus",false);
    public BooleanSetting fire = registerBoolean("Fire",false);
    public BooleanSetting bigFire = registerBoolean("Extend Fire",false, () -> fire.getValue());

}
