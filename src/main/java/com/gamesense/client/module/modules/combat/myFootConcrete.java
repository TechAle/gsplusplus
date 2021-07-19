package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

@Module.Declaration(name = "My Foot Concrete", category = Category.Combat)
public class myFootConcrete extends Module {

    boolean finishOnGround,
            materials;
    BooleanSetting allowEchest = registerBoolean("Allow Echest", true);
    BooleanSetting instaActive = registerBoolean("Insta Active", true);
    BooleanSetting disactiveAfter = registerBoolean("Insta disactive", true);
    BooleanSetting alwaysActive = registerBoolean("Always Active", false);
    BooleanSetting onShift = registerBoolean("On Shift", true);
    BooleanSetting preRotate = registerBoolean("Pre Rotate", false);
    BooleanSetting cetnerPlayer = registerBoolean("Center Player", false);
    ModeSetting rubberbandMode = registerMode("Rubberband Mode", Arrays.asList("+Y", "-Y", "Add Y"), "+Y");
    IntegerSetting ym = registerInteger("Y-", -4, -64, 0);
    IntegerSetting yp = registerInteger("Y+", 128, 0, 200);
    IntegerSetting addY = registerInteger("Add Y", 10, -40, 40);

    public void onEnable() {
        initValues();

        if (instaActive.getValue())
            instaBurrow(disactiveAfter.getValue());
    }

    void initValues() {
        finishOnGround = false;
        materials = true;
    }

    public void onUpdate() {

        if ((onShift.getValue() && mc.player.isSneaking()) || alwaysActive.getValue() || finishOnGround)
            instaBurrow(disactiveAfter.getValue());

    }

    public void onDisable() {
        if (materials)
            setDisabledMessage("No materials found... FootConcrete disabled");
    }


    void instaBurrow(boolean disactive) {

        // Only when we are onGround
        if (mc.player.onGround) {

            // If above it's air, returm
            if (!(BlockUtil.getBlock(EntityUtil.getPosition(mc.player).add(0,2,0)) instanceof BlockAir))
                return;

            // Get block
            int slotBlock = InventoryUtil.findObsidianSlot(false, false);

            // If nothing found
            if (slotBlock == -1) {
                // Get echest
                if (allowEchest.getValue())
                    slotBlock = InventoryUtil.findFirstBlockSlot(Blocks.ENDER_CHEST.getClass(), 0, 8);
            }

            // If nothing found, return
            if (slotBlock == -1) {
                materials = false;
                disable();
                return;
            }

            // Get if we are on a chest
            BlockPos feet;
            boolean isEchest = BlockUtil.getBlock( (feet = EntityUtil.getPosition(mc.player))) == Blocks.ENDER_CHEST;

            // Get our posY (this is for eChest position)
            double posY = mc.player.posY + (mc.player.posY % 1 > .2 ? 1 : 0);

            // Create a new pos of us
            BlockPos pos = new BlockPos(mc.player.posX, posY, mc.player.posZ);

            // If the block is not replacable, if someone is with us or if there are no blocks, return
            if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()
                    || mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).stream().anyMatch(entity -> entity instanceof EntityPlayer && entity != mc.player)
                    || BlockUtil.getBlock(feet.add(0, -1, 0)) instanceof BlockAir) {
                if (!alwaysActive.getValue())
                    disable();
                return;
            }

            // If we are not sneaking, sneak
            boolean isSneaking = false;
            if (BlockUtil.canBeClicked(feet) && !mc.player.isSneaking()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                isSneaking = true;
            }

            // if preRotate, rotate
            if (preRotate.getValue())
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(0, 90, true));

            // Get pos
            double posX = mc.player.posX,
                    posZ = mc.player.posZ;

            // If centerPlayer, go center
            if (cetnerPlayer.getValue()) {
                Vec3d newPos = BlockUtil.getCenterOfBlock(posX, mc.player.posY, posZ);
                posX = newPos.x;
                posZ = newPos.z;
            }

            // Get slot of now
            int oldSlot = mc.player.inventory.currentItem;

            // If it's different from what we have now
            if (slotBlock != oldSlot)
                mc.player.connection.sendPacket(new CPacketHeldItemChange(slotBlock));

            // Send burrow exploit
            mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, mc.player.posY + 0.42, posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, mc.player.posY + 0.75, posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, mc.player.posY + 1.01, posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, mc.player.posY + 1.16 + (isEchest ? .1 : 0), posZ, true));

            // Start placing
            EnumFacing side = EnumFacing.DOWN;
            BlockPos neighbour = pos.offset(side);
            EnumFacing opposite = side.getOpposite();
            Vec3d vec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));

            // idk why these but PlayerControllerMP use them
            float f = (float)(vec.x - (double)pos.getX());
            float f1 = (float)(vec.y - (double)pos.getY());
            float f2 = (float)(vec.z - (double)pos.getZ());

            // Place
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                    neighbour
                    , opposite, EnumHand.MAIN_HAND, f, f1, f2));

            // Swing (it's needed for placing)
            mc.player.swingArm(EnumHand.MAIN_HAND);

            // Rubberband
            double newY = -4;
            switch (rubberbandMode.getValue()) {
                case "+Y":
                    newY = yp.getValue();
                    break;
                case "-Y":
                    newY = ym.getValue();
                    break;
                case "Add Y":
                    newY = mc.player.posY + addY.getValue();
                    break;
            }
            mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, newY, posZ, true));

            // return old slot
            if (slotBlock != oldSlot)
                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

            // Stop sneaking
            if (isSneaking)
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));

        } else finishOnGround = true;

        if (disactive)
            disable();
    }

}