package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.KeyBinding;
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

@Module.Declaration(name = "Foot Walker", category = Category.Combat)
public class FootWalker extends Module {

    boolean finishOnGround,
            materials,
            center,
            havePhase,
            beforeShiftJump;
    BooleanSetting allowEchest = registerBoolean("Allow Echest", true);
    BooleanSetting onlyEchest = registerBoolean("Only Echest", false, () -> allowEchest.getValue());
    BooleanSetting instaActive = registerBoolean("Insta Active", true);
    BooleanSetting disactiveAfter = registerBoolean("Insta disactive", true);
    BooleanSetting alwaysActive = registerBoolean("Always Active", false);
    BooleanSetting onShift = registerBoolean("On Shift", true);
    BooleanSetting preRotate = registerBoolean("Pre Rotate", false);
    ModeSetting rubberbandMode = registerMode("Rubberband Mode", Arrays.asList("+Y", "-Y", "Add Y", "Free Y"), "+Y");
    IntegerSetting ym = registerInteger("Y-", -4, -64, 0, () -> rubberbandMode.getValue().equals("-Y"));
    IntegerSetting yp = registerInteger("Y+", 128, 0, 200, () -> rubberbandMode.getValue().equals("+Y"));
    IntegerSetting minY = registerInteger("Min Y", 9, 0, 30,
            () -> rubberbandMode.getValue().equals("Free Y"));
    IntegerSetting yStart = registerInteger("Min Y Start", -9, 0, -20,
            () -> rubberbandMode.getValue().equals("Free Y"));
    IntegerSetting maxStartY = registerInteger("Max Start Y", 8, 5, 20,
            () -> rubberbandMode.getValue().equals("Free Y"));
    IntegerSetting maxFinishY = registerInteger("Max Finish Y", 15, 10, 40,
            () -> rubberbandMode.getValue().equals("Free Y"));
    IntegerSetting addY = registerInteger("Rub Add Y", 10, -40, 40, () -> rubberbandMode.getValue().equals("Add Y"));
    BooleanSetting onPlayer = registerBoolean("On Player", false);
    DoubleSetting rangePlayer = registerDouble("Range Player", 3, 0, 4, () -> onPlayer.getValue());
    BooleanSetting phase = registerBoolean("Phase", false);
    BooleanSetting predictPhase = registerBoolean("Predict Phase", false, () -> phase.getValue());
    ModeSetting phaseRubberband = registerMode("Phase Rubberband", Arrays.asList("Y+", "Y-", "Y0", "AddY", "X", "Z", "XZ"), "Y+",
            () -> phase.getValue());
    IntegerSetting phaseAddY = registerInteger("Phase Add Y", 40, -40, 40,
            () -> phaseRubberband.getValue().equals("AddY") && phase.getValue());
    BooleanSetting scaffold = registerBoolean("Scaffold", false);
    BooleanSetting shiftJump = registerBoolean("Shift Jump", false);
    BooleanSetting safeMode = registerBoolean("Safe Mode", false);
    BooleanSetting normalSwitch = registerBoolean("Normal Switch", false);
    BooleanSetting switchBack = registerBoolean("Switch Back", false, () -> normalSwitch.getValue());


    public void onEnable() {
        // Reset values
        initValues();

        // If insta active, burrow
        if (instaActive.getValue())
            instaBurrow(disactiveAfter.getValue());
    }

    void initValues() {
        finishOnGround = center = havePhase = beforeShiftJump = false;
        materials = true;
    }

    public void onUpdate() {

        // If we have to phase
        if (havePhase) {
            // Phase
            doPhase();
            // Reset
            havePhase = false;
            if (disactiveAfter.getValue())
                disable();
        }

        // If something here is on
        if ((onShift.getValue() && mc.player.isSneaking()) || alwaysActive.getValue() || finishOnGround ||
                (onPlayer.getValue() && PlayerUtil.findClosestTarget(rangePlayer.getValue(), null) != null))
            // Active
            instaBurrow(disactiveAfter.getValue());

        if ( shiftJump.getValue() && mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown()) {
            if (!beforeShiftJump) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, Math.floor(mc.player.posY) + 1, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                mc.player.setPosition(mc.player.posX, Math.floor(mc.player.posY) + 1, mc.player.posZ);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
            }
        }

        if (center) {
            PlayerUtil.centerPlayer(BlockUtil.getCenterOfBlock(mc.player.posX, mc.player.posY, mc.player.posZ));
            center = false;
        }

    }

    public void onDisable() {
        if (materials)
            setDisabledMessage("No materials found... FootConcrete disabled");
    }

    void instaBurrow(boolean disactive) {

        // Only when we are onGround
        if (mc.player.onGround) {

            // Get block
            int slotBlock = onlyEchest.getValue() ? -1 : InventoryUtil.findObsidianSlot(false, false);

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

            // Get our posY (this is for eChest position)
            double posY = mc.player.posY % 1 >= .5 ? Math.round(mc.player.posY) : mc.player.posY;

            // Create a new pos of us
            BlockPos pos = new BlockPos(mc.player.posX, posY, mc.player.posZ);

            // If the block is not replacable, if someone is with us or if there are no blocks, return
            if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()
                    || mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).stream().anyMatch(entity -> entity instanceof EntityPlayer && entity != mc.player)) {
                if (!alwaysActive.getValue())
                    disable();
                return;
            }

            boolean scaf = false;
            if (BlockUtil.getBlock(pos.add(0, -1, 0)) instanceof BlockAir) {
                if (scaffold.getValue())
                    scaf = true;
                else {
                    if (!alwaysActive.getValue())
                        disable();
                    return;
                }
            }

            // If above it's air, returm
            if (!(BlockUtil.getBlock(pos.add(0, 2, 0)) instanceof BlockAir))
                return;

            // if preRotate, rotate
            if (preRotate.getValue())
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(0, 90, true));

            // If Free Y, lets get the y where we are going to get teleported
            int y = Integer.MIN_VALUE;
            if (rubberbandMode.getValue().equals("Free Y")) {
                boolean bottom = BlockUtil.getBlock(mc.player.posX, mc.player.posY - minY.getValue(), mc.player.posZ) instanceof BlockAir;
                for(int i = minY.getValue() - 1; i > -1; i--) {
                    boolean air = BlockUtil.getBlock(mc.player.posX, mc.player.posY - i, mc.player.posZ) instanceof BlockAir;
                    if (mc.player.posY - i < yStart.getValue())
                        continue;
                    if (bottom && air) {
                        y = -i;
                        break;
                    }
                    else bottom = air;
                }

                if (y == Integer.MIN_VALUE) {
                    bottom = BlockUtil.getBlock(mc.player.posX, mc.player.posY + maxStartY.getValue(), mc.player.posZ) instanceof BlockAir;
                    for(int i = maxStartY.getValue() + 1; i < maxFinishY.getValue(); i++) {
                        boolean air = BlockUtil.getBlock(mc.player.posX, mc.player.posY + y, mc.player.posZ) instanceof BlockAir;
                        if (bottom && air) {
                            y = i;
                            break;
                        }
                        else bottom = air;
                    }
                    if (y == Integer.MIN_VALUE)
                        return;
                }

            }

            // Get pos
            double posX = mc.player.posX,
                    posZ = mc.player.posZ;
            Vec3d newPos = BlockUtil.getCenterOfBlock(posX, mc.player.posY, posZ);
            // Get slot of now
            int oldSlot = mc.player.inventory.currentItem;
            if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox()).isEmpty() || safeMode.getValue()) {
                double distance;
                if (( distance = mc.player.getDistanceSq(newPos.x, mc.player.posY, newPos.z)) > .1) {

                    if (scaf) {

                        if (slotBlock != oldSlot) {
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(slotBlock));
                            if (normalSwitch.getValue())
                                mc.player.inventory.currentItem = slotBlock;
                        }

                        placeBlockPacket(null, pos.add(0, -1, 0));
                    }

                    finishOnGround = true;
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                    double  newX = posX + (newPos.x - posX) / 2,
                            newZ = posZ + (newPos.z - posZ) / 2;
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(newX, mc.player.posY, newZ, true));
                    mc.player.setPosition(newX, mc.player.posY, newZ);

                    if (slotBlock != oldSlot)
                        if (normalSwitch.getValue()) {
                            if (switchBack.getValue())
                                mc.player.inventory.currentItem = oldSlot;
                        } else
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));

                    return;
                } else if (safeMode.getValue() && distance > 0.05) {

                    if (scaf) {

                        if (slotBlock != oldSlot) {
                            if (normalSwitch.getValue()) {
                                mc.player.inventory.currentItem = slotBlock;
                            }
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(slotBlock));
                        }

                        placeBlockPacket(null, pos.add(0, -1, 0));
                    }


                    center = true;

                    if (slotBlock != oldSlot)
                        if (normalSwitch.getValue()) {
                            if (switchBack.getValue())
                                mc.player.inventory.currentItem = oldSlot;
                        } else
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));

                    return;
                }
            }

            posX = newPos.x;
            posZ = newPos.z;


            // If we are not sneaking, sneak
            boolean isSneaking = false;
            if (BlockUtil.canBeClicked(pos.add(0, -1, 0)) && !mc.player.isSneaking()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                isSneaking = true;
            }

            // If it's different from what we have now
            if (slotBlock != oldSlot) {
                if (normalSwitch.getValue()) {
                    mc.player.inventory.currentItem = slotBlock;
                }
                mc.player.connection.sendPacket(new CPacketHeldItemChange(slotBlock));}

            if (scaf) {
                placeBlockPacket(null, pos.add(0, -1, 0));
            }

            // Send burrow exploit
            mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, mc.player.posY + 0.42, posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, mc.player.posY + 0.75, posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, mc.player.posY + 1.01, posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, mc.player.posY + 1.16, posZ, true));

            // Start placing
            placeBlockPacket(EnumFacing.DOWN, pos);

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
                    newY = posY+ addY.getValue();
                    break;
                case "Free Y":
                    newY = posY + y;
                    break;
            }
            mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, newY, posZ, true));

            // return old slot
            if (slotBlock != oldSlot)
                if (normalSwitch.getValue()) {
                    if (switchBack.getValue())
                        mc.player.inventory.currentItem = oldSlot;
                } else
                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

            // Stop sneaking
            if (isSneaking)
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));

            // If phase, active
            if (phase.getValue()) {
                if (predictPhase.getValue())
                    doPhase();
                havePhase = true;
            }

        } else finishOnGround = true;

        if (disactive && !phase.getValue())
            disable();
    }

    boolean placeBlockPacket(EnumFacing side, BlockPos pos) {

        if (side == null) {
            side = BlockUtil.getPlaceableSide(pos);
        }
        if (side == null)
            return false;
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
        return true;
    }

    // This is kinda phobos
    void doPhase() {
        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 0.03125, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
        switch (phaseRubberband.getValue()) {
            case "Y+":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, 1000, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                break;
            case "Y-":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, -1000, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                break;
            case "Y0":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, 0, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                break;
            case "AddY":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + phaseAddY.getValue(), mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                break;
            case "X":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + 75, mc.player.posY, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                break;
            case "Z":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY, mc.player.posZ + 75, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                break;
            case "XZ":
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX + 75, mc.player.posY, mc.player.posZ + 75, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
                break;
        }

    }

}