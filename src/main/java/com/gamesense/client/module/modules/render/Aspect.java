package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.AspectEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.modules.combat.PistonCrystal;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

@Module.Declaration(name = "Aspect", category = Category.Render)
public class Aspect extends Module {

    DoubleSetting aspect = registerDouble("Aspect", 1, 0, 10);
    BooleanSetting credits = registerBoolean("Credits", false);

    @Override
    protected void onEnable() {
        if (credits.getValue())
            PistonCrystal.printDebug("Aspect module imported from quantum-0.4.6", false);
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<AspectEvent> aspectListener = new Listener<>(event -> event.setAspect(aspect.getValue().floatValue()));


}