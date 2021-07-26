package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "Scaffold", category = Category.Movement)
public class Scaffold extends Module {
    IntegerSetting extend = registerInteger("extendMS", 10, 0, 1000);

    int targetBlockSlot;
    int oldSlot;
    int direction;

    boolean doSupport;

    BlockPos belowPlayer;
    BlockPos supportBlock;


    @Override
    public void onUpdate() {

        direction = (MathHelper.floor((double) (mc.player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7);


/*        if (mc.gameSettings.keyBindJump.isPressed()) {

            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16, mc.player.posZ, true));

            }*/

        targetBlockSlot =

                oldSlot = mc.player.inventory.currentItem;

        if (targetBlockSlot == -1) {
            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You dont have any obby lol");
            disable();
        }

        mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));

        switch (MathHelper.floor((double) (mc.player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7) {
            case 0:
                belowPlayer = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ + 1);
                doSupport = false;

            case 1:
                belowPlayer = new BlockPos(mc.player.posX - 1, mc.player.posY - 1, mc.player.posZ + 1);
                doSupport = true;
                supportBlock = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ + 1);

            case 2:
                belowPlayer = new BlockPos(mc.player.posX - 1, mc.player.posY - 1, mc.player.posZ);
                doSupport = false;

            case 3:
                belowPlayer = new BlockPos(mc.player.posX - 1, mc.player.posY - 1, mc.player.posZ - 1);
                doSupport = true;
                supportBlock = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ - 1);

            case 4:
                belowPlayer = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ - 1);
                doSupport = false;

            case 5:
                belowPlayer = new BlockPos(mc.player.posX + 1, mc.player.posY - 1, mc.player.posZ - 1);
                doSupport = true;
                supportBlock = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ - 1);


            case 6:
                belowPlayer = new BlockPos(mc.player.posX + 1, mc.player.posY - 1, mc.player.posZ);
                doSupport = false;

            case 7:
                belowPlayer = new BlockPos(mc.player.posX + 1, mc.player.posY - 1, mc.player.posZ + 1);
                doSupport = true;
                supportBlock = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ + 1);
        }

            /*
        switch (MathHelper.floor((double) (mc.player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7) {
            case 0:
                return "+Z";
            case 1:
                return "-X +Z";
            case 2:
                return "-X";
            case 3:
                return "-X -Z";
            case 4:
                return "-Z";
            case 5:
                return "+X -Z";
            case 6:
                return "+X";
            case 7:
                return "+X +Z";
        }
        }*/

        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

        if (!mc.world.getBlockState(belowPlayer).getMaterial().isReplaceable()
                || mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(belowPlayer)).stream().anyMatch(entity -> entity instanceof EntityPlayer && entity != mc.player)) {
            return;
        }
        int newSlot;
        newSlot = InventoryUtil.findObsidianSlot(false, false);

        if (newSlot == -1)
            return;

        int oldSlot;
        oldSlot = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = newSlot;

        // place block

        if (doSupport) {
            placeBlockPacket(null, supportBlock);
        }

        placeBlockPacket(null, belowPlayer);

        mc.player.inventory.currentItem = oldSlot;
    }


    void placeBlockPacket(EnumFacing side, BlockPos pos) {

        if (side == null) {
            side = BlockUtil.getPlaceableSide(pos);
        }
        if (side == null)
            return;
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d vec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));

        // idk why these but PlayerControllerMP use them
        float f = (float) (vec.x - (double) pos.getX());
        float f1 = (float) (vec.y - (double) pos.getY());
        float f2 = (float) (vec.z - (double) pos.getZ());

        // Place
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                neighbour
                , opposite, EnumHand.MAIN_HAND, f, f1, f2));

        // Swing (it's needed for placing)
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }
}

