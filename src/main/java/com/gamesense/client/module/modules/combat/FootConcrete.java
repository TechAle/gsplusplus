package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.modules.combat.Elevatot;
import com.gamesense.client.module.Module;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.PlacementUtil;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenCustomHashMap;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import scala.Int;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Doogie13
 * @since 15/07/2021
 */

@Module.Declaration(name = "FootConcrete", category = Category.Combat)
public class FootConcrete extends Module {
    ModeSetting mode = registerMode("Jump", Arrays.asList("jump", "clip"), "jump");
    IntegerSetting clipHeight = registerInteger("clipHeight", 5, 0, 25);
    IntegerSetting clipDelay = registerInteger("rubberbandDelay", 10, 0, 50);
    IntegerSetting placeDelay = registerInteger("placeDelay", 7, 0, 1000);
    BooleanSetting silentSwitch = registerBoolean("silentSwitch", true);
    BooleanSetting rotate = registerBoolean("rotate", true);
    BooleanSetting offHandObby = registerBoolean("offHandObby", false);
    DoubleSetting yOffset = registerDouble("yOffset", 1, 0, 5);

    private boolean activedOff;

    boolean doGlitch;

    float oldPitch;

    boolean hasPlaced;

    BlockPos burrowBlockPos;

    int oldslot;

    final Timer concreteTimer = new Timer();
    final Timer glitchTimer = new Timer();

    private final ArrayList<EnumFacing> exd = new ArrayList<EnumFacing>() {
        {
            add(EnumFacing.DOWN);
            add(EnumFacing.UP);
        }
    };

    public void onEnable() {

        if (mc.player.onGround) {

            BlockPos burrowBlockPos = new BlockPos(mc.player.posX,mc.player.posY-1,mc.player.posZ);

            mc.player.jump();
            concreteTimer.reset();
            glitchTimer.reset();

            doGlitch = false;

        } else {

            disable();

        }
    }

    public void onUpdate() {

        if (concreteTimer.getTimePassed() >= placeDelay.getValue()) {

            oldPitch = mc.player.rotationPitch;

            placeBlock(burrowBlockPos);

            doGlitch = true;

        }

        if (glitchTimer.getTimePassed() >= clipDelay.getValue() && mode.getValue().equals("clip") && doGlitch) {

            mc.player.setPosition(mc.player.posX, mc.player.posY + clipHeight.getValue(), mc.player.posZ);

            glitchTimer.reset();

            doGlitch = false;

            disable();

        } else if (mode.getValue().equals("jump") && glitchTimer.getTimePassed() >= clipDelay.getValue() && doGlitch) {

            mc.player.jump();

            glitchTimer.reset();

            disable();
        }
    }

    private boolean placeBlock(BlockPos pos) {
        EnumHand handSwing = EnumHand.MAIN_HAND;
        if (offHandObby.getValue()) {
            int obsidianSlot = InventoryUtil.findObsidianSlot(offHandObby.getValue(), activedOff);

            if (obsidianSlot == -1) {
                return false;
            }

            if (obsidianSlot == 9) {
                activedOff = true;
                if (mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemOffhand().getItem()).getBlock() instanceof BlockObsidian) {
                    // We can continue
                    handSwing = EnumHand.OFF_HAND;
                } else return false;
            }
        }

        if (true || silentSwitch.getValue()) {
            //int oldHand = mc.player.inventory.currentItem;
            int newHand = findRightBlock();

            if (newHand != -1) {
                if (mc.player.inventory.currentItem != newHand) {
                    if (silentSwitch.getValue()) {
                        if (!hasPlaced) {
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(newHand));
                            hasPlaced = true;
                            oldslot = mc.player.inventory.currentItem;
                        }
                    } else {
                        mc.player.inventory.currentItem = newHand;
                        mc.playerController.syncCurrentPlayItem();
                    }
                }
            } else {
                return false;
            }
        }

        return mode.getValue().equalsIgnoreCase("skull") ?
                PlacementUtil.place(pos, handSwing, rotate.getValue(), exd)
                : PlacementUtil.place(pos, handSwing, rotate.getValue(), !silentSwitch.getValue());

    }

    private int findRightBlock() {
        return InventoryUtil.findFirstBlockSlot(BlockObsidian.class, 0, 8);
    }
}





