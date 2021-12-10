package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

/**
 * @author Hoosiers
 * @author 0b00101010
 * @since 09/28/20
 * @since 25/01/21
 */

@Module.Declaration(name = "Anchor", category = Category.Movement, priority = 1000)
public class Anchor extends Module {

    BooleanSetting guarantee = registerBoolean("Guarantee Hole", true);
    IntegerSetting activateHeight = registerInteger("Activate Height", 2, 1, 5);
    IntegerSetting activationPitch = registerInteger("Activation Pitch", 75, 0, 90);
    BooleanSetting stopSpeed = registerBoolean("Stop Speed", true);
    BooleanSetting fastFall = registerBoolean("Fast Fall", false);

    public static boolean active = false;

    BlockPos playerPos;

    protected void onDisable() {
        active = false;
    }

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {
        if (mc.player == null || mc.world == null) {
            return;
        }
        active = false;

        if (mc.player.rotationPitch < activationPitch.getValue())
            return;

        if (mc.player.posY < 0) {
            return;
        }

        double blockX = Math.floor(mc.player.posX);
        double blockZ = Math.floor(mc.player.posZ);

        double offsetX = Math.abs(mc.player.posX - blockX);
        double offsetZ = Math.abs(mc.player.posZ - blockZ);

        if (guarantee.getValue() && (offsetX < 0.3f || offsetX > 0.7f || offsetZ < 0.3f || offsetZ > 0.7f)) {
            return;
        }

        playerPos = new BlockPos(blockX, mc.player.posY, blockZ);

        if (mc.world.getBlockState(playerPos).getBlock() != Blocks.AIR) {
            return;
        }

        BlockPos currentBlock = playerPos.down();
        for (int i = 0; i < activateHeight.getValue(); i++) {
            currentBlock = currentBlock.down();
            if (mc.world.getBlockState(currentBlock).getBlock() != Blocks.AIR) {
                HashMap<HoleUtil.BlockOffset, HoleUtil.BlockSafety> sides = HoleUtil.getUnsafeSides(currentBlock.up());
                sides.entrySet().removeIf(entry -> entry.getValue() == HoleUtil.BlockSafety.RESISTANT);
                if (sides.size() == 0) {
                    if (stopSpeed.getValue())
                        active = true;
                    mc.player.motionX = 0f;
                    mc.player.motionZ = 0f;
                    if (fastFall.getValue())
                        mc.player.motionY = -10f;
                    event.cancel();
                }
            }
        }
    });


}