/*
package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;

@Module.Declaration(name = "AutoCreeper", category = Category.Combat)
public class AutoCreeper extends Module {

    // Module to get me used to combat stuff idk

    IntegerSetting delay = registerInteger("Delay", 3, 0, 20);
    DoubleSetting range = registerDouble("Range", 5, 0, 6);
    BooleanSetting rotate = registerBoolean("Rotate", true);
    BooleanSetting silent = registerBoolean("Silent Switch", true);

    EntityPlayer target = null;
    int oldSlot;
    int slot = -1;
    Vec2f rot;

    Timer delayTimer = new Timer();

    @Override
    public void onUpdate() {

        if (target == null || target.getDistance(mc.player) > range.getValue()) {

            target = getTarget();

        } else if (mc.player.ticksExisted % delay.getValue() == 0){
            slot = getSlot();

            if (slot == -1)
                disable();

            oldSlot = mc.player.inventory.currentItem;

            if (silent.getValue()) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            } else {
                mc.player.inventory.currentItem = slot;
            }

            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(target.posX, Math.ceil(target.posY - 0.5)-1, target.posZ), EnumFacing.UP, EnumHand.MAIN_HAND, 0, 0, 0));

            if (silent.getValue()) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
            } else {

                mc.player.inventory.currentItem = oldSlot;

            }
        }
    }


    public EntityPlayer getTarget() {

        target = PlayerUtil.findClosestTarget();
        return target;

    }

    public int getSlot() {

        int newSlot = -1;
        for (int i = 0; i < 9; i++) {

            ItemStack stack =
                    mc.player.inventory.getStackInSlot(i);

            if (stack == ItemStack.EMPTY && stack.getItem() == Items.SPAWN_EGG) {
                continue;
            }

            newSlot = i;
            break;
        }

        return newSlot;

    }

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

}
*/
