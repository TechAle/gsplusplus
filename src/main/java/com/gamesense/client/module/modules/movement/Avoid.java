package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "Avoid", category = Category.Movement)
public class Avoid extends Module {

    public static Avoid INSTANCE;
    public Avoid() {
        INSTANCE = this;
    }

    public BooleanSetting theVoid = registerBoolean("Void", false);
    public BooleanSetting cactus = registerBoolean("Cactus", false);
    public BooleanSetting fire = registerBoolean("Fire", false);
    public BooleanSetting bigFire = registerBoolean("Extend Fire", false, () -> fire.getValue());
    BooleanSetting cancel = registerBoolean("Cancel Fire", false, () -> fire.getValue());

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (mc.world.getBlockState(new BlockPos(mc.player.getPositionVector()).add(mc.player.motionX, mc.player.motionY, mc.player.motionZ)).getBlock().equals(Blocks.FIRE) && cancel.getValue() && fire.getValue()) {
            event.setX(0);
            event.setY(0);
            event.setZ(0);
        }

    });

}
