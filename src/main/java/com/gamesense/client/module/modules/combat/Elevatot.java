package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.BlockChangeEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.misc.AutoGG;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gamesense.api.util.player.SpoofRotationUtil.ROTATION_UTIL;

/**
 * @Author TechAle
 * Most things are ported from PistonCrystal (guess why lol)
 */

/*
    Remove antiweakness [done]
    Place everything [done]
    Add working rotatione [done]
    Fix bug crash [done]
    Add ondisable
    Add event packet destroyed
    Break torch when placed
    Place torch when broke
 */

@Module.Declaration(name = "Elevatot", category = Category.Combat)
public class Elevatot extends Module {

    ModeSetting target = registerMode("Target", Arrays.asList("Nearest", "Looking"), "Nearest");
    ModeSetting placeMode = registerMode("Place", Arrays.asList("Torch", "Block", "Both"), "Torch");
    IntegerSetting supportDelay = registerInteger("Support Delay", 0, 0, 8);
    IntegerSetting pistonDelay = registerInteger("Piston Delay", 0, 0, 8);
    IntegerSetting redstoneDelay = registerInteger("Redstone Delay", 0, 0, 8);
    IntegerSetting blocksPerTick = registerInteger("Blocks per Tick", 4, 1, 8);
    IntegerSetting tickBreakRedstone = registerInteger("Tick Break Redstone", 2, 0, 10);
    DoubleSetting enemyRange = registerDouble("Range", 4.9, 0, 6);
    BooleanSetting debugMode = registerBoolean("Debug Mode", false);
    BooleanSetting trapMode = registerBoolean("Trap Mode", false);
    BooleanSetting rotate = registerBoolean("Rotate", false);
    BooleanSetting forceBurrow = registerBoolean("Force Burrow", false);

    EntityPlayer aimTarget;

    double[][] sur_block;
    double[] enemyCoordsDouble;

    int[][] disp_surblock = {
            {1, 0, 0},
            {-1, 0, 0},
            {0, 0, 1},
            {0, 0, -1}
    };
    int[] slot_mat,
                  enemyCoordsInt,
                  meCoordsInt;

    int lastStage,
        blockPlaced,
        tickPassedRedstone,
        delayTimeTicks;

    boolean redstoneBlockMode,
            enoughSpace,
            isHole,
            noMaterials,
            redstoneAbovePiston,
            isSneaking,
            redstonePlaced;

    // Class for the structure
    class structureTemp {
        public double distance;
        public int supportBlock;
        public List<Vec3d> to_place;
        public int direction;
        public float offsetX;
        public float offsetY;
        public float offsetZ;
        public int position;

        public structureTemp(double distance, int supportBlock, List<Vec3d> to_place, int position) {
            this.distance = distance;
            this.supportBlock = supportBlock;
            this.to_place = to_place;
            this.direction = -1;
            this.position = position;
        }

        public void replaceValues(double distance, int supportBlock, List<Vec3d> to_place, int direction, float offsetX, float offsetZ, float offsetY, int position) {
            this.distance = distance;
            this.supportBlock = supportBlock;
            this.to_place = to_place;
            this.direction = direction;
            this.offsetX = offsetX;
            this.offsetZ = offsetZ;
            this.offsetY = offsetY;
            this.position = position;
        }
    }

    structureTemp toPlace;

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<BlockChangeEvent> blockChangeEventListener = new Listener<>(event -> {

        if (mc.player == null || mc.world == null) return;

        if (event.getBlock() == null || event.getPosition() == null) return;
        BlockPos temp;
        if (event.getPosition().getX() == (temp = compactBlockPos(2)).getX()
                && event.getPosition().getY() == temp.getY()
                && event.getPosition().getZ() == temp.getZ() ) {
            if (event.getBlock() instanceof BlockRedstoneTorch) {
                if (tickBreakRedstone.getValue() == 0) {
                    breakBlock(temp);
                    lastStage = 2;
                } else {
                    lastStage = 3;
                }
            } else if (event.getBlock() instanceof BlockAir) {
                if (redstoneDelay.getValue() == 0) {
                    placeBlock(temp, 0, 0, 0, false, false, slot_mat[2], -1);
                    mc.world.setBlockState(temp, Blocks.REDSTONE_TORCH.getDefaultState());
                }
            }
        }


    });

    // Algo for breaking a block
    private void breakBlock(BlockPos pos) {
        // If we have a redstone block
        if (redstoneBlockMode) {
            // Switch to the pick
            mc.player.inventory.currentItem = slot_mat[3];
        }
        EnumFacing side = BlockUtil.getPlaceableSide(pos);
        if (side != null) {
            // If rotate, look at the redstone torch
            if (rotate.getValue()) {
                BlockPos neighbour = pos.offset(side);
                EnumFacing opposite = side.getOpposite();
                Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
                BlockUtil.faceVectorPacketInstant(hitVec, true);
                /*
                if (forceRotation.getValue()) {
                    lastHitVec = hitVec;
                }*/
            }
            // Destroy it
            mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.player.connection.sendPacket(new CPacketPlayerDigging(
                    CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, side
            ));
            mc.player.connection.sendPacket(new CPacketPlayerDigging(
                    CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, side
            ));
        }
    }

    @Override
    public void onEnable() {
        ROTATION_UTIL.onEnable();

        initValues();

        // Get Target
        if (getAimTarget())
            return;

        playerChecks();

    }

    @Override
    public void onDisable() {
        if (isSneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            isSneaking = false;
        }

        String output = "";
        String materialsNeeded = "";

        // No target found
        if (aimTarget == null) {
            output = "No target found...";
        } else
            if (!isHole)
                output = "The enemy is not in a hole...";
            else if (!enoughSpace)
                output = "Not enough space...";
            else if (noMaterials) {
                output = "No materials detected...";
                materialsNeeded = getMissingMaterials();
            }

        // Output in chat
        setDisabledMessage(output + "Elevatot turned OFF!");
        if (!materialsNeeded.equals(""))
            setDisabledMessage("Materials missing:" + materialsNeeded);


    }

    String getMissingMaterials() {
        /*
			// I use this as a remind to which index refers to what
			0 => obsidian
			1 => piston
			2 => redstone
			3 => pick
		 */

        StringBuilder output = new StringBuilder();

        if (slot_mat[0] == -1)
            output.append(" Obsidian");
        if (slot_mat[1] == -1)
            output.append(" Piston");
        if (slot_mat[2] == -1)
            output.append(" Redstone");
        if (slot_mat[3] == -1 && redstoneBlockMode)
            output.append(" Pick");
        if (slot_mat[4] == -1 && forceBurrow.getValue())
            output.append(" Skull");

        return output.toString();
    }

    @Override
    public void onUpdate() {
        // If no mc.player
        if (mc.player == null) {
            disable();
            return;
        }

        /*
            -1 (default) = When started, no wait, why would you lmao
            0 = Before there was a place support
            1 = Before he placed the piston
            2 = Before he placed the redstone torch
            3 = Before breaking the block
        */
        int toWait;
        switch (lastStage) {
            case 0:
                toWait = supportDelay.getValue();
                break;
            case 1:
                toWait = pistonDelay.getValue();
                break;
            case 2:
                toWait = redstoneDelay.getValue();
                break;
            case 3:
                toWait = tickBreakRedstone.getValue();
                break;
            default:
                toWait = 0;
                break;
        }

        if (delayTimeTicks < toWait) {
            delayTimeTicks++;
            return;
        }

        // Enable rotation spoof
        ROTATION_UTIL.shouldSpoofAngles(true);

        // Check if something is not ok
        if (enemyCoordsDouble == null || aimTarget == null) {
            if (aimTarget == null) {
                aimTarget = PlayerUtil.findLookingPlayer(enemyRange.getValue());
                if (aimTarget != null) {

                    if (ModuleManager.isModuleEnabled(AutoGG.class)) {
                        AutoGG.INSTANCE.addTargetedPlayer(aimTarget.getName());
                    }

                    playerChecks();
                }
            }
            return;
        }
        if (checkVariable())
            return;
        /*
            First we have to place every supports blocks.
            Then, we have to do this:
            Check if the piston exists, if not, place it.
            Check if the redstone torch exists, if not, place it.
         */

        // First place support blocks
        if (placeSupport()) {
            BlockPos temp;
            // Check for the piston
            if (BlockUtil.getBlock(temp = compactBlockPos(1)) instanceof BlockAir) {
                placeBlock(temp, toPlace.offsetX, toPlace.offsetY, toPlace.offsetZ, false, true, slot_mat[1], toPlace.position);
                // Check if we can continue
                if (continueBlock()) {
                    lastStage = 1;
                    return;
                }
            }
            // Check for the redstone
            if (BlockUtil.getBlock(temp = compactBlockPos(2)) instanceof BlockAir) {
                placeBlock(temp, 0, 0, 0, false, false, slot_mat[2], -1);
                // Check if we can continue
                lastStage = 2;
                return;
            }

            // Break the redstone
            if (lastStage == 3) {
                breakBlock(compactBlockPos(2));
                lastStage = 2;
                return;
            }


        }




    }

    boolean continueBlock() {
        return ++blockPlaced == blocksPerTick.getValue();
    }

    boolean placeSupport() {

        // If we have something to place
        if (toPlace.supportBlock > 0) {

            if (forceBurrow.getValue() && BlockUtil.getBlock(aimTarget.getPosition()) instanceof BlockAir ) {
                boolean temp = redstoneAbovePiston;
                redstoneAbovePiston = true;
                placeBlock(aimTarget.getPosition(), 0, 0, 0, true, false, slot_mat[4], -1);
                redstoneAbovePiston = temp;
                if (continueBlock()) {
                    lastStage = 0;
                    return false;
                }
            }

            // Iterate until the finish
            for(int i = 0; i < toPlace.supportBlock; i++) {

                // Get the coordinates of that block
                BlockPos targetPos = getTargetPos(i);

                // If it's air
                if (BlockUtil.getBlock(targetPos) instanceof BlockAir) {
                    placeBlock(targetPos, 0, 0, 0, false, false, slot_mat[0], -1);

                    // If we reached the limit
                    if (continueBlock()) {
                        // Stop
                        lastStage = 0;
                        return false;
                    }

                }

            }


        }

        return true;
    }

    final ArrayList<EnumFacing> exd = new ArrayList<EnumFacing>() {
        {
            add(EnumFacing.DOWN);
        }
    };

    boolean placeBlock(BlockPos pos, double offsetX, double offsetY, double offsetZ, boolean redstone, boolean piston, int slot, int position) {
        // Get the block
        Block block = mc.world.getBlockState(pos).getBlock();
        // Get all sides
        EnumFacing side;
        if (redstone && redstoneAbovePiston) {
            side = BlockUtil.getPlaceableSideExlude(pos, exd);
        } else side = BlockUtil.getPlaceableSide(pos);

        // If there is a solid block
        if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
            return false;
        }
        // If we cannot find any side
        if (side == null) {
            return false;
        }

        // Get position of the side
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();

        // If that block can be clicked
        if (!BlockUtil.canBeClicked(neighbour)) {
            return false;
        }

        // Get the position where we are gonna click
        Vec3d hitVec = new Vec3d(neighbour).add(0.5 + offsetX, 0.5, 0.5 + offsetZ).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();

        //try {

        if (mc.player.inventory.getStackInSlot(slot) != ItemStack.EMPTY) {
            if (mc.player.inventory.currentItem != slot) {
                // I dont wanna people getting kicked :P
                if (slot == -1) {
                    noMaterials = true;
                    return  false;
                }
                mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                mc.player.inventory.currentItem = slot;
            }
        }

        /*}catch (Exception e) {
            PistonCrystal.printDebug("Fatal Error during the creation of the structure. Please, report this bug in the discor's server", true);
            final Logger LOGGER = LogManager.getLogger("GameSense");
            LOGGER.error("[PistonCrystal] error during the creation of the structure.");
            if (e.getMessage() != null)
                LOGGER.error("[PistonCrystal] error message: " + e.getClass().getName() + " " + e.getMessage());
            else
                LOGGER.error("[PistonCrystal] cannot find the cause");
            int i5 = 0;

            if (e.getStackTrace().length != 0) {
                LOGGER.error("[PistonCrystal] StackTrace Start");
                for (StackTraceElement errorMess : e.getStackTrace()) {
                    LOGGER.error("[PistonCrystal] " + errorMess.toString());
                }
                LOGGER.error("[PistonCrystal] StackTrace End");
            }
            disable();
        }*/

        if (!isSneaking && BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock)) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            isSneaking = true;
        }

        // If rotate
        if (rotate.getValue()) {
            // Look
            BlockUtil.faceVectorPacketInstant(hitVec, true);
        }
        // Else, we have still to rotate for the piston
        else if (piston) {
            switch (position) {
                case 0:
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(0, 0, mc.player.onGround));
                    break;
                case 1:
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(180, 0, mc.player.onGround));
                    break;
                case 2:
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(-90, 0, mc.player.onGround));
                    break;
                default:
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(90, 0, mc.player.onGround));
                    break;
            }
        }

        // Place the block
        mc.playerController.processRightClickBlock(mc.player, mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);

        return true;

    }

    // Given a index of a block, get the target position (this is used for support blocks)
    BlockPos getTargetPos(int idx) {
        BlockPos offsetPos = new BlockPos(toPlace.to_place.get(idx));
        return new BlockPos(enemyCoordsDouble[0] + offsetPos.getX(), enemyCoordsDouble[1] + offsetPos.getY(), enemyCoordsDouble[2] + offsetPos.getZ());
    }

    // Given a step, return the absolute block position
    public BlockPos compactBlockPos(int step) {
        // Get enemy's relative position of the block
        BlockPos offsetPos = new BlockPos(toPlace.to_place.get(toPlace.supportBlock + step - 1));
        // Get absolute position and return
        return new BlockPos(enemyCoordsDouble[0] + offsetPos.getX(), enemyCoordsDouble[1] + offsetPos.getY(), enemyCoordsDouble[2] + offsetPos.getZ());

    }

    // Check if we have to disable
    boolean checkVariable() {
        // If something went wrong
        if (noMaterials || !isHole || !enoughSpace) {
            disable();
            return true;
        }
        return false;
    }

    void initValues() {
        sur_block = new double[4][3];
        slot_mat = new int[] {
                -1, -1, -1, -1, -1
        };
        enemyCoordsDouble = new double[3];
        toPlace = new structureTemp(0, 0, null, -1);

        redstoneBlockMode = noMaterials = redstonePlaced = false;

        isHole = true;

        aimTarget = null;

        lastStage = -1;

        delayTimeTicks = 0;

    }

    // Get all the materials
    boolean getMaterialsSlot() {
		/*
			// I use this as a remind to which index refers to what
			0 => obsidian
			1 => piston
			2 => redstone
			3 => pick
			4 => skull
		 */

        if (placeMode.getValue().equals("Block"))
            redstoneBlockMode = true;


        // Iterate for all the inventory
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            // If there is no block
            if (stack == ItemStack.EMPTY) {
                continue;
            } else
            // If Pick
            if (stack.getItem() instanceof ItemPickaxe) {
                slot_mat[3] = i;
            } else if (forceBurrow.getValue() && stack.getItem() instanceof ItemSkull) {
                slot_mat[4] = i;
            }
            if (stack.getItem() instanceof ItemBlock) {

                // If yes, get the block
                Block block = ((ItemBlock) stack.getItem()).getBlock();

                // Obsidian
                if (block instanceof BlockObsidian) {
                    slot_mat[0] = i;
                } else
                    // PistonBlock
                    if (block instanceof BlockPistonBase) {
                        slot_mat[1] = i;
                    } else
                        // RedstoneTorch / RedstoneBlock
                        if (!placeMode.getValue().equals("Block") && block instanceof BlockRedstoneTorch) {
                            slot_mat[2] = i;
                            redstoneBlockMode = false;
                        } else if (!placeMode.getValue().equals("Torch") && block.translationKey.equals("blockRedstone")) {
                            slot_mat[2] = i;
                            redstoneBlockMode = true;
                        }
            }
        }

        // Count what we found
        int count = 0;
        for (int val : slot_mat) {
            if (val != -1)
                count++;
        }

        if (debugMode.getValue())
            PistonCrystal.printDebug(String.format("%d %d %d %d", slot_mat[0], slot_mat[1], slot_mat[2], slot_mat[3]), false);

        // If we have everything we need, return true
        return count >= 3 + (redstoneBlockMode ? 1 : 0) + (forceBurrow.getValue() ? 1 : 0);

    }

    // Get target function
    boolean getAimTarget() {
        /// Get aimTarget
        // If nearest, get it
        if (target.getValue().equals("Nearest"))
            aimTarget = PlayerUtil.findClosestTarget(enemyRange.getValue(), aimTarget);
            // if looking
        else
            aimTarget = PlayerUtil.findLookingPlayer(enemyRange.getValue());

        // If we didnt found a target
        if (aimTarget == null || !target.getValue().equals("Looking")) {
            // if it's not looking and we didnt found a target
            if (!target.getValue().equals("Looking") && aimTarget == null)
                disable();
            // If not found a target
            return aimTarget == null;
        }
        return false;
    }

    // Make some checks for startup
    void playerChecks() {
        // Get all the materials
        if (getMaterialsSlot()) {
            // check if the enemy is in a hole
            if (is_in_hole()) {
                // Get enemy coordinates
                enemyCoordsDouble = new double[]{aimTarget.posX, aimTarget.posY, aimTarget.posZ};
                enemyCoordsInt = new int[]{(int) enemyCoordsDouble[0], (int) enemyCoordsDouble[1], (int) enemyCoordsDouble[2]};
                // Get me coordinates
                meCoordsInt = new int[]{(int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ};
                // Start choosing where to place what
                enoughSpace = createStructure();
                // Is not in a hoke
            } else {
                isHole = false;
            }
            // No materials
        } else noMaterials = true;
    }

    boolean is_in_hole() {
        sur_block = new double[][]{
                {aimTarget.posX + 1, aimTarget.posY, aimTarget.posZ},
                {aimTarget.posX - 1, aimTarget.posY, aimTarget.posZ},
                {aimTarget.posX, aimTarget.posY, aimTarget.posZ + 1},
                {aimTarget.posX, aimTarget.posY, aimTarget.posZ - 1}
        };

        // Check if the guy is in a hole
        return HoleUtil.isHole(EntityUtil.getPosition(aimTarget), true, true).getType() != HoleUtil.HoleType.NONE;
    }

    boolean createStructure() {

        /// Create default start value
        // Structure default
        structureTemp addedStructure = new structureTemp(Double.MAX_VALUE, 0, null, -1);
        // Distance we are going to find
        double distanceNowCrystal;
        // Since they may happens some errors that i did not expect, i use a try-catch
        //try {

        // First check, h check.
        // Iterate for the surround (why is the foreach iterate in a random way in elevatot LMAO
        for(int i = 0; i < 4; i++) {

            /// Note:
            /*
                Abs = Absolute
                Rel = Relative
             */
            /*
                Since they are a lot of if, i prefer keeping them
                separated but, also, on the same tab.
                I'll use "continue"
             */
            /// Piston Coordinates ///
            // Init + Get
            double[] pistonCoordsAbs = {sur_block[i][0], sur_block[i][1] + 1, sur_block[i][2]};
            int[] pistonCoordsRel = {disp_surblock[i][0], disp_surblock[i][1] + 1, disp_surblock[i][2]};

            /// Crystal Position Checks ///
            // Check, first of all, the distance
            if (!((distanceNowCrystal = mc.player.getDistance(pistonCoordsAbs[0], pistonCoordsAbs[1], pistonCoordsAbs[2])) < addedStructure.distance))
                continue;

            // Check if the block is free
            if (!(BlockUtil.getBlock(pistonCoordsAbs[0], pistonCoordsAbs[1], pistonCoordsAbs[2]) instanceof BlockAir) && !(BlockUtil.getBlock(pistonCoordsAbs[0], pistonCoordsAbs[1], pistonCoordsAbs[2]) instanceof BlockPistonBase))
                continue;

            /// Redstone ///
            // Check for the redstone
            /// Redstone Coordinates
            double[] redstoneCoordsAbs = new double[3];
            int[] redstoneCoordsRel = new int[3];
            double minFound = 1000;
            double minNow = -1;
            boolean foundOne = false;
            // Iterate for all 4 positions
            for (int[] pos : disp_surblock) {
                // Get coordinates
                double[] torchCoords = new double[]{pistonCoordsAbs[0] + pos[0], pistonCoordsAbs[1], pistonCoordsAbs[2] + pos[2]};
                // If it's min of what we have now
                if ((minNow = mc.player.getDistance(torchCoords[0], torchCoords[1], torchCoords[2])) > minFound)
                    continue;

                // Check if: Someone is here, if it's air
                if (PistonCrystal.someoneInCoords(torchCoords[0], torchCoords[2])
                        || !(BlockUtil.getBlock(torchCoords[0], torchCoords[1], torchCoords[2]) instanceof BlockRedstoneTorch
                        || BlockUtil.getBlock(torchCoords[0], torchCoords[1], torchCoords[2]) instanceof BlockAir)) {
                    continue;
                }

                redstoneCoordsAbs = new double[]{torchCoords[0], torchCoords[1], torchCoords[2]};
                redstoneCoordsRel = new int[]{pistonCoordsRel[0] + pos[0], pistonCoordsRel[1], pistonCoordsRel[2] + pos[2]};
                foundOne = true;
                minFound = minNow;
            }

            redstoneAbovePiston = false;
            if (!foundOne) {
                // Lets check if we can place it on top of the piston
                if (!redstoneBlockMode && BlockUtil.getBlock(pistonCoordsAbs[0], pistonCoordsAbs[1] + 1, pistonCoordsAbs[2]) instanceof BlockAir) {
                    redstoneCoordsAbs = new double[]{pistonCoordsAbs[0], pistonCoordsAbs[1] + 1, pistonCoordsAbs[2]};
                    redstoneCoordsRel = new int[]{pistonCoordsRel[0], pistonCoordsRel[1] + 1, pistonCoordsRel[2]};
                    redstoneAbovePiston = true;
                }
                if (!redstoneAbovePiston)
                    continue;
            }

            /// Create the structure
            // Skeleton
            List<Vec3d> toPlaceTemp = new ArrayList<>();
            int supportBlock = 0;

            // If we are placing a redstone torch
            if (!redstoneBlockMode) {
                // Check first if we are above
                if (redstoneAbovePiston) {
                    // Get the position
                    int[] toAdd;
                    if (enemyCoordsInt[0] == (int) pistonCoordsAbs[0] && enemyCoordsInt[2] == (int) pistonCoordsAbs[2]) {
                        toAdd = new int[]{pistonCoordsRel[0], pistonCoordsRel[1], 0};
                    } else {
                        toAdd = new int[]{pistonCoordsRel[0], pistonCoordsRel[1], pistonCoordsRel[2]};
                    }
                    // Lets check if there is the structure
                    for (int hight = -1; hight < 2; hight++)
                        // Is air
                        if (!PistonCrystal.someoneInCoords(pistonCoordsAbs[0] + toAdd[0], pistonCoordsAbs[2] + toAdd[2])
                                && BlockUtil.getBlock(pistonCoordsAbs[0] + toAdd[0], pistonCoordsAbs[1] + hight, pistonCoordsAbs[2] + toAdd[2])
                                instanceof BlockAir) {
                            // Obby
                            toPlaceTemp.add(new Vec3d(pistonCoordsRel[0] + toAdd[0], pistonCoordsRel[1] + hight, pistonCoordsRel[2] + toAdd[2]));
                            supportBlock++;
                        }
                } else if (BlockUtil.getBlock(redstoneCoordsAbs[0], redstoneCoordsAbs[1] - 1, redstoneCoordsAbs[2]) instanceof BlockAir) {
                    // Alse, place obby down
                    toPlaceTemp.add(new Vec3d(redstoneCoordsRel[0], redstoneCoordsRel[1] - 1, redstoneCoordsRel[2]));
                    supportBlock++;
                }
            }

            // If trapPlayer
            if (trapMode.getValue()) {
                // Iterate for everything
                toPlaceTemp.addAll(Arrays.asList(// Supports
                        new Vec3d(-1, -1, -1),
                        new Vec3d(-1, 0, -1),
                        new Vec3d(-1, 1, -1),
                        // Start circle
                        new Vec3d(-1, 2, -1),
                        new Vec3d(-1, 2, 0),
                        new Vec3d(0, 2, -1),
                        new Vec3d(1, 2, -1),
                        new Vec3d(1, 2, 0),
                        new Vec3d(1, 2, 1),
                        new Vec3d(0, 2, 1)));
                supportBlock += 10;
            }

            /// Add all others blocks
            // Piston
            toPlaceTemp.add(new Vec3d(pistonCoordsRel[0], pistonCoordsRel[1], pistonCoordsRel[2]));
            // Redstone
            toPlaceTemp.add(new Vec3d(redstoneCoordsRel[0], redstoneCoordsRel[1], redstoneCoordsRel[2]));

            /// Rotation calculation
            float offsetX, offsetZ, offsetY;
            int position;
            // Check the position
            if (disp_surblock[i][0] == 0) {
                if (disp_surblock[i][2] == 1)
                    position = 0;
                else position = 1;
            } else {
                if (disp_surblock[i][0] == 1)
                    position = 2;
                else position = 3;
            }
            // If horrizontaly
            if (disp_surblock[i][0] != 0) {
                offsetX = disp_surblock[i][0] / 2f;
                // Check which is better for distance
                if (mc.player.getDistanceSq(pistonCoordsAbs[0], pistonCoordsAbs[1], pistonCoordsAbs[2] + 0.5) > mc.player.getDistanceSq(pistonCoordsAbs[0], pistonCoordsAbs[1], pistonCoordsAbs[2] - 0.5))
                    offsetZ = -0.5f;
                else
                    offsetZ = 0.5f;
                // If vertically
            } else {
                offsetZ = disp_surblock[i][2] / 2f;
                // Check which is better for distance
                if (mc.player.getDistanceSq(pistonCoordsAbs[0] + 0.5, pistonCoordsAbs[1], pistonCoordsAbs[2]) > mc.player.getDistanceSq(pistonCoordsAbs[0] - 0.5, pistonCoordsAbs[1], pistonCoordsAbs[2]))
                    offsetX = -0.5f;
                else
                    offsetX = 0.5f;
            }

            /// Calculate the y offset.
            // If we are above, 1, if we are belove, 0
            offsetY = meCoordsInt[1] - enemyCoordsInt[1] == -1 ? 0 : 1;

            // Repleace the structure
            addedStructure.replaceValues(distanceNowCrystal, supportBlock, toPlaceTemp, -1, offsetX, offsetZ, offsetY, position);


            toPlace = addedStructure;



        }



        /*}catch (Exception e) {
            PistonCrystal.printDebug("Fatal Error during the creation of the structure. Please, report this bug in the discord's server", true);
            final Logger LOGGER = LogManager.getLogger("GameSense");
            LOGGER.error("[Elevator] error during the creation of the structure.");
            if (e.getMessage() != null)
                LOGGER.error("[Elevator] error message: " + e.getClass().getName() + " " + e.getMessage());
            else
                LOGGER.error("[Elevator] cannot find the cause");
            int i5 = 0;

            if (e.getStackTrace().length != 0) {
                LOGGER.error("[Elevator] StackTrace Start");
                for (StackTraceElement errorMess : e.getStackTrace()) {
                    LOGGER.error("[Elevator] " + errorMess.toString());
                }
                LOGGER.error("[Elevator] StackTrace End");
            }

            if (aimTarget != null) {
                LOGGER.error("[Elevator] closest target is not null");
            } else LOGGER.error("[Elevator] closest target is null somehow");
            for (Double[] cord_b : sur_block) {
                if (cord_b != null) {
                    LOGGER.error("[Elevator] " + i5 + " is not null");
                } else {
                    LOGGER.error("[Elevator] " + i5 + " is null");
                }
                i5++;
            }

        }*/

        if (debugMode.getValue() && addedStructure.to_place != null) {
            PistonCrystal.printDebug("Skeleton structure:", false);
            for (Vec3d parte : addedStructure.to_place) {
                PistonCrystal.printDebug(String.format("%f %f %f", parte.x, parte.y, parte.z), false);
            }
            PistonCrystal.printDebug(String.format("X: %f Y: %f Z: %f", toPlace.offsetX, toPlace.offsetY, toPlace.offsetZ), false);
        }

        return addedStructure.to_place != null;
    }

}
