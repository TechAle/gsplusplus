package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.exploits.RubberBand;
import com.gamesense.client.module.modules.gui.ColorMain;
import com.gamesense.client.module.modules.movement.Blink;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

/**
 * @author Doogie13
 * @since 15/07/2021
 */

@Module.Declaration(name = "FootConcrete", category = Category.Combat, priority = 101)
public class FootConcrete extends Module {

    final Timer concreteTimer = new Timer();

    ModeSetting jumpMode = registerMode("Jump Mode", Arrays.asList("Real", "Instant"), "Instant");

    BooleanSetting general = registerBoolean("General Settings", false);
    ModeSetting rubberBandMode = registerMode("Rubberband Mode", Arrays.asList("flat", "clip", "basic"), "jump");
    IntegerSetting strength = registerInteger("Strength", 1, 0, 25, () -> general.getValue() && !rubberBandMode.getValue().equalsIgnoreCase("clip"));
    BooleanSetting useBlink = registerBoolean("Use Blink", true, () -> jumpMode.getValue().equals("real") && general.getValue());
    BooleanSetting rotate = registerBoolean("rotate", true, () -> general.getValue());
    BooleanSetting positive = registerBoolean("Positive Pos", false, () -> rubberBandMode.getValue().equalsIgnoreCase("clip"));
    BooleanSetting debugpos = registerBoolean("Debug Position", false, () -> rubberBandMode.getValue().equalsIgnoreCase("clip") && general.getValue());

    BooleanSetting blocks = registerBoolean("Blocks Menu", false);
    BooleanSetting obby = registerBoolean("Obsidian", true, () -> blocks.getValue());
    BooleanSetting echest = registerBoolean("Ender Chest", true, () -> blocks.getValue());
    BooleanSetting rod = registerBoolean("End Rod", false, () -> blocks.getValue());
    BooleanSetting anvil = registerBoolean("Anvil", false, () -> blocks.getValue());
    BooleanSetting any = registerBoolean("Any", false, () -> blocks.getValue());

    boolean doGlitch;
    boolean invalidHotbar;
    int oldSlot;
    int targetBlockSlot;
    BlockPos burrowBlockPos;
    int oldslot;
    BlockPos pos;

    public void onEnable() {

        invalidHotbar = false;

        //BLINK

        if (!mc.world.isAirBlock(new BlockPos(mc.player.getPositionVector()))) {

            MessageBus.sendClientPrefixMessage("You are already clipped, disabling!");
            disable();

        }

        // FIND SLOT

        targetBlockSlot = getBlocks();

        if (targetBlockSlot == -1) {

            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "No burrow blocks in hotbar, disabling");

            invalidHotbar = true;

            disable();


            disable();

        }

        // JUMP

        if (!invalidHotbar) {

            if (mc.player.onGround) {

                burrowBlockPos = new BlockPos(mc.player.getPositionVector());

                if (mc.world.isOutsideBuildHeight(burrowBlockPos)) {
                    disable();
                    MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You are trying to burrow above build limit, disabling.");
                }

                if (jumpMode.getValue().equals("real")) {

                    if (useBlink.getValue()) {
                        ModuleManager.getModule(Blink.class).enable();
                    }
                    mc.player.jump();
                    pos = new BlockPos(mc.player.getPositionVector());

                }

                concreteTimer.reset();

            } else {

                disable();

            }
        }
    }

    public void onUpdate() {

        if (jumpMode.getValue().equalsIgnoreCase("Real")) { // should be 4 ticks since jump will go 0.42, 0.75, 1.01, 1.16
            if (mc.player.posY > Math.floor(pos.y) + 1.1) {

                targetBlockSlot = getBlocks();

                oldSlot = mc.player.inventory.currentItem;

                if (targetBlockSlot == -1)
                    disable();

                if (useBlink.getValue())
                    ModuleManager.getModule(Blink.class).disable();

                place(burrowBlockPos,targetBlockSlot,oldSlot);

                RubberBand.getPacket(debugpos.getValue(),positive.getValue());

                disable();

            }
        } else {

            targetBlockSlot = getBlocks();

            oldSlot = mc.player.inventory.currentItem;

            if (targetBlockSlot == -1) {
                disable();
            }

            PlayerUtil.fakeJump();

            place(burrowBlockPos,targetBlockSlot,oldSlot);

            RubberBand.getPacket(debugpos.getValue(),positive.getValue());

            disable();

        }

    }

    void place(BlockPos pos, int targetBlockSlot, int oldSlot) {

        mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));

        PlacementUtil.place(pos, EnumHand.MAIN_HAND, rotate.getValue(), false, false);

        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));

    }

    @Override
    protected void onDisable() {
        if (useBlink.getValue() && jumpMode.getValue().equalsIgnoreCase("Real") && ModuleManager.isModuleEnabled(Blink.class))
            ModuleManager.getModule(Blink.class).disable();
    }

    int getBlocks() {

        int current = -1;

        if (any.getValue() && InventoryUtil.findAnyBlockSlot(0, 8) != -1)
            current = InventoryUtil.findAnyBlockSlot(0, 8);

        if (anvil.getValue() && InventoryUtil.findFirstBlockSlot(Blocks.ANVIL.getClass(), 0, 8) != -1)
            current = InventoryUtil.findFirstBlockSlot(Blocks.ANVIL.getClass(), 0, 8);

        if (rod.getValue() && InventoryUtil.findFirstBlockSlot(Blocks.END_ROD.getClass(), 0, 8) != -1)
            current = InventoryUtil.findFirstBlockSlot(Blocks.END_ROD.getClass(), 0, 8);

        if (echest.getValue() && InventoryUtil.findFirstBlockSlot(Blocks.ENDER_CHEST.getClass(), 0, 8) != -1)
            current = InventoryUtil.findFirstBlockSlot(Blocks.ENDER_CHEST.getClass(), 0, 8);

        if (obby.getValue() && InventoryUtil.findFirstBlockSlot(Blocks.OBSIDIAN.getClass(), 0, 8) != -1)
            current = InventoryUtil.findFirstBlockSlot(Blocks.OBSIDIAN.getClass(), 0, 8);

        return current;

    }
}