package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
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
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.network.play.client.CPacketHeldItemChange;
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
    ModeSetting mode = registerMode("rubberbandMode", Arrays.asList("jump", "clip"), "jump");
    BooleanSetting useBlink = registerBoolean("useBlink", true);
    IntegerSetting clipHeight = registerInteger("clipHeight", 1, 0, 25);
    IntegerSetting clipDelay = registerInteger("rubberbandDelay", 10, 0, 50);
    IntegerSetting placeDelay = registerInteger("placeDelay", 160, 0, 250);
    BooleanSetting silentSwitch = registerBoolean("silentSwitch", true);
    BooleanSetting rotate = registerBoolean("rotate", true);

    private boolean activedOff;

    boolean doGlitch;

    float oldPitch;

    int targetBlockSlot;

    int targetBlockSlotObby;

    int targetBlockSlotEchest;

    boolean hasPlaced;

    BlockPos burrowBlockPos;

    int oldslot;

    final Timer concreteTimer = new Timer();
    final Timer glitchTimer = new Timer();

    public void onEnable() {

        if (useBlink.getValue()){
            ModuleManager.getModule(Blink.class).enable();
        }

            targetBlockSlotObby = InventoryUtil.findFirstBlockSlot(BlockObsidian.class, 0,8);

            targetBlockSlotEchest = InventoryUtil.findFirstBlockSlot(BlockEnderChest.class, 0, 8);

            if (targetBlockSlotEchest > targetBlockSlotObby){
                targetBlockSlot = targetBlockSlotObby;
            } else {
                targetBlockSlot = targetBlockSlotEchest;
            }

        if (targetBlockSlot > 8 || targetBlockSlot < 0) {
    MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getEnabledColor() + "No burrow blocks in hotbar!");
    disable();
        }

        if (mc.player.onGround) {

            burrowBlockPos = new BlockPos(Math.ceil(mc.player.posX)-1,Math.ceil(mc.player.posY-1)+1.5,Math.ceil(mc.player.posZ)-1);
            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getEnabledColor() + "Placed at " + burrowBlockPos + " with " + placeDelay +" delay!");

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

            if (useBlink.getValue()){

                ModuleManager.getModule(Blink.class).disable();
            }

            if (!silentSwitch.getValue()) {

                mc.player.inventory.currentItem = targetBlockSlot;

            }else{

                mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));

            }

            oldPitch = mc.player.rotationPitch;

            PlacementUtil.place(burrowBlockPos, EnumHand.MAIN_HAND, rotate.getValue());

            oldslot = mc.player.inventory.currentItem;

            doGlitch = true;

        }

        if (glitchTimer.getTimePassed() >= clipDelay.getValue() && mode.getValue().equals("clip") && doGlitch) {

            if (!silentSwitch.getValue()) {
                mc.player.inventory.currentItem = oldslot;
            }else{
                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
            }

            mc.player.setPosition(mc.player.posX, mc.player.posY + clipHeight.getValue(), mc.player.posZ);

            glitchTimer.reset();

            doGlitch = false;

            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));

            disable();

        } else if (mode.getValue().equals("jump") && glitchTimer.getTimePassed() >= clipDelay.getValue() && doGlitch) {

            if (!silentSwitch.getValue()) {
                mc.player.inventory.currentItem = oldslot;
            }else{
                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));
            }

            mc.player.jump();

            glitchTimer.reset();

            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));

            disable();
        }
    }
}