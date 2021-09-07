package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;

@Module.Declaration(name = "TntMineCart", category = Category.Combat)
public class TntMineCart extends Module {

    IntegerSetting delay = registerInteger("Delay", 3, 0, 20);
    BooleanSetting rotate = registerBoolean("Rotate", true);
    BooleanSetting silent = registerBoolean("Silent Switch", true);

    EntityPlayer target;

    int slot;
    int oldSlot;
    int phase;

    Vec2f rot;
    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {

        if (rotate.getValue() && target != null) {
            rot = RotationUtil.getRotationTo(target.getPositionVector());
            if (event.getPacket() instanceof CPacketPlayer) {

                ((CPacketPlayer) event.getPacket()).yaw = rot.x;
                ((CPacketPlayer) event.getPacket()).pitch = rot.y;

            }
        }
    });

    public static int getSlot(Block blockToFind) {

        int slot = -1;
        for (int i = 0; i < 9; i++) {

            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }

            Block block = ((ItemBlock) stack.getItem()).getBlock();
            if (block.equals(blockToFind)) {
                slot = i;
                break;
            }

        }

        return slot;

    }

    @Override
    public void onUpdate() {

        if (mc.world.isAirBlock(new BlockPos(target.getPositionVector())))
            phase = 0;
        else
            phase = 1;

        switch (phase) {
            case 0: {

                target = getTarget();
                oldSlot = mc.player.inventory.currentItem;
                slot = getSlot(Blocks.ACTIVATOR_RAIL);

                if (!silent.getValue())
                    mc.player.inventory.changeCurrentItem(slot);

                else
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));

                PlacementUtil.place(new BlockPos(target.getPositionVector()), EnumHand.MAIN_HAND, false); // we are already rotating with the listener

                if (silent.getValue())
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));

            }
            case 1: {

                if (mc.player.ticksExisted % delay.getValue() == 0) {
                    target = getTarget();
                    oldSlot = mc.player.inventory.currentItem;
                    slot = getSlot(Blocks.TNT);

                    if (!silent.getValue())
                        mc.player.inventory.changeCurrentItem(slot);

                    else
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));

                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(target.getPositionVector()), EnumFacing.UP, EnumHand.MAIN_HAND, 0, 0, 0));

                    if (silent.getValue())
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                }

            }
        }

    }

    public EntityPlayer getTarget() {

        target = PlayerUtil.findClosestTarget();
        return target;

    }

}
