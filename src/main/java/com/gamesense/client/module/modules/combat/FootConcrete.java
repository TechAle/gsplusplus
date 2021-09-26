package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.HoleUtil;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Doogie13
 * @since 15/07/2021
 */

@Module.Declaration(name = "FootConcrete", category = Category.Combat)
public class FootConcrete extends Module {

    ModeSetting jumpMode = registerMode("jumpMode", Arrays.asList("real", "fake"), "real");

    BooleanSetting general = registerBoolean("General Settings", false);

    BooleanSetting smooth = registerBoolean("Smoothen", false, () -> jumpMode.getValue().equals("fake") && general.getValue());
    ModeSetting mode = registerMode("rubberbandMode", Arrays.asList("jump", "clip"), "jump", () -> jumpMode.getValue().equals("real") && general.getValue());
    BooleanSetting useBlink = registerBoolean("useBlink", true, () -> jumpMode.getValue().equals("real") && general.getValue());
    IntegerSetting placeDelay = registerInteger("placeDelay", 160, 0, 250, () -> jumpMode.getValue().equals("real") && general.getValue());
    IntegerSetting range = registerInteger("clipRange", 50, 1, 256, () -> general.getValue());
    BooleanSetting rotate = registerBoolean("rotate", true, () -> general.getValue());


    BooleanSetting blocks = registerBoolean("Blocks Menu", false);

    BooleanSetting obby = registerBoolean("Obsidian", true, () -> blocks.getValue());
    BooleanSetting echest = registerBoolean("Ender Chest", true, () -> blocks.getValue());
    BooleanSetting rod = registerBoolean("End Rod", false, () -> blocks.getValue());
    BooleanSetting anvil = registerBoolean("Anvil", false, () -> blocks.getValue());
    BooleanSetting any = registerBoolean("Any", false, () -> blocks.getValue());


    final Timer concreteTimer = new Timer();
    boolean doGlitch;
    boolean invalidHotbar;
    boolean rotation;
    float oldPitch;
    int oldSlot;
    int targetBlockSlot;
    BlockPos burrowBlockPos;
    int oldslot;

    public void onEnable() {

        if (smooth.getValue()) // so the server sends us to EXACTLY the same spot we clipped from (will test on footwalker)
            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));

        if (rotate.getValue()) {

            rotation = true;

        }

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
                } else {

                    // CIRUU BURROW (not ashamed to admit it)

                    targetBlockSlot = getBlocks();

                    oldSlot = mc.player.inventory.currentItem;

                    if (targetBlockSlot == -1) {
                        MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You are trying to burrow above build limit, disabling.");
                        disable();
                    }

                    mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));

                    PlayerUtil.fakeJump();

                    PlacementUtil.place(burrowBlockPos, EnumHand.MAIN_HAND, (rotation));

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

        if (!invalidHotbar) {

            // PLACE

            if (concreteTimer.getTimePassed() >= placeDelay.getValue()) {

                if (useBlink.getValue()) {

                    ModuleManager.getModule(Blink.class).disable();
                }

                mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));

                oldPitch = mc.player.rotationPitch;

                PlacementUtil.place(burrowBlockPos, EnumHand.MAIN_HAND, rotation);

                oldslot = mc.player.inventory.currentItem;

                doGlitch = true;

            }

            // RUBBERBAND

            if (mode.getValue().equals("clip") && doGlitch) {

                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));

                getPacket();

                doGlitch = false;

                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));

                disable();

            } else if (mode.getValue().equals("jump") && doGlitch) {

                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));


                mc.player.jump();

                doGlitch = false;

                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldslot));

                disable();
            }
        }
    }

    BlockPos getTwo() {

        HoleUtil.HoleInfo hole = HoleUtil.isHole(new BlockPos(mc.player.getPositionVector()), false, true);
        AxisAlignedBB aabb = hole.getCentre();
        Vec3d posV = aabb.getCenter();

        if (mc.world.isAirBlock(new BlockPos(posV.add(0, 0, .5)))) {

            return new BlockPos(posV.add(0, 0, .5));

        } else {

            return new BlockPos(posV.add(.5, 0, 0));

        }

    }

    private List<BlockPos> findHoles() {
        NonNullList<BlockPos> holes = NonNullList.create();

        //from old HoleFill module, really good way to do this
        List<BlockPos> blockPosList = EntityUtil.getHollowSphere(PlayerUtil.getPlayerPos(), range.getValue(), range.getValue(), true, 1, 2,2);

        for (BlockPos pos : blockPosList) {

            if (mc.world.isAirBlock(pos.add(0, 1, 0)) && mc.world.isAirBlock(pos) && pos.getDistance(((int) mc.player.posX), ((int) mc.player.posY), ((int) mc.player.posZ)) >= 2)
                holes.add(pos);

        }

        return holes;
    }

    void getPacket() {

        try {
            MessageBus.sendClientPrefixMessage(findHoles().get(findHoles().size() - 1).getX() + " " + findHoles().get(findHoles().size() - 1).getY() + " " + findHoles().get(findHoles().size() - 1).getZ() + "");
            mc.player.connection.sendPacket(new CPacketPlayer.Position(Math.floor(findHoles().get(findHoles().size() - 1).getX())+.5, findHoles().get(findHoles().size() - 1).getY(), Math.floor(findHoles().get(findHoles().size() - 1).getZ())+.5, true));
        } catch (Exception e) {

            MessageBus.sendClientPrefixMessage(e + "");
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX,mc.player.posY+1,mc.player.posZ, false));

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