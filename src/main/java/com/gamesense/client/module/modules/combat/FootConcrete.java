package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import com.gamesense.client.module.modules.movement.Blink;

import java.util.Arrays;

/**
 * @author Doogie13
 * @since 15/07/2021
 */

@Module.Declaration(name = "FootConcrete", category = Category.Combat)
public class FootConcrete extends Module {
    ModeSetting jumpMode = registerMode("jumpMode", Arrays.asList("real", "fake"), "real");
    ModeSetting mode = registerMode("rubberbandMode", Arrays.asList("jump", "clip"), "jump", () -> jumpMode.getValue().equals("real"));
    BooleanSetting useBlink = registerBoolean("useBlink", true, () -> jumpMode.getValue().equals("real"));
    BooleanSetting useTimer = registerBoolean("useTimer", true, () -> jumpMode.getValue().equals("real"));
    DoubleSetting timerSpeed = registerDouble("timerSpeed", 20, 1, 50, () -> useTimer.getValue() && jumpMode.getValue().equals("real"));
    BooleanSetting absoluteClipHeight = registerBoolean("absoluteClipHeight", false);
    IntegerSetting clipHeight = registerInteger("clipHeight", -5, -25, 25);
    IntegerSetting placeDelay = registerInteger("placeDelay", 160, 0, 250, () -> jumpMode.getValue().equals("real"));
    BooleanSetting allowEchest = registerBoolean("allowEchest", true);
    BooleanSetting onlyEchest = registerBoolean("onlyEchest", false, () -> allowEchest.getValue());
    BooleanSetting silentSwitch = registerBoolean("silentSwitch", true, () -> jumpMode.getValue().equals("real"));
    BooleanSetting phase = registerBoolean("clipDown", false);
    BooleanSetting rotate = registerBoolean("rotate", true);

    boolean doGlitch;

    boolean invalidHotbar;

    float oldPitch;

    int oldSlot;

    int targetBlockSlot;

    BlockPos burrowBlockPos;

    float timerSpeedVal;

    int oldslot;

    final Timer concreteTimer = new Timer();

    public void onEnable() {

        invalidHotbar = false;

        //BLINK AND TIMER

        if (useBlink.getValue()) {
            ModuleManager.getModule(Blink.class).enable();
        }

        timerSpeedVal = timerSpeed.getValue().floatValue();

        if (useTimer.getValue()) {
            Minecraft.getMinecraft().timer.tickLength = 50.0f / timerSpeedVal;
        }

        // FIND SLOT

        targetBlockSlot = InventoryUtil.findObsidianSlot(false, false);

        if (targetBlockSlot == -1 || onlyEchest.getValue()) {
            if (allowEchest.getValue())
                targetBlockSlot = InventoryUtil.findFirstBlockSlot(Blocks.ENDER_CHEST.getClass(), 0, 8);
        }

        if (targetBlockSlot == -1) {

            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "No burrow blocks in hotbar, disabling");

            invalidHotbar = true;

            disable();

            if (useBlink.getValue()) {
                ModuleManager.getModule(Blink.class).disable();
            }

            if (useTimer.getValue()) {
                Minecraft.getMinecraft().timer.tickLength = 50;
            }

            disable();

        }

        // JUMP

        if (!invalidHotbar) {

            if (mc.player.onGround) {


                burrowBlockPos = new BlockPos(Math.ceil(mc.player.posX) - 1, Math.ceil(mc.player.posY - 1) + 1.5, Math.ceil(mc.player.posZ) - 1);

                if (mc.world.isOutsideBuildHeight(burrowBlockPos)) {
                    disable();
                    MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You are trying to burrow above build limit, disabling.");
                }

                if (jumpMode.getValue().equals("real")) {
                    mc.player.jump();
                } else {

                    // CIRUU BURROW (not ashamed to admit it)

                    targetBlockSlot = InventoryUtil.findObsidianSlot(false, false);

                    if (targetBlockSlot == -1 || onlyEchest.getValue()) {
                        if (allowEchest.getValue())
                            targetBlockSlot = InventoryUtil.findFirstBlockSlot(Blocks.ENDER_CHEST.getClass(), 0, 8);
                    }

                    oldSlot = mc.player.inventory.currentItem;

                    if (targetBlockSlot == -1) {
                        MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You are trying to burrow above build limit, disabling.");
                        disable();
                    }

                    mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));

                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, true));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, true));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, true));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16, mc.player.posZ, true));

                    PlacementUtil.place(burrowBlockPos, EnumHand.MAIN_HAND, rotate.getValue());

                    if (!absoluteClipHeight.getValue()) {

                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + clipHeight.getValue(), mc.player.posZ, true));

                    } else {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, clipHeight.getValue(), mc.player.posZ, true));
                    }

                    mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));

                    if (phase.getValue()) {
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + mc.player.motionX, mc.player.posY - 0.0624, mc.player.posZ + mc.player.motionZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + mc.player.motionX, mc.player.posY - 42069, mc.player.posZ + mc.player.motionZ, mc.player.rotationYaw , mc.player.rotationPitch, true));
                    }

                    disable();

                }
                concreteTimer.reset();

                doGlitch = false;

            } else {

                disable();

            }
        }
    }

    public void onUpdate() {

        if (!invalidHotbar) {

            // PLACE

            if (concreteTimer.getTimePassed() >= placeDelay.getValue()) {

                if (useBlink.getValue()) {

                    ModuleManager.getModule(Blink.class).disable();
                }

                if (!silentSwitch.getValue()) { //Silent switch check

                    mc.player.inventory.currentItem = targetBlockSlot;

                } else {

                    mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));

                }

                oldPitch = mc.player.rotationPitch;

                PlacementUtil.place(burrowBlockPos, EnumHand.MAIN_HAND, rotate.getValue());

                oldslot = mc.player.inventory.currentItem;

                doGlitch = true;

                //DISABLE TIMER

                if (useTimer.getValue()) {
                    Minecraft.getMinecraft().timer.tickLength = 50;
                }

            }

            // RUBBERBAND

            if (mode.getValue().equals("clip") && doGlitch) {

                if (!silentSwitch.getValue()) {
                    mc.player.inventory.currentItem = oldslot;
                } else {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
                }

                mc.player.setPosition(mc.player.posX, mc.player.posY + clipHeight.getValue(), mc.player.posZ);

                doGlitch = false;

                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));

                disable();

            } else if (mode.getValue().equals("jump") && doGlitch) {

                if (!silentSwitch.getValue()) {
                    mc.player.inventory.currentItem = oldslot;
                } else {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
                }

                mc.player.jump();

                doGlitch = false;

                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));

                disable();
            }
        }
    }
}