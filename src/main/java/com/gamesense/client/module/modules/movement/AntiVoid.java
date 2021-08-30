package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockObsidian;
import net.minecraft.item.ItemChorusFruit;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Array;
import java.util.Arrays;

@Module.Declaration(name = "AntiVoid", category = Category.Movement)
public class AntiVoid extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("Freeze", "Glitch", "Catch"), "Freeze");
    DoubleSetting height = registerDouble("Height", 2, 0, 5);
    BooleanSetting chorus = registerBoolean("Chorus", false);

    boolean chor;

    @Override
    public void onUpdate() {
        if (!mc.player.noClip /*to account for packetfly*/ && mc.player.posY < height.getValue() && mode.getValue().equalsIgnoreCase("Freeze") && mc.world.getBlockState(new BlockPos(mc.player.posX, 1, mc.player.posZ)).getMaterial().isReplaceable()) {

            if (mode.getValue().equals("Freeze")){
                int newSlot;

                mc.player.setVelocity(0.0D, 0.0D, 0.0D);

                if (mc.player.getRidingEntity() != null)
                    mc.player.getRidingEntity().setVelocity(0.0D, 0.0D, 0.0D);

                if (chorus.getValue()) {
                    // Courtesy of KAMI, this item finding algo
                    newSlot = -1;
                    for (int i = 0; i < 9; i++) {
                        // filter out non-block items
                        ItemStack stack =
                                mc.player.inventory.getStackInSlot(i);

                        if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemChorusFruit)) {
                            continue;
                        }

                        newSlot = i;
                        break;
                    }

                    if (newSlot == -1) {

                        newSlot = 1;

                        MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "Out of chorus!");
                        chor = false;

                    } else {
                        chor = true;
                    }

                    if (chor) {

                        mc.player.inventory.currentItem = newSlot;

                        if (mc.player.canEat(true)) {

                            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));

                        }

                    }
                }

            } else if (mode.getValue().equals("Glitch")) {

                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX,mc.player.posY + 69, mc.player.posZ,mc.player.onGround));

            } else if (mode.getValue().equals("Catch")){

                int oldSlot = mc.player.inventory.currentItem;

                int newSlot;
                // Courtesy of KAMI, this block finding algo
                newSlot = -1;
                for (int i = 0; i < 9; i++) {
                    // filter out non-block items
                    ItemStack stack =
                            mc.player.inventory.getStackInSlot(i);

                    if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) {
                        continue;
                    }

                    // filter out non-solid blocks
                    if (!Block.getBlockFromItem(stack.getItem()).getDefaultState()
                            .isFullBlock())
                        continue;

                    // don't use falling blocks if it'd fall
                    if (((ItemBlock) stack.getItem()).getBlock() instanceof BlockFalling) {
                        continue;
                    }

                    newSlot = i;
                    break;
                }

                if (newSlot == -1) {

                    newSlot = 1;

                    MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "Out of valid blocks. Disabling!");
                    disable();

                }

                mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));

                PlacementUtil.place(new BlockPos(mc.player.posX, 1, mc.player.posZ), EnumHand.MAIN_HAND, true);

                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

            }
        }
    }
}

