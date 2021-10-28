package com.gamesense.client.module.modules.render;

import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraftforge.client.event.EntityViewRenderEvent;

@Module.Declaration(name = "FOVMod", category = Category.Render)
public class FOVMod extends Module {

    IntegerSetting fov = registerInteger("FOV", 90,1,179);

    @EventHandler
    private final Listener<EntityViewRenderEvent.FOVModifier> fovModifierListener = new Listener<>(event -> {
            event.setFOV(fov.getValue().floatValue());
    });

}
