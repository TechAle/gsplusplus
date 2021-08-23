package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PredictUtil;
import com.gamesense.api.util.player.SpoofRotationUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import io.netty.util.internal.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.Arrays;

@Module.Declaration(name = "ScaffoldRewrite", category = Category.Movement)
public class ScaffoldRewrite extends Module {

    BooleanSetting towerSetting = registerBoolean("Tower", true);
    ModeSetting towerMode = registerMode("Tower Mode", Arrays.asList("Jump", "Motion", "None"), "Motion");
    DoubleSetting jumpMotion = registerDouble("Jump Speed", 5,0,10);
    DoubleSetting downSpeed = registerDouble("DownSpeed", 0,0,0.2);
    BooleanSetting rotate = registerBoolean("Rotate", false);

    int oldSlot;
    int newSlot;

    double oldTower;

    EntityPlayer predPlayer;

    BlockPos scaffold;
    BlockPos scaffoldSupport;
    BlockPos towerPos;
    BlockPos downPos;

    com.gamesense.api.util.misc.Timer towerTimer = new com.gamesense.api.util.misc.Timer();

    public void onUpdate() {

        oldSlot = mc.player.inventory.currentItem;

        towerPos = new BlockPos(mc.player.posX, mc.player.posY-1,mc.player.posZ);

        if (mc.gameSettings.keyBindJump.isKeyDown()) {

            scaffold = towerPos;

            if (towerSetting.getValue()){
                mc.player.motionX *= 0.3;
                mc.player.motionZ *= 0.3;
            }

        }

        predPlayer = PredictUtil.predictPlayer(mc.player, new PredictUtil.PredictSettings(2, false, 0, 0, 0, 0,0,0, false, 0, false, false, false, false));

        scaffold = predPlayer.getPosition();

        if (mc.gameSettings.keyBindSprint.isKeyDown()) scaffold.add(0,-1,0);

        // Courtesy of KAMI, this block finding algo
        newSlot = -1;
        for(int i = 0; i < 9; i++)
        {
            // filter out non-block items
            ItemStack stack =
                    mc.player.inventory.getStackInSlot(i);

            if(stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }
            Block block = ((ItemBlock) stack.getItem()).getBlock();

            // filter out non-solid blocks
            if(!Block.getBlockFromItem(stack.getItem()).getDefaultState()
                    .isFullBlock())
                continue;

            // don't use falling blocks if it'd fall
            if (((ItemBlock) stack.getItem()).getBlock() instanceof BlockFalling) {
                if (mc.world.getBlockState(scaffold).getMaterial().isReplaceable()) continue;
            }

            newSlot = i;
            break;
        }

        if(newSlot == -1) {

            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "Out of valid blocks. Disabling!");

        }

        if (towerSetting.getValue() && mc.gameSettings.keyBindJump.isKeyDown()) { // TOWER

            switch (towerMode.getValue()){

                case "Motion": { // might be broken
                    if (mc.player.onGround) {
                        mc.player.isAirBorne = true;
                        mc.player.motionY = 0.42;
                        oldTower = mc.player.posY;
                    }

                    if (mc.player.posY > oldTower + 0.42) {

                        mc.player.setPosition(mc.player.posX, Math.floor(mc.player.posY), mc.player.posZ);
                        mc.player.motionY = 0.42;
                        oldTower = mc.player.posY;
                    }

                }
                case "Jump": { // Should work in mean time

                    if (mc.player.onGround) {

                        oldTower = mc.player.posY;
                        mc.player.jump();

                    }

                    if (mc.player.posY > oldTower + 1.1) /* peak of jump is ~ 1.17ish so we will reach 1.1 */ {

                        mc.player.motionY = jumpMotion.getValue(); // go down faster

                    }

                }

            placeBlockPacket(null, towerPos, false);

            }
        }

        if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSprint.isKeyDown()) placeBlockPacket(null, scaffold, true);

        double[] dirDown = MotionUtil.forward(downSpeed.getValue());
        if (mc.gameSettings.keyBindSprint.isKeyDown()) placeBlockPacket(null,downPos,true); mc.player.motionX = dirDown[0]; mc.player.motionZ = dirDown[1];

    }
    void placeBlockPacket(EnumFacing side, BlockPos pos, boolean allowSupport) {

        mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));

        if (side == null && BlockUtil.getPlaceableSide(pos) != null) {
            side = BlockUtil.getPlaceableSide(pos);
        }
        if (side == null && allowSupport) {
            doSupport();
            return;
        }

        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d vec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));

        // idk why these but PlayerControllerMP use them
        float f = (float) (vec.x - (double) pos.getX());
        float f1 = (float) (vec.y - (double) pos.getY());
        float f2 = (float) (vec.z - (double) pos.getZ());

        PlacementUtil.place(pos,EnumHand.MAIN_HAND,rotate.getValue());
    }

    void doSupport() {

        // Round player yaw to nearest 45

        float yawRounded;

        float yaw = Math.abs(Math.round(mc.player.rotationYaw) % 360);
        float division = (int) Math.floor(yaw / 45);
        float remainder = (int) (yaw % 45);
        if (remainder < 45 / 2) {
            yawRounded = 45 * division;
        } else {
            yawRounded = 45 * (division + 1);
        }

        // TERRIBLE CODING AHEAD //

        if (mc.gameSettings.keyBindLeft.isKeyDown()) {

            yawRounded -= 45;

        }
        if (mc.gameSettings.keyBindRight.isKeyDown()) {

            yawRounded += 45;

        }

        if (yawRounded == 45) { //NE
            scaffoldSupport = scaffold;
            scaffoldSupport.add(-1,0,0);
        } else if (yawRounded == 45+90) { // SE
            scaffoldSupport = scaffold;
            scaffoldSupport.add(1,0,0);
        } else if (yawRounded == 45+90+90) { // SW
            scaffoldSupport = scaffold;
            scaffoldSupport.add(1,0,0);
        } else if (yawRounded == 45+90+90+90) { // NW
            scaffoldSupport = scaffold;
            scaffoldSupport.add(-1,0,0);
        }

        placeBlockPacket(null ,scaffoldSupport, false);

    }
}
