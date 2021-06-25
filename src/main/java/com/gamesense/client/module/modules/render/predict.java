package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.GeometryMasks;
import com.gamesense.api.util.world.Offsets;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.modules.combat.PistonCrystal;
import com.gamesense.test.EntityPositionsProvider;
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
    IntegerSetting tickPredict = registerInteger("Tick Predict", 2, 0, 30);
    IntegerSetting decreaseY = registerInteger("Decrease Y", 1, 1, 5);
    BooleanSetting splitXZ = registerBoolean("Split XZ", true);
    BooleanSetting hideSelf = registerBoolean("Hide Self", false);
    IntegerSetting width = registerInteger("Line Width", 2, 1, 5);
    ColorSetting mainColor = registerColor("Color");

    public void onEnable() {
    }



    public void onUpdate() {

    }

    public void onWorldRender(RenderEvent event) {
        mc.world.loadedEntityList.stream().filter(entity -> entity instanceof EntityPlayer && (!hideSelf.getValue() || entity != mc.player)).filter(this::rangeEntityCheck).forEach(entity -> {
            double[] posVec = new double[] {entity.posX, entity.posY, entity.posZ};
            EntityPlayer prova = (EntityPlayer) entity;


            double[] newPosVec = posVec.clone();
            double motionX = entity.motionX;
            double motionY = entity.motionY;
            double motionZ = entity.motionZ;
            for(int i = 0; i < tickPredict.getValue(); i++) {
                RayTraceResult result;
                if (splitXZ.getValue()) {
                    newPosVec = posVec.clone();
                    newPosVec[0] += motionX;
                    result = mc.world.rayTraceBlocks(new Vec3d(posVec[0], posVec[1], posVec[2]), new Vec3d(newPosVec[0], posVec[1], posVec[2]));
                    if (result == null || result.typeOfHit == RayTraceResult.Type.ENTITY) {
                        posVec = newPosVec.clone();
                    }

                    newPosVec = posVec.clone();
                    newPosVec[2] += motionZ;
                    result = mc.world.rayTraceBlocks(new Vec3d(posVec[0], posVec[1], posVec[2]), new Vec3d(newPosVec[0], posVec[1], newPosVec[2]));
                    if (result == null || result.typeOfHit == RayTraceResult.Type.ENTITY) {
                        posVec = newPosVec.clone();
                    }
                } else {
                    newPosVec = posVec.clone();
                    newPosVec[0] += motionX;
                    newPosVec[2] += motionZ;
                    result = mc.world.rayTraceBlocks(new Vec3d(posVec[0], posVec[1], posVec[2]), new Vec3d(newPosVec[0], posVec[1], newPosVec[2]));
                    if (result == null || result.typeOfHit == RayTraceResult.Type.ENTITY) {
                        posVec = newPosVec.clone();
                    }
                }

                newPosVec[1] += motionY;
                result = mc.world.rayTraceBlocks(new Vec3d(posVec[0], posVec[1], posVec[2]), new Vec3d(newPosVec[0], newPosVec[1], newPosVec[2]));
                if (result == null || result.typeOfHit == RayTraceResult.Type.ENTITY) {
                    posVec = newPosVec.clone();
                    motionY -= 10/decreaseY.getValue();
                }


            }
            EntityOtherPlayerMP clonedPlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("fdee323e-7f0c-4c15-8d1c-0f277442342a"), "Fit"));
            clonedPlayer.setPosition(posVec[0], posVec[1], posVec[2]);
            RenderUtil.drawBoundingBox(clonedPlayer.getEntityBoundingBox(), width.getValue(), mainColor.getColor());
        });
    }

    private boolean rangeEntityCheck(Entity entity) {
        return entity.getDistance(mc.player) <= range.getValue();
    }

}