package com.gamesense.client.module.modules.movement;

import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.client.settings.KeyBinding;

@Module.Declaration(name = "AutoWalk", category = Category.Movement)
public class AutoWalk extends Module {

    @Override
    public void onUpdate() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
    }

    @Override
    protected void onDisable() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
    }
}
