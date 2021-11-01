package com.gamesense.client.module.modules.combat;

import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;

@Module.Declaration(name = "Friends", category = Category.Combat, enabled = true)
public class Friends extends Module {

    public static Friends INSTANCE;

    public Friends() {
        INSTANCE = this;
    }

}
