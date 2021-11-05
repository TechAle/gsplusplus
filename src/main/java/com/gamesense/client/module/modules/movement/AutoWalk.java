package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

@Module.Declaration(name = "AutoWalk", category = Category.Movement)
public class AutoWalk extends Module {

    @Override
    public void onUpdate() {
        mc.player.movementInput.moveForward = 1;
    }
}
