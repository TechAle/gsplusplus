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

@Module.Declaration(name = "FootConcrete", category = Category.Combat)
public class FootConcrete extends Module {

    final Timer concreteTimer = new Timer();
    ModeSetting jumpMode = registerMode("jumpMode", Arrays.asList("real", "fake"), "real");
    BooleanSetting general = registerBoolean("General Settings", false);
    ModeSetting mode = registerMode("rubberbandMode", Arrays.asList("flat", "clip", "basic"), "jump");
    IntegerSetting strength = registerInteger("Strength", 1, 0, 25, () -> general.getValue() && !mode.getValue().equalsIgnoreCase("clip"));
    BooleanSetting useBlink = registerBoolean("useBlink", true, () -> jumpMode.getValue().equals("real") && general.getValue());
    BooleanSetting conserve = registerBoolean("Conserve", false, () -> general.getValue());
    IntegerSetting range = registerInteger("clipRange", 50, 1, 99, () -> general.getValue());
    BooleanSetting rotate = registerBoolean("rotate", true, () -> general.getValue());
    BooleanSetting debugpos = registerBoolean("Debug Position", false, () -> mode.getValue().equalsIgnoreCase("clip") && general.getValue());

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

        //BLINK AND TIMER

        if (useBlink.getValue()) {
            ModuleManager.getModule(Blink.class).enable();
        }

        // FIND SLOT

        targetBlockSlot = getBlocks();

        if (targetBlockSlot == -1) {

            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "No burrow blocks in hotbar, disabling");

            invalidHotbar = true;

            disable();

            if (useBlink.getValue()) {
                ModuleManager.getModule(Blink.class).disable();
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
                    pos = new BlockPos(mc.player.getPositionVector());
                } else {

                    // CIRUU BURROW (not ashamed to admit it)

                    targetBlockSlot = getBlocks();

                    oldSlot = mc.player.inventory.currentItem;

                    if (targetBlockSlot == -1) {
                        MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You are trying to burrow above build limit, disabling.");
                        disable();
                    }

                    mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));

                    PlayerUtil.fakeJump(!conserve.getValue());

                    PlacementUtil.place(burrowBlockPos, EnumHand.MAIN_HAND, rotate.getValue(), false, true);

                    getPacket();

                    mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));

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

        if (mode.getValue().equalsIgnoreCase("Real")) {

            if (mc.player.posY > Math.floor(pos.y) + 1.1) {

                targetBlockSlot = getBlocks();

                oldSlot = mc.player.inventory.currentItem;

                if (targetBlockSlot == -1)
                    disable();

                mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));

                PlacementUtil.place(burrowBlockPos, EnumHand.MAIN_HAND, rotate.getValue(), false, true);

                getPacket();

                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));

                disable();
            }

        }

    }

    private BlockPos findHoles() {

        NonNullList<BlockPos> holes = NonNullList.create();

        for (int i = -range.getValue() / 2; i < range.getValue() / 2; i++)
            if (!new BlockPos(mc.player.posX, mc.player.posY + i, mc.player.posZ).equals(new BlockPos(mc.player.getPositionVector())) && mc.world.isAirBlock(new BlockPos(mc.player.posX, mc.player.posY + i, mc.player.posZ))) {
                holes.add(new BlockPos(mc.player.posX, mc.player.posY + i, mc.player.posZ));
            }

        for (BlockPos pos : holes) {

            if (mc.world.isAirBlock(pos.add(0, 1, 0)) && mc.world.isAirBlock(pos) && pos.getDistance(((int) mc.player.posX), ((int) mc.player.posY), ((int) mc.player.posZ)) >= 2
                && Math.floor(pos.y) == Math.floor(mc.player.posY)) {
                holes.add(pos);
                break;
            }

        }

        return holes.get(0);
    }

    void getPacket() {

        if (mode.getValue().equalsIgnoreCase("Clip")) {
            BlockPos pos = findHoles();

            if (debugpos.getValue())
                MessageBus.sendClientPrefixMessage("Pos: " + (Math.floor(pos.x) + 0.5) + " " + Math.floor(pos.y) + " " + (Math.floor(pos.z) + 0.5) + " " + mc.world.isAirBlock(pos.down()));

            mc.player.connection.sendPacket(new CPacketPlayer.Position(Math.floor(pos.x) + 0.5, Math.floor(pos.y), Math.floor(pos.z) + 0.5, mc.world.isAirBlock(pos.down())));

        } else if (mode.getValue().equalsIgnoreCase("flat")) {

            for (int i = 0; i < strength.getValue(); i++)
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(RotationUtil.normalizeAngle((float) Math.random() * 1000),
                        RotationUtil.normalizeAngle((float) Math.random() * 1000), false)); // rotations to rubberband us lmao

        } else {

            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + strength.getValue(), mc.player.posZ, false));

        }
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