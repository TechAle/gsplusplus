package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.*;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.modules.combat.PistonCrystal;
import com.mojang.authlib.GameProfile;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @Author: TechAle
 */

@Module.Declaration(name = "Predict", category = Category.Render)
public class predict extends Module {

    IntegerSetting range = registerInteger("Range", 10,0, 100);
    IntegerSetting tickPredict = registerInteger("Tick Predict", 8, 0, 30);
    BooleanSetting calculateYPredict = registerBoolean("Calculate Y Predict", true);
    IntegerSetting startDecrease = registerInteger("Start Decrease", 39, 0, 200, () -> calculateYPredict.getValue());
    IntegerSetting expnentStartDecrease = registerInteger("Exponent Start", 2, 1, 5, () -> calculateYPredict.getValue());
    IntegerSetting decreaseY = registerInteger("Decrease Y", 2, 1, 5, () -> calculateYPredict.getValue());
    IntegerSetting exponentDecreaseY = registerInteger("Exponent Decrease Y", 1, 1, 3, () -> calculateYPredict.getValue());
    IntegerSetting increaseY = registerInteger("Increase Y", 3, 1, 5, () -> calculateYPredict.getValue());
    IntegerSetting exponentIncreaseY = registerInteger("Exponent Increase Y", 2, 1, 3, () -> calculateYPredict.getValue());
    BooleanSetting splitXZ = registerBoolean("Split XZ", true);
    BooleanSetting hideSelf = registerBoolean("Hide Self", false);
    IntegerSetting width = registerInteger("Line Width", 2, 1, 5);
    BooleanSetting justOnce = registerBoolean("Just Once", false);
    BooleanSetting debug = registerBoolean("Debug", false);
    BooleanSetting showPredictions = registerBoolean("Show Predictions", false);
    BooleanSetting manualOutHole = registerBoolean("Manual Out Hole", false);
    BooleanSetting aboveHoleManual = registerBoolean("Above Hole Manual", false, () -> manualOutHole.getValue());
    ColorSetting mainColor = registerColor("Color");


    public void onWorldRender(RenderEvent event) {
        mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityPlayer && (!hideSelf.getValue() || entity != mc.player)).filter(this::rangeEntityCheck).forEach(entity -> {
            // Position of the player
            double[] posVec = new double[] {entity.posX, entity.posY, entity.posZ};
            // This is likely a temp variable that is going to replace posVec
            double[] newPosVec = posVec.clone();
            // entity motions
            double motionX = entity.posX - entity.prevPosX;
            double motionY = entity.posY - entity.prevPosY;
            double motionZ = entity.posZ - entity.prevPosZ;
            // Y Prediction stuff
            boolean goingUp = false;
            boolean start = true;
            int up = 0, down = 0;
            if (debug.getValue())
                PistonCrystal.printDebug(String.format("Values: %f %f %f", newPosVec[0], newPosVec[1], newPosVec[2]), false);

            // If he want manual out hole
            boolean isHole = false;
            if (manualOutHole.getValue() && motionY > 0) {
                if (HoleUtil.isHole(EntityUtil.getPosition(entity), false, true).getType() != HoleUtil.HoleType.NONE)
                    isHole = true;
                else if (aboveHoleManual.getValue() && HoleUtil.isHole(EntityUtil.getPosition(entity).add(0, -1, 0), false, true).getType() != HoleUtil.HoleType.NONE)
                    isHole = true;

                if (isHole)
                    posVec[1] += 1;

            }

            for(int i = 0; i < tickPredict.getValue(); i++) {
                RayTraceResult result;
                // Here we can choose if calculating XZ separated or not
                if (splitXZ.getValue()) {
                    // Clone posVec
                    newPosVec = posVec.clone();
                    // Add X
                    newPosVec[0] += motionX;
                    // Check collisions
                    result = mc.world.rayTraceBlocks(new Vec3d(posVec[0], posVec[1], posVec[2]), new Vec3d(newPosVec[0], posVec[1], posVec[2]));
                    if (result == null || result.typeOfHit == RayTraceResult.Type.ENTITY) {
                        posVec = newPosVec.clone();
                    }
                    // Calculate Z
                    newPosVec = posVec.clone();
                    newPosVec[2] += motionZ;
                    // Check collisions
                    result = mc.world.rayTraceBlocks(new Vec3d(posVec[0], posVec[1], posVec[2]), new Vec3d(newPosVec[0], posVec[1], newPosVec[2]));
                    if (result == null || result.typeOfHit == RayTraceResult.Type.ENTITY) {
                        posVec = newPosVec.clone();
                    }
                // In case of calculating them toogheter
                } else {
                    // Add XZ and check collisions
                    newPosVec = posVec.clone();
                    newPosVec[0] += motionX;
                    newPosVec[2] += motionZ;
                    result = mc.world.rayTraceBlocks(new Vec3d(posVec[0], posVec[1], posVec[2]), new Vec3d(newPosVec[0], posVec[1], newPosVec[2]));
                    if (result == null || result.typeOfHit == RayTraceResult.Type.ENTITY) {
                        posVec = newPosVec.clone();
                    }
                }

                /*
                    The y predict is a little bit complex:
                    1) We have to understand if we are going up or down
                    2) We have to understand when going up and going down and when switching it
                    3) Implement a physic to the y transiction
                    So,
                    1) We understand if we are going up or down by the default motionY: If it's > 0, we are going up.
                        This was the intention, then i decided to set it always to false because of strange bugs
                        it predict nicely so no problem
                    2) We understand when switch:
                        a) from down to up when we collide with something under us, if we collide with something
                        b) From up to down when the motionY become from positive to negative
                    and this open the 3 point about the physyc:
                    3) For making everything simpler and not overcomplex, when going up the motionY become
                    a little bit bigger untill the reach a value that is called Exponent Start.
                    Meanwhile, for going down, it's a little bit easier: we just do like before
                    but until we collide with something.
                    Yes, this is a unprecise but, this error of imprecision, is noticable only in
                    long tick predict, and you dont use long tick prediction lol.
                    i think this y prediction is usefull for detecting, in short distance, if we are going up or down
                    on a block
                    */
                if (calculateYPredict.getValue() && !isHole) {
                    newPosVec = posVec.clone();
                    // If the enemy is not on the ground. We also be sure that it's not -0.078
                    // Because -0.078 is the motion we have when standing in a block.
                    // I dont know if we have antiHunger the server say we are onGround or not, i'll keep it here
                    if (!entity.onGround && motionY != -0.0784000015258789) {
                        if (start) {
                            // If it's the first time, we have to check first if our motionY is == 0.
                            // MotionY is == 0 when we are jumping at the moment when we are going down
                            if (motionY == 0)
                                motionY = startDecrease.getValue() / Math.pow(10, expnentStartDecrease.getValue());
                            // Check if we are going up or down. We say > because of motionY
                            goingUp = false;
                            start = false;
                            if (debug.getValue())
                                PistonCrystal.printDebug("Start motionY: " + motionY, false);
                        }
                        // Lets just add values to our motionY
                        motionY += goingUp ? increaseY.getValue() / Math.pow(10, exponentIncreaseY.getValue()) : decreaseY.getValue() / Math.pow(10, exponentDecreaseY.getValue());
                        // If the motionY is going too far, go down
                        if (Math.abs(motionY) > startDecrease.getValue() / Math.pow(10, expnentStartDecrease.getValue())) {
                            goingUp = false;
                            if (debug.getValue())
                                up++;
                            motionY = decreaseY.getValue() / Math.pow(10, exponentDecreaseY.getValue());
                        }
                        // Lets add motionY
                        newPosVec[1] += (goingUp ? 1 : -1) * motionY;
                        // Get result
                        result = mc.world.rayTraceBlocks(new Vec3d(posVec[0], posVec[1], posVec[2]),
                                new Vec3d(newPosVec[0], newPosVec[1], newPosVec[2]));

                        if (result == null || result.typeOfHit == RayTraceResult.Type.ENTITY) {
                            posVec = newPosVec.clone();
                        } else {
                            if (!goingUp) {
                                goingUp = true;
                                // Add this for deleting before motion
                                newPosVec[1] += (increaseY.getValue() / Math.pow(10, exponentIncreaseY.getValue()));
                                motionY = increaseY.getValue() / Math.pow(10, exponentIncreaseY.getValue());
                                newPosVec[1] += motionY;
                                if (debug.getValue())
                                    down++;
                            }
                        }


                    }
                }


                if (showPredictions.getValue())
                    PistonCrystal.printDebug(String.format("Values: %f %f %f", posVec[0], posVec[1], posVec[2]), false);

            }
            if (debug.getValue()) {
                PistonCrystal.printDebug(String.format("Player: %s Total ticks: %d Up: %d Down: %d", ((EntityPlayer) entity).getGameProfile().getName(), tickPredict.getValue(), up, down), false);
            }
            EntityOtherPlayerMP clonedPlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("fdee323e-7f0c-4c15-8d1c-0f277442342a"), "Fit"));
            clonedPlayer.setPosition(posVec[0], posVec[1], posVec[2]);
            RenderUtil.drawBoundingBox(clonedPlayer.getEntityBoundingBox(), width.getValue(), mainColor.getColor());
        });
        if (justOnce.getValue())
            disable();
    }

    private boolean rangeEntityCheck(Entity entity) {
        return entity.getDistance(mc.player) <= range.getValue();
    }

}