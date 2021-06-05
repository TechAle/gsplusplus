package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.api.util.world.combat.DamageUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gamesense.api.util.player.SpoofRotationUtil.ROTATION_UTIL;

/**
 * @Author TechAle
 * Most things are ported from PistonCrystal (guess why lol)
 */

@Module.Declaration(name = "Elevatot", category = Category.Combat)
public class Elevatot extends Module {

    ModeSetting target = registerMode("Target", Arrays.asList("Nearest", "Looking"), "Nearest");
    ModeSetting placeMode = registerMode("Place", Arrays.asList("Torch", "Block", "Both"), "Torch");
    DoubleSetting enemyRange = registerDouble("Range", 4.9, 0, 6);
    IntegerSetting maxYincr = registerInteger("Max Y", 3, 0, 5);
    BooleanSetting antiWeakness = registerBoolean("Anti Weakness", false);
    BooleanSetting debugMode = registerBoolean("Debug Mode", false);
    BooleanSetting trapMode = registerBoolean("Trap Mode", false);

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


    boolean redstoneBlockMode,
            enoughSpace,
            isHole,
            noMaterials;

    @Override
    public void onEnable() {
        ROTATION_UTIL.onEnable();

        initValues();

        // Get Target
        if (getAimTarget())
            return;

        playerChecks();

    }

    private void initValues() {
        sur_block = new double[4][3];
        slot_mat = new int[] {
                -1, -1, -1, -1, -1
        };
        enemyCoordsDouble = new double[3];
        toPlace = new structureTemp(0, 0, null);

        redstoneBlockMode = isHole = noMaterials = false;

        aimTarget = null;
    }

    // Get all the materials
    private boolean getMaterialsSlot() {
		/*
			// I use this as a remind to which index refers to what
			0 => obsidian
			1 => piston
			2 => redstone
			3 => sword
			4 => pick
		 */

        if (placeMode.getValue().equals("Block"))
            redstoneBlockMode = true;

        // Iterate for all the inventory
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            // If there is no block
            if (stack == ItemStack.EMPTY) {
                continue;
            }
            if (antiWeakness.getValue() && stack.getItem() instanceof ItemSword) {
                slot_mat[3] = i;
            } else
                // If Pick
                if (stack.getItem() instanceof ItemPickaxe) {
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
            PistonCrystal.printDebug(String.format("%d %d %d %d %d", slot_mat[0], slot_mat[1], slot_mat[2], slot_mat[3], slot_mat[4]), false);

        // If we have everything we need, return true
        return count >= 4 + (antiWeakness.getValue() ? 1 : 0);

    }

    // Get target function
    private boolean getAimTarget() {
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

    @Override
    public void onDisable() {
    }

    @Override
    public void onUpdate() {
        int a = 0;
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

    // Class for the structure
    private static class structureTemp {
        public double distance;
        public int supportBlock;
        public List<Vec3d> to_place;
        public int direction;
        public float offsetX;
        public float offsetY;
        public float offsetZ;

        public structureTemp(double distance, int supportBlock, List<Vec3d> to_place) {
            this.distance = distance;
            this.supportBlock = supportBlock;
            this.to_place = to_place;
            this.direction = -1;
        }

        public void replaceValues(double distance, int supportBlock, List<Vec3d> to_place, int direction, float offsetX, float offsetZ, float offsetY) {
            this.distance = distance;
            this.supportBlock = supportBlock;
            this.to_place = to_place;
            this.direction = direction;
            this.offsetX = offsetX;
            this.offsetZ = offsetZ;
            this.offsetY = offsetY;
        }
    }

    boolean redstoneAbovePiston;
    structureTemp toPlace;

    boolean createStructure() {

        /// Create default start value
        // Structure default
        structureTemp addedStructure = new structureTemp(Double.MAX_VALUE, 0, null);
        // Distance we are going to find
        double distanceNowCrystal;
        // Since they may happens some errors that i did not expect, i use a try-catch
        //try {

        // First check, h check.
        if (meCoordsInt[1] - enemyCoordsInt[1] > -1
                && meCoordsInt[1] - enemyCoordsInt[1] <= maxYincr.getValue()) {
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
                    0 = {double[3]@11290} [55.575943989104964, 1.0, 17.51419950026377]
                    1 = {double[3]@11304} [53.575943989104964, 1.0, 17.51419950026377]
                    2 = {double[3]@11380} [54.575943989104964, 1.0, 18.51419950026377]
                    3 = {double[3]@11381} [54.575943989104964, 1.0, 16.51419950026377]
                    54 1 17
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
                if (!(BlockUtil.getBlock(pistonCoordsAbs[0], pistonCoordsAbs[1], pistonCoordsAbs[2]) instanceof BlockAir))
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

                /// Add all others blocks
                // Piston
                toPlaceTemp.add(new Vec3d(pistonCoordsRel[0], pistonCoordsRel[1], pistonCoordsRel[2]));
                // Redstone
                toPlaceTemp.add(new Vec3d(redstoneCoordsRel[0], redstoneCoordsRel[1], redstoneCoordsRel[2]));

                /// Rotation calculation
                float offsetX, offsetZ, offsetY;
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
                addedStructure.replaceValues(distanceNowCrystal, supportBlock, toPlaceTemp, -1, offsetX, offsetZ, offsetY);

                // If trapPlayer
                if (trapMode.getValue()) {
                    // Iterate for everything
                    addedStructure.to_place.addAll(Arrays.asList(// Supports
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
                    addedStructure.supportBlock += 10;
                }
                toPlace = addedStructure;



            }

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
