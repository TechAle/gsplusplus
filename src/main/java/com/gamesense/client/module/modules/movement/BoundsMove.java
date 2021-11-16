package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.PhaseUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;

import java.util.Arrays;

@Module.Declaration(name = "BoundsMove", category = Category.Movement)
public class BoundsMove extends Module {

    ModeSetting bound = registerMode("Bounds", PhaseUtil.bound, "Up");

    @Override
    public void onUpdate() {
        if ((mc.player.moveForward != 0 || mc.player.moveStrafing != 0)
                && !(ModuleManager.getModule(Flight.class).isEnabled()
                && ModuleManager.getModule(Flight.class).mode.getValue().equalsIgnoreCase("Packet"))) {
            PhaseUtil.doBounds(bound.getValue(), true);
        }
    }
}


