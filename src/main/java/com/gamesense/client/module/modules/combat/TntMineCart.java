package com.gamesense.client.module.modules.combat;


import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

@Module.Declaration(name = "TntMineCart", category = Category.Combat)
public class TntMineCart extends Module {

    DoubleSetting range = registerDouble("Range", 5, 0, 10);
    IntegerSetting delay = registerInteger("Cart Delay", 3, 0, 20);
    IntegerSetting torchDelay = registerInteger("Torch Delay", 10, 0, 40);
    BooleanSetting rotate = registerBoolean("Rotate", false);

    Entity target;
    BlockPos tpos;
    Timer torchT = new Timer();
    Timer retry = new Timer();

    boolean allowMC = true;
    boolean allowTorch = true;

    @Override
    protected void onEnable() {
        target = getTarget();
        tpos = new BlockPos(target.getPositionVector());
        allowMC = true;
        allowTorch = true;
    }

    @Override
    public void onUpdate() {
        if (PlayerUtil.nullCheck()) {

            if (getTargetValid(target)) {

                disable();
                return;

            }

            if (!(BlockUtil.getBlock(tpos) == Blocks.ACTIVATOR_RAIL)) {

                if (delay.getValue() % mc.player.ticksExisted == 0 && !(torchT.getTimePassed() / 50 >= torchDelay.getValue())) {

                    if (!(getSlot(Blocks.REDSTONE_TORCH) == -1)) {
                        mc.player.inventory.currentItem = getSlot(Blocks.ACTIVATOR_RAIL);
                    } else disable();

                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(tpos, EnumFacing.NORTH, EnumHand.MAIN_HAND, 0, 0, 0));

                    allowTorch = true;

                }
            } else if (!(torchT.getTimePassed() / 50 >= torchDelay.getValue())) {

                allowMC = false;

                PlacementUtil.place(tpos.add(0, 1, 0), EnumHand.MAIN_HAND, rotate.getValue());

                allowTorch = false;

                if (!(getSlot(Blocks.REDSTONE_TORCH) == -1)) {
                    mc.player.inventory.currentItem = getSlot(Blocks.REDSTONE_TORCH);
                } else disable();

                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, tpos.add(0, 1, 0), Objects.requireNonNull(BlockUtil.getPlaceableSide(tpos.add(0, 1, 0)))));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, tpos.add(0, 1, 0), Objects.requireNonNull(BlockUtil.getPlaceableSide(tpos.add(0, 1, 0)))));

                retry.reset();

            } else if (retry.getTimePassed() >= 400) {

                allowTorch = true;
                allowMC = true;

            }
        }


    }

    Entity getTarget() {

        return PlayerUtil.findClosestTarget(range.getValue(), null);

    }

    boolean getTargetValid(Entity existing) {

        return target.getDistance(mc.player) > range.getValue();

    }

    int getSlot(Item item) {

        int newSlot = -1;

        for (int i = 0; i < 9; i++) {
            // filter out non-block items
            ItemStack stack =
                    mc.player.inventory.getStackInSlot(i);


            // filter out non-solid blocks
            if (!(stack.getItem().getClass() == item.getClass()))
                continue;


            newSlot = i;
            break;
        }

        return newSlot;

    }

    int getSlot(Block block) {

        int newSlot = -1;

        for (int i = 0; i < 9; i++) {
            // filter out non-block items
            ItemStack stack =
                    mc.player.inventory.getStackInSlot(i);


            // filter out non-solid blocks
            if (!(Block.getBlockFromItem(stack.getItem()).getClass() == block.getClass()))
                continue;


            newSlot = i;
            break;
        }

        return newSlot;

    }

}
