package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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

        if (mc.player.onGround) {

            int slotBlock = InventoryUtil.findObsidianSlot(false, false);

            if (slotBlock == -1) {
                if (allowEchest.getValue())
                    slotBlock = InventoryUtil.findFirstBlockSlot(Blocks.ENDER_CHEST.getClass(), 0, 8);
            }

            if (slotBlock == -1) {
                materials = false;
                disable();
                return;
            }

            BlockPos feet;
            boolean isEchest = BlockUtil.getBlock( (feet = EntityUtil.getPosition(mc.player))) == Blocks.ENDER_CHEST;

            double posY = mc.player.posY + (mc.player.posY % 1 > .2 ? 1 : 0);

            BlockPos pos = new BlockPos(mc.player.posX, posY, mc.player.posZ);

            if (!mc.world.getBlockState(pos).getMaterial().isReplaceable() || mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).stream().anyMatch(entity -> entity instanceof EntityPlayer && entity != mc.player)) {
                if (!alwaysActive.getValue())
                    disable();
                return;
            }

            boolean isSneaking = false;
            if (BlockUtil.canBeClicked(feet) && !mc.player.isSneaking()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                isSneaking = true;
            }

            if (preRotate.getValue())
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(0, 90, true));

            int oldSlot = mc.player.inventory.currentItem;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slotBlock));

            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16 + (isEchest ? .1 : 0), mc.player.posZ, true));

            EnumFacing side = EnumFacing.DOWN;
            BlockPos neighbour = pos.offset(side);
            EnumFacing opposite = side.getOpposite();

            mc.playerController.processRightClickBlock(
                    mc.player, mc.world, neighbour, opposite,
                    new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5)), EnumHand.MAIN_HAND);
            mc.player.swingArm(EnumHand.MAIN_HAND);


            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, -4, mc.player.posZ, true));

            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

            if (isSneaking)
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));

        } else finishOnGround = true;

        if (disactive)
            disable();
    }

}