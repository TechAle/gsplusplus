package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.player.PredictUtil;
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
    IntegerSetting exponentStartDecrease = registerInteger("Exponent Start", 2, 1, 5, () -> calculateYPredict.getValue());
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
    BooleanSetting stairPredict = registerBoolean("Stair Predict", false);
    IntegerSetting nStair = registerInteger("N Stair", 2, 1, 4, () -> stairPredict.getValue());
    DoubleSetting speedActivationStair = registerDouble("Speed Activation Stair", .3, 0, 1, () -> stairPredict.getValue());
    ColorSetting mainColor = registerColor("Color");

    public void onWorldRender(RenderEvent event) {
        PredictUtil.PredictSettings settings = new PredictUtil.PredictSettings(tickPredict.getValue(), calculateYPredict.getValue(), startDecrease.getValue(), exponentStartDecrease.getValue(), decreaseY.getValue(), exponentDecreaseY.getValue(), increaseY.getValue(), exponentIncreaseY.getValue(), splitXZ.getValue(), width.getValue(), debug.getValue(), showPredictions.getValue(), manualOutHole.getValue(), aboveHoleManual.getValue(), stairPredict.getValue(), nStair.getValue(), speedActivationStair.getValue());
        mc.world.playerEntities.stream().filter(entity -> (!hideSelf.getValue() || entity != mc.player)).filter(this::rangeEntityCheck).forEach(entity -> {
            EntityPlayer clonedPlayer = PredictUtil.predictPlayer(entity, settings);
            RenderUtil.drawBoundingBox(clonedPlayer.getEntityBoundingBox(), width.getValue(), mainColor.getColor());
        });
        if (justOnce.getValue())
            disable();
    }

    private boolean rangeEntityCheck(Entity entity) {
        return entity.getDistance(mc.player) <= range.getValue();
    }

}