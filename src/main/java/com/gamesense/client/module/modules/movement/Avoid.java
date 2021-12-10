package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.BoundingBoxEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

@Module.Declaration(name = "Avoid", category = Category.Movement)
public class Avoid extends Module {

    public static Avoid INSTANCE;
    public Avoid() {
        INSTANCE = this;
    }

    public BooleanSetting unloaded = registerBoolean("Unloaded", false);
    public BooleanSetting cactus = registerBoolean("Cactus", false);
    public BooleanSetting fire = registerBoolean("Fire", false);
    public BooleanSetting bigFire = registerBoolean("Extend Fire", false, () -> fire.getValue());

    @EventHandler
    private final Listener<BoundingBoxEvent> playerMoveEventListener = new Listener<>(event -> {

        if (event.getBlock().equals(Blocks.STRUCTURE_VOID) && unloaded.getValue()
                || event.getBlock().equals(Blocks.CACTUS) && cactus.getValue()
                || event.getBlock().equals(Blocks.FIRE) && fire.getValue())

            if (bigFire.getValue() && event.getBlock() == Blocks.FIRE)
                event.setbb(Block.FULL_BLOCK_AABB.expand(0.1,0.1,0.1));
            else
                event.setbb(Block.FULL_BLOCK_AABB);

    });

}
