package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
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
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
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
    BooleanSetting chorus = registerBoolean("Chorus", false, () -> mode.getValue().equals("Freeze"));
    BooleanSetting packetfly = registerBoolean("PacketFly", false, () -> mode.getValue().equals("Catch"));

    boolean chor;

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        try {


            if (mc.player.posY < height.getValue() + 0.1 && mode.getValue().equalsIgnoreCase("Freeze") && mc.world.getBlockState(new BlockPos(mc.player.posX, 0, mc.player.posZ)).getMaterial().isReplaceable()) {

                switch (mode.getValue()) {
                    case "Freeze": {
                        int newSlot;

                        mc.player.posY = height.getValue();
                        event.setY(0);

                        if (mc.player.getRidingEntity() != null)
                            mc.player.ridingEntity.setVelocity(0.0D, 0.0D, 0.0D);

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

                                    mc.player.setActiveHand(EnumHand.MAIN_HAND);

                                }

                            }
                        }

                        break;
                    }
                    case "Glitch":

                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 69, mc.player.posZ, mc.player.onGround));

                        break;
                    case "Catch": {

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

                        PlacementUtil.place(new BlockPos(mc.player.posX, 0, mc.player.posZ), EnumHand.MAIN_HAND, true);

                        if (mc.world.getBlockState(new BlockPos(mc.player.posX, 0, mc.player.posZ)).getMaterial().isReplaceable() && packetfly.getValue()) {

                            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + mc.player.motionX, mc.player.posY + 0.0624, mc.player.posZ + mc.player.motionZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));

                        }

                        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

                        break;
                    }
                }
            }
        } catch (Exception ignored) {}
    });
}

