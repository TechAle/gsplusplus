package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.PredictUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

@Module.Declaration(name = "Scaffold", category = Category.Movement)
public class Scaffold extends Module {

    ModeSetting towerMode = registerMode("Tower Mode", Arrays.asList("Motion", "Jump"), "Jump");
    DoubleSetting downSpeed = registerDouble("Down Speed", 0, 0, 0.25);
    BooleanSetting rotate = registerBoolean("Rotate", true);

    BooleanSetting showPredictSettings = registerBoolean("Predict Settings", false);

    IntegerSetting tickPredict = registerInteger("Tick Predict", 8, 0, 30, () -> showPredictSettings.getValue());

    int oldSlot;
    int targetBlockSlot;
    int timer;

    double oldTower;
    boolean doDown;
    boolean doTechaleVoodooMagic;
    boolean towering;
    boolean dontPlace;
    boolean cont;

    BlockPos belowPlayerBlock;
    BlockPos playerBlock;
    BlockPos supportBlock;

    PredictUtil.PredictSettings predictSettings;

    final Timer stuckTimer = new Timer();
    final Timer switchTimer = new Timer();
    final Timer clipTimer = new Timer();

    @Override
    public void onUpdate() {

        predictSettings = new PredictUtil.PredictSettings(tickPredict.getValue(), false, 0, 0, 0, 0, 0, 0, false, 0, false, false, false, false, false, 0, 0);

        supportBlock = new BlockPos(mc.player.posX, mc.player.posY - 2, mc.player.posZ);

        playerBlock = new BlockPos(PredictUtil.predictPlayer(mc.player, predictSettings));

        //DOWN SHIT

        if (mc.gameSettings.keyBindSprint.isKeyDown()) {
            belowPlayerBlock = playerBlock.add(0, -2, 0);
            doDown = true;
            if (!mc.player.onGround) {
                final double[] dir = MotionUtil.forward(downSpeed.getValue());
                mc.player.motionX = dir[0];
                mc.player.motionZ = dir[1];
            }

        } else {
            belowPlayerBlock = playerBlock.add(0, -1, 0);
            doDown = false;
        }


//
//            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, true));
//            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, true));
//            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, true));
//            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16, mc.player.posZ, true));
//
//            }

        targetBlockSlot = InventoryUtil.findObsidianSlot(false, false);

        if (targetBlockSlot > 9 || targetBlockSlot < 0) {
            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You dont have any obby lol");
            dontPlace = true;
        }


        for (int i = 1; i < 10; i++) {

            if (cont && mc.player.inventory.getStackInSlot(i) != ItemStack.EMPTY && mc.player.inventory.getStackInSlot(i).item instanceof ItemBlock) {

                targetBlockSlot = i;

                cont = false;

            }

        }


        if (targetBlockSlot == -1) {

            if (mc.player.posY > 0) {
                MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "No blocks in hotbar, disabling.");
                disable();
            }
        }


        if (!mc.world.getBlockState(belowPlayerBlock).getMaterial().isReplaceable()
                || mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(belowPlayerBlock)).stream().anyMatch(entity -> entity instanceof EntityPlayer && entity != mc.player)) {

            doTechaleVoodooMagic = !stuckTimer.hasReached(250, true) && !towering;

            return;
        }

        int newSlot;
        newSlot = InventoryUtil.findObsidianSlot(false, false);

        if (newSlot == -1)
            return;

        if (mc.player.inventory.currentItem != newSlot) {
            oldSlot = mc.player.inventory.currentItem;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));
            switchTimer.reset();
        }

        // place block

        if (!dontPlace) {
            if (!doDown && doTechaleVoodooMagic) {
                placeBlockPacket(belowPlayerBlock);

            } else {
                placeBlockPacket(supportBlock);
            }
        }

        if (mc.world.getBlockState(belowPlayerBlock).getMaterial().isReplaceable() && !mc.player.onGround && !towering && !doDown && !dontPlace && !mc.gameSettings.keyBindJump.isKeyDown()) {

            clutch();

        }

        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            switch (towerMode.getValue()) {
                case "Jump":
                    if (mc.player.onGround) {

                        oldTower = mc.player.posY;
                        mc.player.jump();

                    }

                    if (mc.player.motionY == 0.0030162615090425808 ) {

                        mc.player.motionY = -69; // go down faster

                    }
                    break;

                case "Motion": {

                    if (mc.player.onGround)
                        timer = 0;
                    else
                        timer++;

                    if (timer == 2 && mc.gameSettings.keyBindJump.isKeyDown()) {

                        mc.player.motionY = .42;
                        timer = 0;

                    }

                }
            }
        }
    }


    void placeBlockPacket(BlockPos pos) {

        mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));

        PlacementUtil.place(pos, EnumHand.MAIN_HAND, rotate.getValue());

        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
    }

    public void clutch() {

        BlockPos xppos = new BlockPos(mc.player.posX + 1, mc.player.posY - 1, mc.player.posZ);
        BlockPos xmpos = new BlockPos(mc.player.posX - 1, mc.player.posY - 1, mc.player.posZ);
        BlockPos zppos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ + 1);
        BlockPos zmpos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ - 1);


        if (!dontPlace) {
            placeBlockPacket(xppos);
            if (mc.world.getBlockState(xppos).getMaterial().isReplaceable()) {
                placeBlockPacket(xmpos);
                if (mc.world.getBlockState(xmpos).getMaterial().isReplaceable()) {
                    placeBlockPacket(zppos);
                    if (mc.world.getBlockState(xppos).getMaterial().isReplaceable()) {
                        placeBlockPacket(zmpos);
                    }
                }
            }
        }
    }
}
