package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.util.math.BlockPos;

@Module.Declaration(name = "FastFall", category = Category.Movement)
public class FastFall extends Module {

    DoubleSetting dist = registerDouble("Min Distance", 3,0,25);
    DoubleSetting speed = registerDouble("Multiplier", 3,0,10);

    @Override
    public void onUpdate() {
        if (mc.world.isAirBlock(new BlockPos(mc.player.getPositionVector()))) {
            if (mc.player.onGround &&
                    (!mc.player.isElytraFlying()
                            || mc.player.fallDistance < dist.getValue()
                            || !mc.player.capabilities.isFlying))
                mc.player.motionY -= speed.getValue();
        }

    }
}
