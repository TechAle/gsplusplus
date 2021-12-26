/*
    Author: TechAle
    Description: Place and break crystals with the power of multithreading (fuck yeha!)
    Created: 06/28/21
 */
package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.Phase;
import com.gamesense.api.event.events.DamageBlockEvent;
import com.gamesense.api.event.events.OnUpdateWalkingPlayerEvent;
import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.misc.KeyBoardClass;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.*;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.GeometryMasks;
import com.gamesense.api.util.world.combat.CrystalUtil;
import com.gamesense.api.util.world.combat.DamageUtil;
import com.gamesense.api.util.world.combat.ac.CrystalInfo;
import com.gamesense.api.util.world.combat.ac.PlayerInfo;
import com.gamesense.api.util.world.combat.ac.PositionInfo;
import com.gamesense.client.manager.managers.PlayerPacketManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.mixin.mixins.accessor.AccessorCPacketAttack;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module.Declaration(name = "AutoCrystalRewrite", category = Category.Combat, priority = 100)
public class AutoCrystalRewrite extends Module {

    //region Settings
    //region Logic
    BooleanSetting logicTarget = registerBoolean("Logic Section", true);
    ModeSetting logic = registerMode("Logic", Arrays.asList("Place->Break", "Break->Place", "Place", "Break"), "Place->Break", () -> logicTarget.getValue());
    BooleanSetting oneStop = registerBoolean("One Stop", false,
            () -> logicTarget.getValue() && (logic.getValue().equals("Place->Break") || logic.getValue().equals("Break->Place")));
    ModeSetting targetPlacing = registerMode("Target Placing", Arrays.asList("Nearest", "Lowest", "Damage"), "Nearest", () -> logicTarget.getValue());
    ModeSetting targetBreaking = registerMode("Target Breaking", Arrays.asList("Nearest", "Lowest", "Damage"), "Nearest", () -> logicTarget.getValue());
    BooleanSetting stopGapple = registerBoolean("Stop Gapple", false, () -> logicTarget.getValue());
    IntegerSetting tickWaitEat = registerInteger("Tick Wait Eat", 4, 0, 10,
            () -> logicTarget.getValue() && stopGapple.getValue());
    public BooleanSetting newPlace = registerBoolean("1.13 mode", false, () -> logicTarget.getValue());
    BooleanSetting ignoreTerrain = registerBoolean("Ignore Terrain", false, () -> logicTarget.getValue());
    BooleanSetting bindIgnoreTerrain = registerBoolean("Bind IgnoreTerrain", false, () -> logicTarget.getValue() && ignoreTerrain.getValue());
    BooleanSetting entityPredict = registerBoolean("Entity Predict", false, () -> logicTarget.getValue());
    IntegerSetting offset = registerInteger("OffSet Predict", 0,0, 10, () -> logicTarget.getValue() && entityPredict.getValue());
    IntegerSetting tryAttack = registerInteger("Try Attack", 1, 1, 10, () -> logicTarget.getValue() && entityPredict.getValue());
    IntegerSetting delayAttacks = registerInteger("Delay Attacks", 50, 0, 500, () -> logicTarget.getValue() && entityPredict.getValue());
    IntegerSetting midDelayAttacks = registerInteger("Mid Delay Attack", 5, 0, 100, () -> logicTarget.getValue() && entityPredict.getValue());
    //endregion

    //region Ranges
    BooleanSetting ranges = registerBoolean("Range Section", false);
    DoubleSetting rangeEnemyPlace = registerDouble("Range Enemy Place", 7, 0, 12, () -> ranges.getValue());
    DoubleSetting rangeEnemyBreaking = registerDouble("Range Enemy Breaking", 7, 0, 12, () -> ranges.getValue());
    public DoubleSetting placeRange = registerDouble("Place Range", 6, 0, 8, () -> ranges.getValue());
    public DoubleSetting breakRange = registerDouble("Break Range", 6, 0, 8, () -> ranges.getValue());
    DoubleSetting crystalWallPlace = registerDouble("Wall Range Place", 3.5, 0, 8, () -> ranges.getValue());
    DoubleSetting wallrangeBreak = registerDouble("Wall Range Break", 3.5, 0, 8, () -> ranges.getValue());
    IntegerSetting maxYTarget = registerInteger("Max Y", 3, 0, 5, () -> ranges.getValue());
    IntegerSetting minYTarget = registerInteger("Min Y", 3, 0, 5, () -> ranges.getValue());
    //endregion

    //region Place
    BooleanSetting place = registerBoolean("Place Section", false);
    ModeSetting placeDelay = registerMode("Place Delay", Arrays.asList("Tick", "Time", "Vanilla"), "Tick", () -> place.getValue());
    IntegerSetting tickDelayPlace = registerInteger("Tick Delay Place", 0, 0, 20,
            () -> place.getValue() && placeDelay.getValue().equals("Tick"));
    IntegerSetting timeDelayPlace = registerInteger("TIme Delay Place", 0, 0, 2000,
            () -> place.getValue() && placeDelay.getValue().equals("Time"));
    IntegerSetting vanillaSpeedPlace = registerInteger("Vanilla Speed pl", 19, 0, 20,
            () -> place.getValue() && placeDelay.getValue().equals("Vanilla"));
    BooleanSetting placeOnCrystal = registerBoolean("Place On Crystal", false,
            () -> place.getValue());
    DoubleSetting minDamagePlace = registerDouble("Min Damage Place", 5, 0, 30, () -> place.getValue());
    DoubleSetting maxSelfDamagePlace = registerDouble("Max Self Damage Place", 12, 0, 30, () -> place.getValue());
    BooleanSetting relativeDamagePlace = registerBoolean("Relative Damage Pl", false, () -> place.getValue());
    DoubleSetting relativeDamageValuePlace = registerDouble("Damage Relative Damage Pl", .8, 0, 1, () -> place.getValue() && relativeDamagePlace.getValue());
    IntegerSetting armourFacePlace = registerInteger("Armour Health%", 20, 0, 100, () -> place.getValue());
    IntegerSetting facePlaceValue = registerInteger("FacePlace HP", 8, 0, 36, () -> place.getValue());
    DoubleSetting minFacePlaceDmg = registerDouble("FacePlace Dmg", 2, 0, 10, () -> place.getValue());
    BooleanSetting antiSuicidepl = registerBoolean("AntiSuicide pl", true, () -> place.getValue());
    BooleanSetting includeCrystalMapping = registerBoolean("Include Crystal Mapping", true, () -> place.getValue());
    ModeSetting limitPacketPlace = registerMode("Limit Packet Place", Arrays.asList("None", "Tick", "Time"), "None",
            () -> place.getValue());
    IntegerSetting limitTickPlace = registerInteger("Limit Tick Place", 0, 0, 20,
            () -> place.getValue() && limitPacketPlace.getValue().equals("Tick"));
    IntegerSetting limitTickTime = registerInteger("Limit Time Place", 0, 0, 2000,
            () -> place.getValue() && limitPacketPlace.getValue().equals("Time"));
    ModeSetting swingModepl = registerMode("Swing Mode pl", Arrays.asList("Client", "Server", "None"), "Server",
            () -> place.getValue());
    BooleanSetting hideClientpl = registerBoolean("Hide Client pl", false,
            () -> place.getValue() && swingModepl.getValue().equals("Server"));
    BooleanSetting autoWeb = registerBoolean("Auto Web", false, () -> place.getValue());
    BooleanSetting stopCrystal = registerBoolean("Stop Crystal", true, () -> place.getValue() && autoWeb.getValue());
    BooleanSetting preRotateWeb = registerBoolean("Pre Rotate Web", false, () -> place.getValue() && autoWeb.getValue());
    BooleanSetting focusWebRotate = registerBoolean("Focus Ber Rotate", false,
            () -> place.getValue() && autoWeb.getValue());
    BooleanSetting onlyAutoWebActive = registerBoolean("On AutoWeb active", true, () -> place.getValue() && autoWeb.getValue());
    BooleanSetting switchWeb = registerBoolean("Switch Web", false, () -> place.getValue() && autoWeb.getValue());
    BooleanSetting silentSwitchWeb = registerBoolean("Silent Switch Web", false,
            () -> place.getValue() && autoWeb.getValue() );
    BooleanSetting switchBackWeb = registerBoolean("Switch Back Web", false,
            () -> place.getValue() && autoWeb.getValue() && switchWeb.getValue() && !silentSwitchWeb.getValue());
    BooleanSetting switchBackEnd = registerBoolean("Switch Back Web End", false,
            () -> place.getValue() && autoWeb.getValue() && switchWeb.getValue() && !silentSwitchWeb.getValue() && switchBackWeb.getValue());
    BooleanSetting breakNearCrystal = registerBoolean("Break Near Crystal", false,
            () -> place.getValue());
    //endregion

    //region break
    BooleanSetting breakSection = registerBoolean("Break Section", false);
    ModeSetting breakDelay = registerMode("Break Delay", Arrays.asList("Tick", "Time", "Vanilla"), "Tick", () -> breakSection.getValue());
    IntegerSetting tickDelayBreak = registerInteger("Tick Delay Place", 0, 0, 20,
            () -> breakSection.getValue() && breakDelay.getValue().equals("Tick"));
    IntegerSetting timeDelayBreak = registerInteger("TIme Delay Place", 0, 0, 2000,
            () -> breakSection.getValue() && breakDelay.getValue().equals("Time"));
    IntegerSetting vanillaSpeedBreak = registerInteger("Vanilla Speed br", 19, 0, 20,
            () -> breakSection.getValue() && breakDelay.getValue().equals("Vanilla"));
    ModeSetting chooseCrystal = registerMode("Choose Type", Arrays.asList("Own", "All", "Smart"), "Smart",
            () -> breakSection.getValue());
    DoubleSetting minDamageBreak = registerDouble("Min Damage Break", 5, 0, 30, () -> breakSection.getValue());
    DoubleSetting maxSelfDamageBreak = registerDouble("Max Self Damage Break", 12, 0, 30,
            () -> breakSection.getValue() && chooseCrystal.getValue().equals("Smart"));
    BooleanSetting relativeDamageBreak = registerBoolean("Relative Damage Br", false, () -> breakSection.getValue());
    DoubleSetting relativeDamageValueBreak = registerDouble("Damage Relative Damage Br", .8, 0, 1, () -> breakSection.getValue() && relativeDamagePlace.getValue());
    ModeSetting swingModebr = registerMode("Swing Mode br", Arrays.asList("Client", "Server", "None"), "Server",
            () -> breakSection.getValue());
    BooleanSetting hideClientbr = registerBoolean("Hide Client br", false,
            () -> breakSection.getValue() && swingModebr.getValue().equals("Server"));
    ModeSetting breakTypeCrystal = registerMode("Break Type", Arrays.asList("Packet", "Vanilla"), "Packet",
            () -> breakSection.getValue());
    ModeSetting limitBreakPacket = registerMode("Limit Break Packet", Arrays.asList("Tick", "Time", "None"), "None",
            () -> breakSection.getValue());
    IntegerSetting lomitBreakPacketTick = registerInteger("Limit Break Tick", 4, 0, 20,
            () -> breakSection.getValue() && limitBreakPacket.getValue().equals("Tick"));
    IntegerSetting limitBreakPacketTime = registerInteger("Limit Break Time", 500, 0, 2000,
            () -> breakSection.getValue() && limitBreakPacket.getValue().equals("Time"));
    ModeSetting firstHit = registerMode("First Hit", Arrays.asList("Tick", "Time", "None"), "None", () -> breakSection.getValue());
    IntegerSetting firstHitTick = registerInteger("Tick First Hit", 0, 0, 20,
            () -> breakSection.getValue() && firstHit.getValue().equals("Tick"));
    IntegerSetting fitstHitTime = registerInteger("TIme First Hit", 0, 0, 2000,
            () -> breakSection.getValue() && firstHit.getValue().equals("Time"));
    // This is useless lmao
    BooleanSetting cancelCrystal = registerBoolean("Cancel Crystal", false,
            () -> breakSection.getValue());
    // This is also useless ngl
    BooleanSetting setDead = registerBoolean("Set Dead", true,
            () -> breakSection.getValue());
    BooleanSetting placeAfterBreak = registerBoolean("Place After", false,
            () -> breakSection.getValue());
    BooleanSetting instaPlace = registerBoolean("Insta Place", false,
            () -> breakSection.getValue() && placeAfterBreak.getValue());
    BooleanSetting checkinstaPlace = registerBoolean("Check Insta Place", false,
            () -> breakSection.getValue() && placeAfterBreak.getValue() && instaPlace.getValue());
    BooleanSetting forcePlace = registerBoolean("Force Place", false,
            () -> breakSection.getValue() && placeAfterBreak.getValue() && instaPlace.getValue());
    BooleanSetting antiWeakness = registerBoolean("Anti Weakness", false, () -> breakSection.getValue());
    ModeSetting slowBreak = registerMode("Slow Break", Arrays.asList("None", "Tick", "Time"), "None", () -> breakSection.getValue());
    DoubleSetting speedActivation = registerDouble("Speed Activation", 0.5, 0, 1,
            () -> breakSection.getValue() && !slowBreak.getValue().equals("None"));
    IntegerSetting tickSlowBreak = registerInteger("Tick Slow Break", 3, 0, 20,
            () -> breakSection.getValue() && slowBreak.getValue().equals("Tick"));
    IntegerSetting timeSlowBreak = registerInteger("Time Slow Break", 3, 0, 10,
            () -> breakSection.getValue() && slowBreak.getValue().equals("Time"));
    BooleanSetting predictHit = registerBoolean("Predict Hit", false, () -> breakSection.getValue());
    IntegerSetting predictHitDelay = registerInteger("Predict Hit Delay", 0, 0, 500, () -> breakSection.getValue() && predictHit.getValue());
    BooleanSetting antiSuicidebr = registerBoolean("AntiSuicide br", true, () -> breakSection.getValue());
    BooleanSetting antiCity = registerBoolean("Anti City", false, () -> breakSection.getValue());
    BooleanSetting destroyCrystal = registerBoolean("Destroy Stuck Crystal", false, () -> breakSection.getValue() && antiCity.getValue());
    BooleanSetting destroyAboveCrystal = registerBoolean("Destroy Above Crystal", false, () -> breakSection.getValue() &&  antiCity.getValue());
    BooleanSetting allowNon1x1 = registerBoolean("Allow non 1x1", false, () -> breakSection.getValue() &&  antiCity.getValue());

    //endregion

    //region Misc
    BooleanSetting misc = registerBoolean("Misc Section", false);
    BooleanSetting switchHotbar = registerBoolean("Switch Crystal", false, () -> misc.getValue());
    BooleanSetting switchBack = registerBoolean("Switch Back", false,
            () -> misc.getValue() && switchHotbar.getValue());
    IntegerSetting tickSwitchBack = registerInteger("Tick Switch Back", 5, 0, 50,
            () -> misc.getValue() && switchHotbar.getValue() && switchBack.getValue());
    BooleanSetting waitGappleSwitch = registerBoolean("Wait Gapple Switch", false,
            () -> misc.getValue() && switchHotbar.getValue() && stopGapple.getValue());
    BooleanSetting silentSwitch = registerBoolean("Silent Switch", false,
            () -> misc.getValue() && switchHotbar.getValue());
    //endregion

    //region Render
    BooleanSetting renders = registerBoolean("Renders", false);
    ModeSetting typePlace = registerMode("Render Place", Arrays.asList("None", "Outline", "Fill", "Both"), "Both", () -> renders.getValue());
    ModeSetting placeDimension = registerMode("Place Dimension", Arrays.asList("Box", "Flat", "Slab", "Circle"), "Box", () -> renders.getValue() && !typePlace.getValue().equals("None"));
    DoubleSetting rangeCirclePl = registerDouble("Range Circle Pl", .5, .1, 1.5, () -> renders.getValue() && placeDimension.getValue().equals("Circle"));
    DoubleSetting slabHeightPlace = registerDouble("Slab height Place", .2, 0, 1, () -> renders.getValue() && placeDimension.getValue().equals("Slab"));

    //region outline custom place
    // Custom outline
    BooleanSetting OutLineSection = registerBoolean("OutLine Section Custom pl", false,
            () ->  (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both")) && renders.getValue());
    IntegerSetting outlineWidthpl = registerInteger("Outline Width", 5, 1, 5,
            () -> (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both")) && renders.getValue() && OutLineSection.getValue());
    // Bottom
    ModeSetting NVerticesOutlineBot = registerMode("N^ Vertices Outline Bot pl", Arrays.asList("1", "2", "4"), "1",
            () -> (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both")) && (OutLineSection.getValue() && renders.getValue()));
    ModeSetting direction2OutLineBot = registerMode("Direction Outline Bot pl", Arrays.asList("X", "Z"), "X",
            () ->   (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both")) &&
                    (OutLineSection.getValue() ) && NVerticesOutlineBot.getValue().equals("2") );
    ColorSetting firstVerticeOutlineBot = registerColor("1 Vert Out Bot pl", new GSColor(255, 16, 19, 50),
            () ->   (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && renders.getValue()  )
            , true);
    ColorSetting secondVerticeOutlineBot = registerColor("2 Vert Out Bot pl", new GSColor(0, 0, 255, 50),
            () ->   (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && renders.getValue())
                    && (NVerticesOutlineBot.getValue().equals("2") || NVerticesOutlineBot.getValue().equals("4")), true);
    ColorSetting thirdVerticeOutlineBot = registerColor("3 Vert Out Bot pl", new GSColor(0, 255, 128, 50),
            () ->   (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && renders.getValue())
                    && NVerticesOutlineBot.getValue().equals("4"), true);
    ColorSetting fourVerticeOutlineBot = registerColor("4 Vert Out Bot pl", new GSColor(255, 255, 2, 50),
            () ->   (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && renders.getValue())
                    && NVerticesOutlineBot.getValue().equals("4"), true);
    // Top
    ModeSetting NVerticesOutlineTop = registerMode("N^ Vertices Outline Top pl", Arrays.asList("1", "2", "4"), "1",
            () ->   (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both"))  &&
                    (OutLineSection.getValue() && renders.getValue() ));
    ModeSetting direction2OutLineTop = registerMode("Direction Outline Top pl", Arrays.asList("X", "Z"), "X",
            () ->   (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && renders.getValue()) && NVerticesOutlineTop.getValue().equals("2"));
    ColorSetting firstVerticeOutlineTop = registerColor("1 Vert Out Top pl", new GSColor(255, 16, 19, 50),
            () ->   (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && renders.getValue()), true);
    ColorSetting secondVerticeOutlineTop = registerColor("2 Vert Out Top pl", new GSColor(0, 0, 255, 50),
            () ->   (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && renders.getValue())
                    && (NVerticesOutlineTop.getValue().equals("2") || NVerticesOutlineTop.getValue().equals("4")), true);
    ColorSetting thirdVerticeOutlineTop = registerColor("3 Vert Out Top pl", new GSColor(0, 255, 128, 50),
            () ->   (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && renders.getValue())
                    && NVerticesOutlineTop.getValue().equals("4"), true);
    ColorSetting fourVerticeOutlineTop = registerColor("4 Vert Out Top pl", new GSColor(255, 255, 2, 50),
            () ->   (typePlace.getValue().equals("Outline") || typePlace.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && renders.getValue())
                    && NVerticesOutlineTop.getValue().equals("4"), true);
    //endregion
    // region fill custom place
    BooleanSetting FillSection = registerBoolean("Fill Section Custom pl", false,
            () ->  (typePlace.getValue().equals("Fill") || typePlace.getValue().equals("Both")) && renders.getValue());
    // Bottom
    ModeSetting NVerticesFillBot = registerMode("N^ Vertices Fill Bot pl", Arrays.asList("1", "2", "4"), "1",
            () -> (typePlace.getValue().equals("Fill") || typePlace.getValue().equals("Both")) && FillSection.getValue() && renders.getValue());
    ModeSetting direction2FillBot = registerMode("Direction Fill Bot pl", Arrays.asList("X", "Z"), "X",
            () ->   (typePlace.getValue().equals("Fill") || typePlace.getValue().equals("Both")) &&
                    FillSection.getValue() && NVerticesFillBot.getValue().equals("2") && renders.getValue());
    ColorSetting firstVerticeFillBot = registerColor("1 Vert Fill Bot pl", new GSColor(17, 89, 100, 50),
            () ->   (typePlace.getValue().equals("Fill") || typePlace.getValue().equals("Both")) &&
                    FillSection.getValue() && renders.getValue()
            , true);
    ColorSetting secondVerticeFillBot = registerColor("2 Vert Fill Bot pl", new GSColor(0, 0, 255, 50),
            () ->   (typePlace.getValue().equals("Fill") || typePlace.getValue().equals("Both")) &&
                    FillSection.getValue() && renders.getValue()
                    && (NVerticesFillBot.getValue().equals("2") || NVerticesFillBot.getValue().equals("4")), true);
    ColorSetting thirdVerticeFillBot = registerColor("3 Vert Fill Bot pl", new GSColor(0, 255, 128, 50),
            () ->   (typePlace.getValue().equals("Fill") || typePlace.getValue().equals("Both")) &&
                    FillSection.getValue() && renders.getValue()
                    && NVerticesFillBot.getValue().equals("4"), true);
    ColorSetting fourVerticeFillBot = registerColor("4 Vert Fill Bot pl", new GSColor(255, 255, 2, 50),
            () ->   (typePlace.getValue().equals("Fill") || typePlace.getValue().equals("Both")) &&
                    FillSection.getValue() && renders.getValue()
                    && NVerticesFillBot.getValue().equals("4"), true);
    // Top
    ModeSetting NVerticesFillTop = registerMode("N^ Vertices Fill Top pl", Arrays.asList("1", "2", "4"), "1",
            () ->   (typePlace.getValue().equals("Fill") || typePlace.getValue().equals("Both")) &&
                    FillSection.getValue() && renders.getValue());
    ModeSetting direction2FillTop = registerMode("Direction Fill Top pl", Arrays.asList("X", "Z"), "X",
            () ->   (typePlace.getValue().equals("Fill") || typePlace.getValue().equals("Both")) &&
                    FillSection.getValue() && NVerticesFillTop.getValue().equals("2") && renders.getValue());
    ColorSetting firstVerticeFillTop = registerColor("1 Vert Fill Top pl", new GSColor(255, 16, 19, 50),
            () ->   (typePlace.getValue().equals("Fill") || typePlace.getValue().equals("Both")) &&
                    FillSection.getValue() && renders.getValue(), true);
    ColorSetting secondVerticeFillTop = registerColor("2 Vert Fill Top pl", new GSColor(0, 0, 255, 50),
            () ->   (typePlace.getValue().equals("Fill") || typePlace.getValue().equals("Both")) &&
                    FillSection.getValue() && renders.getValue()
                    && (NVerticesFillTop.getValue().equals("2") || NVerticesFillTop.getValue().equals("4")), true);
    ColorSetting thirdVerticeFillTop = registerColor("3 Vert Fill Top pl", new GSColor(0, 255, 128, 50),
            () ->   (typePlace.getValue().equals("Fill") || typePlace.getValue().equals("Both")) &&
                    FillSection.getValue() && renders.getValue()
                    && NVerticesFillTop.getValue().equals("4"), true);
    ColorSetting fourVerticeFillTop = registerColor("4 Vert Fill Top pl", new GSColor(255, 255, 2, 50),
            () ->   (typePlace.getValue().equals("Fill") || typePlace.getValue().equals("Both")) &&
                    FillSection.getValue() && renders.getValue()
                    && NVerticesFillTop.getValue().equals("4"), true);
    //endregion

    ModeSetting typeBreak = registerMode("Render Break", Arrays.asList("None", "Outline", "Fill", "Both"), "Both", () -> renders.getValue());
    ModeSetting breakDimension = registerMode("Break Dimension", Arrays.asList("Box", "Flat", "Slab", "Circle"), "Box", () -> renders.getValue() & !typeBreak.getValue().equals("None"));
    DoubleSetting rangeCircleBr = registerDouble("Range Circle Br", .5, .1, 1.5, () -> renders.getValue() && breakDimension.getValue().equals("Circle"));
    DoubleSetting slabHeightBreak = registerDouble("Slab height Break", .2, 0, 1, () -> renders.getValue() && breakDimension.getValue().equals("Slab"));

    //region outline custom place
    // Custom outline
    BooleanSetting OutLineSectionbr = registerBoolean("OutLine Section Custom br", false,
            () ->  (typeBreak.getValue().equals("Outline") || typeBreak.getValue().equals("Both")) && renders.getValue());
    // Bottom
    ModeSetting NVerticesOutlineBotbr = registerMode("N^ Vertices Outline Bot br", Arrays.asList("1", "2", "4"), "1",
            () -> (typeBreak.getValue().equals("Outline") || typeBreak.getValue().equals("Both")) && (OutLineSectionbr.getValue() && renders.getValue()));
    ModeSetting direction2OutLineBotbr = registerMode("Direction Outline Bot br", Arrays.asList("X", "Z"), "X",
            () ->   (typeBreak.getValue().equals("Outline") || typeBreak.getValue().equals("Both")) &&
                    (OutLineSectionbr.getValue() && renders.getValue()) && NVerticesOutlineBotbr.getValue().equals("2"));
    ColorSetting firstVerticeOutlineBotbr = registerColor("1 Vert Out Bot br", new GSColor(16, 50, 100, 255),
            () ->   (typeBreak.getValue().equals("Outline") || typeBreak.getValue().equals("Both")) &&
                    (OutLineSectionbr.getValue() && renders.getValue())
            , true);
    ColorSetting secondVerticeOutlineBotbr = registerColor("2 Vert Out Bot br", new GSColor(0, 0, 255, 50),
            () ->   (typeBreak.getValue().equals("Outline") || typeBreak.getValue().equals("Both")) &&
                    (OutLineSectionbr.getValue() && renders.getValue())
                    && (NVerticesOutlineBotbr.getValue().equals("2") || NVerticesOutlineBotbr.getValue().equals("4")), true);
    ColorSetting thirdVerticeOutlineBotbr = registerColor("3 Vert Out Bot br", new GSColor(0, 255, 128, 50),
            () ->   (typeBreak.getValue().equals("Outline") || typeBreak.getValue().equals("Both")) &&
                    (OutLineSectionbr.getValue() && renders.getValue())
                    && NVerticesOutlineBotbr.getValue().equals("4"), true);
    ColorSetting fourVerticeOutlineBotbr = registerColor("4 Vert Out Bot br", new GSColor(255, 255, 2, 50),
            () ->   (typeBreak.getValue().equals("Outline") || typeBreak.getValue().equals("Both")) &&
                    (OutLineSectionbr.getValue() && renders.getValue())
                    && NVerticesOutlineBotbr.getValue().equals("4"), true);
    // Top
    ModeSetting NVerticesOutlineTopbr = registerMode("N^ Vertices Outline Top br", Arrays.asList("1", "2", "4"), "1",
            () ->   (typeBreak.getValue().equals("Outline") || typeBreak.getValue().equals("Both")) &&
                    (OutLineSectionbr.getValue() && renders.getValue()));
    ModeSetting direction2OutLineTopbr = registerMode("Direction Outline Top br", Arrays.asList("X", "Z"), "X",
            () ->   (typeBreak.getValue().equals("Outline") || typeBreak.getValue().equals("Both")) &&
                    (OutLineSectionbr.getValue() && renders.getValue()) && NVerticesOutlineTopbr.getValue().equals("2"));
    ColorSetting firstVerticeOutlineTopbr = registerColor("1 Vert Out Top br", new GSColor(255, 16, 19, 255),
            () ->   (typeBreak.getValue().equals("Outline") || typeBreak.getValue().equals("Both")) &&
                    (OutLineSectionbr.getValue() && renders.getValue()), true);
    ColorSetting secondVerticeOutlineTopbr = registerColor("2 Vert Out Top br", new GSColor(0, 0, 255, 50),
            () ->   (typeBreak.getValue().equals("Outline") || typeBreak.getValue().equals("Both")) &&
                    (OutLineSectionbr.getValue() && renders.getValue())
                    && (NVerticesOutlineTopbr.getValue().equals("2") || NVerticesOutlineTopbr.getValue().equals("4")), true);
    ColorSetting thirdVerticeOutlineTopbr = registerColor("3 Vert Out Top br", new GSColor(0, 255, 128, 50),
            () ->   (typeBreak.getValue().equals("Outline") || typeBreak.getValue().equals("Both")) &&
                    (OutLineSectionbr.getValue() && renders.getValue())
                    && NVerticesOutlineTopbr.getValue().equals("4"), true);
    ColorSetting fourVerticeOutlineTopbr = registerColor("4 Vert Out Top br", new GSColor(255, 255, 2, 50),
            () ->   (typeBreak.getValue().equals("Outline") || typeBreak.getValue().equals("Both")) &&
                    (OutLineSectionbr.getValue() && renders.getValue())
                    && NVerticesOutlineTopbr.getValue().equals("4"), true);
    //endregion
    // region fill custom Break
    BooleanSetting FillSectionbr = registerBoolean("Fill Section Custom br", false,
            () ->  (typeBreak.getValue().equals("Fill") || typeBreak.getValue().equals("Both")) && renders.getValue());
    // Bottom
    ModeSetting NVerticesFillBotbr = registerMode("N^ Vertices Fill Bot br", Arrays.asList("1", "2", "4"), "1",
            () -> (typeBreak.getValue().equals("Fill") || typeBreak.getValue().equals("Both")) && FillSectionbr.getValue());
    ModeSetting direction2FillBotbr = registerMode("Direction Fill Bot br", Arrays.asList("X", "Z"), "X",
            () ->   (typeBreak.getValue().equals("Fill") || typeBreak.getValue().equals("Both")) &&
                    FillSectionbr.getValue() && NVerticesFillBotbr.getValue().equals("2"));
    ColorSetting firstVerticeFillBotbr = registerColor("1 Vert Fill Bot br", new GSColor(17, 89, 100, 50),
            () ->   (typeBreak.getValue().equals("Fill") || typeBreak.getValue().equals("Both")) &&
                    FillSectionbr.getValue() && renders.getValue()
            , true);
    ColorSetting secondVerticeFillBotbr = registerColor("2 Vert Fill Bot br", new GSColor(0, 0, 255, 50),
            () ->   (typeBreak.getValue().equals("Fill") || typeBreak.getValue().equals("Both")) &&
                    FillSectionbr.getValue() && renders.getValue()
                    && (NVerticesFillBotbr.getValue().equals("2") || NVerticesFillBotbr.getValue().equals("4")), true);
    ColorSetting thirdVerticeFillBotbr = registerColor("3 Vert Fill Bot br", new GSColor(0, 255, 128, 50),
            () ->   (typeBreak.getValue().equals("Fill") || typeBreak.getValue().equals("Both")) &&
                    FillSectionbr.getValue() && renders.getValue()
                    && NVerticesFillBotbr.getValue().equals("4"), true);
    ColorSetting fourVerticeFillBotbr = registerColor("4 Vert Fill Bot br", new GSColor(255, 255, 2, 50),
            () ->   (typeBreak.getValue().equals("Fill") || typeBreak.getValue().equals("Both")) &&
                    FillSectionbr.getValue() && renders.getValue()
                    && NVerticesFillBot.getValue().equals("4"), true);
    // Top
    ModeSetting NVerticesFillTopbr = registerMode("N^ Vertices Fill Top br", Arrays.asList("1", "2", "4"), "1",
            () ->   (typeBreak.getValue().equals("Fill") || typeBreak.getValue().equals("Both")) &&
                    FillSectionbr.getValue() && renders.getValue());
    ModeSetting direction2FillTopbr = registerMode("Direction Fill Top br", Arrays.asList("X", "Z"), "X",
            () ->   (typeBreak.getValue().equals("Fill") || typeBreak.getValue().equals("Both")) &&
                    FillSectionbr.getValue() && NVerticesFillTopbr.getValue().equals("2") && renders.getValue());
    ColorSetting firstVerticeFillTopbr = registerColor("1 Vert Fill Top br", new GSColor(255, 16, 19, 50),
            () ->   (typeBreak.getValue().equals("Fill") || typeBreak.getValue().equals("Both")) &&
                    FillSectionbr.getValue(), true);
    ColorSetting secondVerticeFillTopbr = registerColor("2 Vert Fill Top br", new GSColor(0, 0, 255, 50),
            () ->   (typeBreak.getValue().equals("Fill") || typeBreak.getValue().equals("Both")) &&
                    FillSectionbr.getValue() && renders.getValue()
                    && (NVerticesFillTopbr.getValue().equals("2") || NVerticesFillTopbr.getValue().equals("4")), true);
    ColorSetting thirdVerticeFillTopbr = registerColor("3 Vert Fill Top br", new GSColor(0, 255, 128, 50),
            () ->   (typeBreak.getValue().equals("Fill") || typeBreak.getValue().equals("Both")) &&
                    FillSectionbr.getValue() && renders.getValue()
                    && NVerticesFillTopbr.getValue().equals("4"), true);
    ColorSetting fourVerticeFillTopbr = registerColor("4 Vert Fill Top br", new GSColor(255, 255, 2, 50),
            () ->   (typeBreak.getValue().equals("Fill") || typeBreak.getValue().equals("Both")) &&
                    FillSectionbr.getValue() && renders.getValue()
                    && NVerticesFillTopbr.getValue().equals("4"), true);
    //endregion

    BooleanSetting showTextpl = registerBoolean("Show text Place", true, () -> renders.getValue());
    ColorSetting colorPlaceText = registerColor("Color Place Text", new GSColor(0, 255, 255),
            () -> renders.getValue() && showTextpl.getValue(), true);
    DoubleSetting textYPlace = registerDouble("Text Y Place", .5, -1, 1, () -> renders.getValue() && showTextpl.getValue());
    BooleanSetting showTextbr = registerBoolean("Show text Brea", true, () -> renders.getValue());
    ColorSetting colorBreakText = registerColor("Color Break Text", new GSColor(0, 255, 255),
            () -> renders.getValue() && showTextbr.getValue(), true);
    DoubleSetting textYBreak = registerDouble("Text Y Break", .5, -1, 1,
            () -> renders.getValue() && showTextbr.getValue());

    BooleanSetting movingPlace = registerBoolean("Moving Place", false, () -> renders.getValue());
    DoubleSetting movingPlaceSpeed = registerDouble("Moving Place Speed", 0.1, 0.01, 0.5, () -> renders.getValue() && movingPlace.getValue());
    BooleanSetting movingBreak = registerBoolean("Moving Break", false, () -> renders.getValue());
    DoubleSetting movingBreakSpeed = registerDouble("Moving Break Speed", 0.1, 0.01, 0.5, () -> renders.getValue() && movingPlace.getValue());

    IntegerSetting extendedPlace = registerInteger("Extended place", 5, 0, 20, () -> renders.getValue());
    IntegerSetting extendedBreak = registerInteger("Extended break", 5, 0, 20, () -> renders.getValue());

    BooleanSetting fadeCapl = registerBoolean("Fade Ca pl", true, () -> renders.getValue());
    IntegerSetting endFadePlace = registerInteger("End Fade Place pl", 0, 0, 255, () -> renders.getValue() && fadeCapl.getValue());
    BooleanSetting fadeCabr = registerBoolean("Fade Ca br", true, () -> renders.getValue());
    IntegerSetting endFadeBreak = registerInteger("End Fade Break pl", 0, 0, 255, () -> renders.getValue() && fadeCabr.getValue());
    IntegerSetting lifeTime = registerInteger("Life Time", 3000, 0, 5000, () -> renders.getValue() && (fadeCapl.getValue() || fadeCabr.getValue()));
    BooleanSetting placeDominant = registerBoolean("Place Dominant", false, () -> renders.getValue() && !(typePlace.getValue().equals("None") && typeBreak.getValue().equals("None")));

    BooleanSetting circleRender = registerBoolean("Circle Render", false, () -> renders.getValue());
    IntegerSetting life = registerInteger("Life", 300, 0, 1000, () -> renders.getValue() && circleRender.getValue());
    DoubleSetting circleRange = registerDouble("Circle Range", 1, 0, 3, () -> renders.getValue() && circleRender.getValue());
    ColorSetting color = registerColor("Color", new GSColor(255, 255, 255, 255), () -> renders.getValue() && circleRender.getValue(), true);
    BooleanSetting desyncCircle = registerBoolean("Desync Circle", false, () -> renders.getValue() && circleRender.getValue());
    IntegerSetting stepRainbowCircle = registerInteger("Step Rainbow Circle", 1, 1, 100, () -> renders.getValue() && circleRender.getValue());
    BooleanSetting increaseHeight = registerBoolean("Increase Height", true, () -> renders.getValue() && circleRender.getValue());
    DoubleSetting speedIncrease = registerDouble("Speed Increase", 0.01, 0.3, 0.001, () -> renders.getValue() && circleRender.getValue());

    //endregion

    //region Predict
    BooleanSetting predictSection = registerBoolean("Predict Section", false);
    BooleanSetting predictSurround = registerBoolean("Predict Surround", false,
            () -> predictSection.getValue());
    BooleanSetting predictPacketSurround = registerBoolean("Predict Packet Surround", false,
            () -> predictSection.getValue() && predictSurround.getValue());
    IntegerSetting percentSurround = registerInteger("Percent Surround", 80, 0, 100,
            () -> predictSection.getValue() && predictSurround.getValue() && !predictPacketSurround.getValue());
    IntegerSetting tickPacketBreak = registerInteger("Tick Packet Break", 40, 0, 100,
            () -> predictSection.getValue() && predictSurround.getValue() && predictPacketSurround.getValue());
    IntegerSetting tickMaxPacketBreak = registerInteger("Tick Max Packet Break", 40, 0, 150,
            () -> predictSection.getValue() && predictSurround.getValue() && predictPacketSurround.getValue());
    DoubleSetting maxSelfDamageSur = registerDouble("Max Self Dam Sur", 7, 0, 20,
            () -> predictSection.getValue() && predictSurround.getValue());
    BooleanSetting predictSelfPlace = registerBoolean("Predict Self Place", false, () -> predictSection.getValue());
    BooleanSetting showSelfPredictPlace = registerBoolean("Show Self Predict Place", false,
            () -> predictSection.getValue() && predictSelfPlace.getValue() );
    ColorSetting colorSelfPlace = registerColor("Color Self Place", new GSColor(0, 255, 255),
            () -> predictSection.getValue() && predictSelfPlace.getValue() && showSelfPredictPlace.getValue());
    BooleanSetting predictPlaceEnemy = registerBoolean("Predict Place Enemy", false, () -> predictSection.getValue());
    ColorSetting showColorPredictEnemyPlace = registerColor("Color Place Predict Enemy", new GSColor(255, 160, 0),
            () -> predictSection.getValue() && predictPlaceEnemy.getValue());
    BooleanSetting predictSelfDBreaking = registerBoolean("Predict Self Break", false, () -> predictSection.getValue());
    BooleanSetting showSelfPredictBreaking = registerBoolean("Show Self Predict Break", false,
            () -> predictSection.getValue() && predictSelfPlace.getValue() );
    ColorSetting colorSelfBreaking = registerColor("Color Self Break", new GSColor(0, 255, 255),
            () -> predictSection.getValue() && predictSelfPlace.getValue() && showSelfPredictPlace.getValue());
    BooleanSetting predictBreakingEnemy = registerBoolean("Predict Break Enemy", false, () -> predictSection.getValue());
    ColorSetting showColorPredictEnemyBreaking = registerColor("Color Break Predict Enemy", new GSColor(255, 160, 0),
            () -> predictSection.getValue() && predictPlaceEnemy.getValue());
    IntegerSetting tickPredict = registerInteger("Tick Predict", 8, 0, 30, () -> predictSection.getValue());
    BooleanSetting calculateYPredict = registerBoolean("Calculate Y Predict", true, () -> predictSection.getValue());
    IntegerSetting startDecrease = registerInteger("Start Decrease", 39, 0, 200, () -> predictSection.getValue() && calculateYPredict.getValue());
    IntegerSetting exponentStartDecrease = registerInteger("Exponent Start", 2, 1, 5,
            () -> predictSection.getValue() && calculateYPredict.getValue());
    IntegerSetting decreaseY = registerInteger("Decrease Y", 2, 1, 5,
            () -> predictSection.getValue() && calculateYPredict.getValue());
    IntegerSetting exponentDecreaseY = registerInteger("Exponent Decrease Y", 1, 1, 3,
            () -> predictSection.getValue() && calculateYPredict.getValue());
    IntegerSetting increaseY = registerInteger("Increase Y", 3, 1, 5,
            () -> predictSection.getValue() && calculateYPredict.getValue());
    IntegerSetting exponentIncreaseY = registerInteger("Exponent Increase Y", 2, 1, 3,
            () -> predictSection.getValue() && calculateYPredict.getValue());
    BooleanSetting splitXZ = registerBoolean("Split XZ", true, () -> predictSection.getValue());
    IntegerSetting widthPredict = registerInteger("Line Width", 2, 1, 5, () -> predictSection.getValue());
    BooleanSetting manualOutHole = registerBoolean("Manual Out Hole", false, () -> predictSection.getValue());
    BooleanSetting aboveHoleManual = registerBoolean("Above Hole Manual", false,
            () -> predictSection.getValue() && manualOutHole.getValue());
    BooleanSetting stairPredict = registerBoolean("Stair Predict", false, () -> predictSection.getValue());
    IntegerSetting nStair = registerInteger("N Stair", 2, 1, 4, () -> predictSection.getValue() && stairPredict.getValue());
    DoubleSetting speedActivationStair = registerDouble("Speed Activation Stair", .11, 0, 1, () -> predictSection.getValue() && stairPredict.getValue());
    //endregion

    //region Threading
    BooleanSetting threading = registerBoolean("Threading Section", false);
    IntegerSetting nThread = registerInteger("N Thread", 4, 1, 20, () -> threading.getValue());
    IntegerSetting maxTarget = registerInteger("Max Target", 5, 1, 30, () -> threading.getValue());
    IntegerSetting placeTimeout = registerInteger("Place Timeout", 100, 0, 1000, () -> threading.getValue());
    IntegerSetting predictPlaceTimeout = registerInteger("Predict Place Timeout", 100, 0, 1000, () -> threading.getValue());
    IntegerSetting breakTimeout = registerInteger("Break Timeout", 100, 0, 1000, () -> threading.getValue());
    IntegerSetting predictBreakTimeout = registerInteger("Predict Break Timeout", 100, 0, 1000, () -> threading.getValue());
    //endregion

    //region Strict
    BooleanSetting strict = registerBoolean("Strict Section", false);
    BooleanSetting raytrace = registerBoolean("Raytrace", false, () -> strict.getValue());
    BooleanSetting rotate = registerBoolean("Rotate", false, () -> strict.getValue());
    BooleanSetting preRotate = registerBoolean("Pre Rotate", false, () -> strict.getValue() && rotate.getValue());
    IntegerSetting tickAfterRotation = registerInteger("Tick After Rotation", 0, 0, 10,
            () -> strict.getValue() && rotate.getValue());
    ModeSetting focusPlaceType = registerMode("Focus Place Type", Arrays.asList("Disabled", "Tick", "Time"), "Disabled"
    , () -> strict.getValue());
    BooleanSetting recalculateDamage = registerBoolean("Recalculate Damage", true,
            () -> strict.getValue() && !focusPlaceType.getValue().equals("Disabled"));
    IntegerSetting tickWaitFocusPlace = registerInteger("Tick Wait Focus Pl", 4, 0, 20,
            () -> strict.getValue() && focusPlaceType.getValue().equals("Tick"));
    IntegerSetting timeWaitFocusPlace = registerInteger("Time Wait Focus Pl", 100, 0, 2000,
            () -> strict.getValue() && focusPlaceType.getValue().equals("Time"));
    BooleanSetting yawCheck = registerBoolean("Yaw Check", false,
            () -> strict.getValue());
    IntegerSetting yawStep = registerInteger("Yaw Step", 40, 0, 180,
            () -> strict.getValue() && yawCheck.getValue());
    BooleanSetting pitchCheck = registerBoolean("Pitch Check", false,
            () -> strict.getValue() );
    IntegerSetting pitchStep = registerInteger("Pitch Step", 40, 0, 180,
            () -> strict.getValue() && pitchCheck.getValue());
    BooleanSetting placeStrictDirection = registerBoolean("Place Strict Predict", false,
            () -> strict.getValue() && (pitchCheck.getValue() || yawCheck.getValue()));
    BooleanSetting predictBreakRotation = registerBoolean("Predict Break Rotation", false, () -> strict.getValue() && (pitchCheck.getValue() || yawCheck.getValue()));
    BooleanSetting blockRotation = registerBoolean("Block Rotation", true,
            () -> strict.getValue() && (pitchCheck.getValue() || yawCheck.getValue()));
    //endregion

    //region Debug
    BooleanSetting debugMenu = registerBoolean("Debug Section", false);
    BooleanSetting timeCalcPlacement = registerBoolean("Calc Placement Time", false, () -> debugMenu.getValue());
    BooleanSetting timeCalcBreaking = registerBoolean("Calc Breaking Time", false, () -> debugMenu.getValue());
    IntegerSetting nCalc = registerInteger("N Calc", 100, 1, 1000,
            () -> debugMenu.getValue() && (timeCalcPlacement.getValue() || timeCalcBreaking.getValue()));
    BooleanSetting debugPredict = registerBoolean("Debug Predict", false, () -> debugMenu.getValue());
    BooleanSetting showPredictions = registerBoolean("Show Predictions", false, () -> debugMenu.getValue() && debugPredict.getValue());
    //endregion

    //region HudDisplay

    BooleanSetting hudDisplayShow = registerBoolean("Hud Display Section", false);
    BooleanSetting showPlaceName = registerBoolean("Show Place Name", false, () -> hudDisplayShow.getValue());
    BooleanSetting showPlaceDamage = registerBoolean("Show Place Damage", false, () -> hudDisplayShow.getValue());
    BooleanSetting showPlaceCrystalsSecond = registerBoolean("Show c/s place", false, () -> hudDisplayShow.getValue());
    BooleanSetting cleanPlace = registerBoolean("Clean Place", true, () -> hudDisplayShow.getValue());
    BooleanSetting showBreakName = registerBoolean("Show break Name", false, () -> hudDisplayShow.getValue());
    BooleanSetting showBreakDamage = registerBoolean("Show break Damage", false, () -> hudDisplayShow.getValue());
    BooleanSetting showBreakCrystalsSecond = registerBoolean("Show c/s break", false, () -> hudDisplayShow.getValue());
    BooleanSetting cleanBreak = registerBoolean("Clean break", true, () -> hudDisplayShow.getValue());

    //endregion

    //region Binds
    StringSetting letterIgnoreTerrain = registerString("Ignore Terrain", "");
    StringSetting forceFacePlace = registerString("Force FacePlace", "");
    StringSetting anvilCity = registerString("Anvil City", "");
    IntegerSetting placeAnvil = registerInteger("Place Anvil", 10, 0, 100);
    //endregion
    //endregion

    //region Global variables

    static class renderClass {
        final int id;
        long start;
        final long life;
        final double circleRange;
        final GSColor color;
        final boolean desyncCircle;
        final int stepRainbowCircle;
        final double range;
        final int desync;
        final boolean increaseHeight;
        final double speedIncrease;
        double nowHeigth = 0;
        boolean up = true;


        public renderClass(int id, long life, GSColor color, double circleRange, boolean desyncCircle, int stepRainbowCircle, double range, int desync, boolean increaseHeight, double speedIncrease) {
            this.increaseHeight = increaseHeight;
            this.speedIncrease = speedIncrease;
            this.id = id;
            this.range = range;
            start = System.currentTimeMillis();
            this.life = life;
            this.desync = desync;
            this.circleRange = circleRange;
            this.color = color;
            this.desyncCircle = desyncCircle;
            this.stepRainbowCircle = stepRainbowCircle;
        }

        boolean update() {
            return System.currentTimeMillis() - start > life;
        }

        boolean reset(int id) {
            if (this.id == id) {
                start = System.currentTimeMillis();
                return true;
            }
            return false;
        }

        void render() {
            Entity e = mc.world.getEntityByID(id);
            if (e != null) {
                double inc = 0;
                if (increaseHeight) {
                    nowHeigth += speedIncrease * (up ? 1 : -1);
                    if (nowHeigth > e.height)
                        up = false;
                    else if (nowHeigth < 0)
                        up = true;
                    inc = nowHeigth;
                }
                if (desyncCircle) {
                    RenderUtil.drawCircle((float) e.posX, (float) (e.posY + inc), (float) e.posZ, range, desync, color.getAlpha());
                } else {
                    RenderUtil.drawCircle((float) e.posX, (float) (e.posY + inc), (float) e.posZ, range, color);
                }
            }
        }
    }

    // This is for comparing the distance between two players
    static class Sortbyroll implements Comparator<EntityPlayer> {

        @Override
        public int compare(EntityPlayer o1, EntityPlayer o2) {
            return (int) (o1.getDistanceSq(mc.player) - o2.getDistanceSq(mc.player));
        }
    }

    // This class is for displaying things
    static class display {

        AxisAlignedBB box;
        BlockPos block;
        final GSColor color;
        int width;
        int type;
        String[] text;
        double yDiff;

        // Draw box (hitbox)
        public display(AxisAlignedBB box, GSColor color, int width) {
            this.box = box;
            this.color = color;
            this.width = width;
            this.type = 0;
        }

        // Draw text
        public display(String text, BlockPos block, GSColor color, double yDiff) {
            this.text = new String[]{text};
            this.block = block;
            this.color = color;
            this.type = 1;
            this.yDiff = yDiff;
        }

        // Function for drawing
        void draw() {
            switch (type) {
                case 0:
                    RenderUtil.drawBoundingBox(box, width, color);
                    break;
                case 1:
                    RenderUtil.drawNametag((double) this.block.getX() + 0.5d, (double) this.block.getY() + yDiff, (double) this.block.getZ() + 0.5d, this.text, this.color, 1);
                    break;
            }
        }
    }

    // Class of the crystal time
    static class crystalTime {
        BlockPos posCrystal;
        int idCrystal = -100;
        final int type;
        int tick;
        int finishTick;
        long start;
        int finish;

        // Tick crystal
        public crystalTime(BlockPos posCrystal, int tick, int finishTick) {
            this.posCrystal = posCrystal;
            this.tick = tick;
            this.type = 0;
            this.finishTick = finishTick;
        }

        // Time crystal
        public crystalTime(BlockPos posCrystal, int finish) {
            this.posCrystal = posCrystal;
            this.start = System.currentTimeMillis();
            this.finish = finish;
            this.type = 1;
        }

        // Time Id
        public crystalTime(BlockPos pos, int id, int finish, boolean lol) {
            this.posCrystal = pos;
            this.idCrystal = id;
            this.start = System.currentTimeMillis();
            this.finish = finish;
            this.type = 1;
        }

        // Tick id
        public crystalTime(BlockPos pos, int id, int tick, int finishTick) {
            this.posCrystal = pos;
            this.idCrystal = id;
            this.tick = tick;
            this.type = 0;
            this.finishTick = finishTick;
        }

        // Check if the crystal is ready.
        boolean isReady() {
            switch (type) {
                // Tick
                case 0:
                    return ++tick >= this.finishTick;
                // Time
                case 1:
                    return System.currentTimeMillis() - this.start >= this.finish;
            }
            // This should never be reached
            return true;
        }

    }

    class crystalPlaceWait {

        // Here we have every crystals
        ArrayList<crystalTime> listWait = new ArrayList<>();

        // Add new crystal with time delay
        void addCrystal(BlockPos cryst, int finish) {
            listWait.add(new crystalTime(cryst,  finish));
        }

        // Add new crystal with tick delay
        void addCrystal(BlockPos cryst, @SuppressWarnings("SameParameterValue") int tick, int tickFinish) {
            removeCrystal((double) cryst.getX(), (double) cryst.getY(), (double) cryst.getZ());
            listWait.add(new crystalTime(cryst,  tick, tickFinish));
        }

        // Add new crystal with time delay
        void addCrystalId(BlockPos cryst, int id, int finish) {
            listWait.add(new crystalTime(cryst, id,  finish, false));
        }

        // Add new crystal with tick delay
        void addCrystalId(BlockPos cryst, int id, @SuppressWarnings("SameParameterValue") int tick, int tickFinish) {
            removeCrystal((double) cryst.getX(), (double) cryst.getY(), (double) cryst.getZ());
            listWait.add(new crystalTime(cryst, id,  tick, tickFinish));
        }

        // If exists, remove crystal at x y z
        boolean removeCrystal(Double x, Double y, Double z) {
            int i = CrystalExists(new BlockPos(x, y, z));
            if (i != -1) {
                listWait.remove(i);
                return true;
            }
            return false;
        }

        // Return the index of the crystal in the array. -1 if it doesnt exists
        int CrystalExists(BlockPos pos) {
            for(int i = 0; i < listWait.size(); i++)
                if ( sameBlockPos(pos, listWait.get(i).posCrystal))
                    return i;
            return -1;
        }

        boolean crystalIdExists(int id) {
            try {
                return listWait.stream().anyMatch(e -> e.idCrystal == id);
            } catch (NullPointerException | ConcurrentModificationException ignored) {
                return false;
            }
        }

        // Update every crystals timers
        void updateCrystals() {
            for(int i = 0; i < listWait.size(); i++) {
                try {
                    if (listWait.get(i).isReady()) {
                        listWait.remove(i);
                        i--;
                    }
                }
                catch(NullPointerException e) {
                    listWait.remove(i);
                    i--;
                }
            }
        }

        int countCrystals() {
            return listWait.size();
        }

    }

    // Wait for slowBreak
    static class slowBreakPlayers {
        final String name;
        int tick = Integer.MAX_VALUE;
        int finalTick;
        long start = Long.MAX_VALUE;
        int finish;

        // Just for storing the crystals. This is for tick
        public slowBreakPlayers(String name, int finalTick, boolean ignored) {
          this.name = name;
          this.finalTick = finalTick;
          this.tick = 0;
        }

        // Just storing crystal. This is time
        public slowBreakPlayers(String name, int finish) {
            this.name = name;
            this.finish = finish;
            this.start = System.currentTimeMillis();
        }

        // Update the crystal, return true if we are at the end
        boolean update() {
            if (tick == Integer.MAX_VALUE)
                return System.currentTimeMillis() - this.start >= this.finish;
            else return ++tick >= this.finalTick;
        }

    }

    // This is used by packet surround
    class packetBlock {
        public final BlockPos block;
        public int tick;
        public final int startTick;
        public final int finishTish;

        // Simple constructor tha store every informations
        public packetBlock(BlockPos block, int startTick, int finishTick) {
            this.block = block;
            this.tick = 0;
            this.startTick = startTick;
            this.finishTish = finishTick;
        }

        boolean update() {
            tick++;
            if (tick > startTick) {
                if (tick > finishTish)
                    return false;

                // Check for the placement
                // If we are eating, dont do it lol
                if (stopGapple(false)) {
                    return true;
                }

                // Get crystal hand
                EnumHand hand = getHandCrystal();
                // If no hand found
                if (hand == null)
                    return true;

                // If we cant place a crystal
                if (!CrystalUtil.canPlaceCrystal(block, newPlace.getValue()))
                    return true;

                // Place crystal and dont allow to place another crystal on the next tick
                placeCrystal(block, hand, false);
                placedCrystal = true;

            }
            return true;
        }
    }

    class crystalPlaced {
        // List of crystals placed
        ArrayList<crystalTime> endCrystalPlaced = new ArrayList<>();

        // Just add a new crystal with the blockPos
        void addCrystal(BlockPos pos) {
            // If we have another crystal at the same pos, just delete it
            endCrystalPlaced.removeIf(check -> sameBlockPos(check.posCrystal, pos));
            endCrystalPlaced.add(new crystalTime(pos, 5000));
        }

        // If we already have a crystal
        boolean hasCrystal(EntityEnderCrystal crystal) {
            // Get blockpos
            BlockPos now = crystal.getPosition().add(0, -1, 0);
            // Check blockpos
            return endCrystalPlaced.stream().anyMatch(
                    check -> sameBlockPos(check.posCrystal, now)
            );
        }

        boolean hasCrystal(BlockPos crystal) {
            return endCrystalPlaced.stream().anyMatch(
                    check -> sameBlockPos(check.posCrystal, crystal)
            );
        }

        // Update every crystals and delete the one that are ready
        void updateCrystals() {
            for(int i = 0; i < endCrystalPlaced.size(); i++) {
                if (endCrystalPlaced.get(i).isReady()) {
                    // I hate this kind of loop
                    endCrystalPlaced.remove(i);
                    i--;
                }
            }
        }

    }

    // This is used for the render
    class renderBlock {
        private final BlockPos pos;
        private long start;
        private final boolean place;

        public renderBlock(boolean place, BlockPos pos) {
            this.place = place;
            this.start = System.currentTimeMillis();
            this.pos = pos;
        }

        // Since we are using private, we have to use a function
        void resetTime() {
            this.start = System.currentTimeMillis();
        }

        // Render a block
        void render() {
            // IF place, render place, else render break
            if (place) {
                drawBoxMain(typePlace.getValue(), this.pos, placeDimension.getValue(), slabHeightPlace.getValue(), true, returnGradient());
            } else
            drawBoxMain(typeBreak.getValue(), this.pos, breakDimension.getValue(), slabHeightBreak.getValue(), false, returnGradient());
        }

        // This function return the gradient
        public int returnGradient() {
            long end = this.start + lifeTime.getValue();
            int result = (int) (((float) (end - System.currentTimeMillis()) / (end - this.start)) * 100);
            // We dont want a free crash, phantom would kill me lol. I want a tea
            if (result < 0)
                result = 0;
            int startFade, endFade;
            // What if i just start speaking italian? Nobody read comments lol
            // Since we use the same function for both place and break, we have to find a way to different them
            if (place) {
                startFade = firstVerticeFillBot.getValue().getAlpha();
                endFade = endFadePlace.getValue();
            } else {
                startFade = firstVerticeFillBotbr.getValue().getAlpha();
                endFade = endFadeBreak.getValue();
            }

            // Return the gradient
            return (int) (((double) startFade - endFade) * (result / 100.0));
        }
    }

    class managerClassRenderBlocks {
        // list of blocks we are rendering
        ArrayList<renderBlock> blocks = new ArrayList<>();

        // Update every blocks. If it passed a certain time, remove them
        void update(int time) {
            blocks.removeIf(e -> System.currentTimeMillis() - e.start > time);
        }

        // render every blocks
        void render() {
            blocks.forEach(e -> {
                // Reset time of blocks that we are going to place, dont render them ofc
                if ( (bestBreak.crystal != null && sameBlockPos(e.pos, bestBreak.crystal.getPosition().add(0, -1, 0)))
                    || ( bestPlace.crystal != null && sameBlockPos(e.pos, bestPlace.crystal)))
                    e.resetTime();
                else
                    // Render! Where is my pillow? I have cold
                    e.render();
            });
        }

        // Add new blocks to render
        void addRender(boolean place, BlockPos pos) {
            // I spent so much fucking time on this module lmao, but i'm happy
            // On the result. I'm sure this is one of the best ca,
            // I basically reached the limit of what a ca can have
            boolean render = true;
            // For every blocks
            // If we have something
            for (renderBlock block : blocks)
                if (sameBlockPos(block.pos, pos) && block.place == place) {
                    // Reset time and dont render after
                    render = false;
                    block.resetTime();
                    break;
                }
            // If we can render, add it
            if (render)
                blocks.add(new renderBlock(place, pos));
        }

    }

    /// Global variables sorted by type
    // Well, do i really have to comment all these things?
    public static boolean stopAC = false;

    boolean checkTimePlace, checkTimeBreak, placedCrystal, brokenCrystal,  isRotating;

    int oldSlot, tick = 0, tickBeforePlace = 0, tickBeforeBreak, slotChange, tickSwitch, oldSlotBackWeb, oldSlotObby, slotWebBack, highestId = -100000,
        placeRender, breakRender;

    double xPlayerRotation, yPlayerRotation;

    Timer timerPlace = new Timer();
    Timer timerBreak = new Timer();

    long timePlace = 0;
    long timeBreak = 0;

    Vec3d lastHitVec;

    /*
        A lot of shits lmao
        managers of things, a lot of things
     */
    crystalPlaceWait listCrystalsPlaced = new crystalPlaceWait();
    crystalPlaceWait listCrystalsSecondWait = new crystalPlaceWait();
    crystalPlaceWait crystalSecondPlace = new crystalPlaceWait();
    crystalPlaceWait breakPacketLimit = new crystalPlaceWait();
    crystalPlaceWait existsCrystal = new crystalPlaceWait();
    crystalPlaceWait crystalSecondBreak = new crystalPlaceWait();
    crystalPlaceWait attempedCrystalBreak = new crystalPlaceWait();
    managerClassRenderBlocks managerRenderBlocks = new managerClassRenderBlocks();
    crystalPlaced endCrystalPlaced = new crystalPlaced();
    crystalTime crystalPlace = null;
    EntityEnderCrystal forceBreak = null;
    BlockPos forceBreakPlace = null;

    /*
        Idk also these are managers but they mostly count things
     */
    ArrayList<display> toDisplay = new ArrayList<>();
    ArrayList<Long> durationsPlace = new ArrayList<>();
    ArrayList<Long> durationsBreaking = new ArrayList<>();
    ArrayList<packetBlock> packetsBlocks = new ArrayList<>();
    ArrayList<slowBreakPlayers> listPlayersBreak = new ArrayList<>();
    ArrayList<renderClass> toRender = new ArrayList<>();


    // Multithreading power!
    ThreadPoolExecutor executor =
            (ThreadPoolExecutor) Executors.newCachedThreadPool();

    // Well, we have bestPlace and bestBreak, right?
    CrystalInfo.PlaceInfo bestPlace = new CrystalInfo.PlaceInfo(-100, null, null, 100d);
    CrystalInfo.NewBreakInfo bestBreak = new CrystalInfo.NewBreakInfo(-100, null, null, 100d);

    // damage target crystal distance (lmao you can understand if i took this from another file by just the uppercase)
    float forcePlaceDamage;
    PlayerInfo forcePlaceTarget;
    BlockPos forcePlaceCrystal = null;

    //endregion

    //region Gamesense call

    public void onEnable() {
        // Just reset some variables
        tickBeforePlace = tickBeforeBreak = tick = 0;
        timePlace = timeBreak = 0;
        placeRender = breakRender = 0;
        oldSlotBackWeb = tickSwitch = slotWebBack = oldSlotObby = -1;
        checkTimePlace = placedCrystal = brokenCrystal = checkTimeBreak;
        yPlayerRotation = xPlayerRotation = Double.MAX_VALUE;
        forceBreak = null;
        forceBreakPlace = null;
        lastHitVec = null;
        // Idk shits happened
        bestPlace = new CrystalInfo.PlaceInfo(-100, null, null, 100d);
        bestBreak = new CrystalInfo.NewBreakInfo(-100, null, null, 100d);
        isRotating = isAnvilling = false;
        stopAC = false;
        crystalAnvil = null;
        highestId = 0;
        // Lmao
        String rickroll = "Never gonna give you up\n" +
                "            Never gonna let you down\n" +
                "            Never gonna run around and desert you\n" +
                "            Never gonna make you cry\n" +
                "            Never gonna say goodbye\n" +
                "            Never gonna tell a lie and hurt you";
    }

    public void onDisable() {
        // Idk shits happened and still happens
        bestPlace = new CrystalInfo.PlaceInfo(-100, null, null, 100d);
        bestBreak = new CrystalInfo.NewBreakInfo(-100, null, null, 100d);
        movingPlaceNow = new Vec3d(-1f, -1f, -1f);
        movingBreakNow = new Vec3d(-1f, -1f, -1f);
    }

    int tickEat = 0;

    boolean stopGapple(boolean decrease) {
        // If we are eating, stop
        if (stopGapple.getValue()) {
            Item item;
            if (
                    mc.player.isHandActive() && (
                            (item = mc.player.getHeldItemMainhand().getItem()) == Items.GOLDEN_APPLE || item == Items.CHORUS_FRUIT
                                    || (item = mc.player.getHeldItemOffhand().getItem()) == Items.GOLDEN_APPLE || item == Items.CHORUS_FRUIT)) {
                if (decrease)   tickEat = tickWaitEat.getValue();
                return true;
            }
            if (tickEat > 0) {
                if (decrease)
                    tickEat--;
                return true;
            }
        }
        return false;
    }

    void updateCounters() {
        // Well, here we just update every managers, do i really have to comment everything?
        listCrystalsPlaced.updateCrystals();
        listCrystalsSecondWait.updateCrystals();
        crystalSecondPlace.updateCrystals();
        endCrystalPlaced.updateCrystals();
        existsCrystal.updateCrystals();
        // PacketsBlocks is the only one that is not pretty lmao
        for(int i = 0; i < packetsBlocks.size(); i++) {
            if (!packetsBlocks.get(i).update()) {
                packetsBlocks.remove(i);
                i--;
            }

        }
        //toRender.removeIf(KillAura.renderClass::update);
        for(int i = 0; i < toRender.size(); i++)
            if (toRender.get(i).update()) {
                toRender.remove(i);
                i--;
            }
        breakPacketLimit.updateCrystals();
        listPlayersBreak.removeIf(slowBreakPlayers::update);
        crystalSecondBreak.updateCrystals();
        attempedCrystalBreak.updateCrystals();
        managerRenderBlocks.update(lifeTime.getValue());
    }

    // Simple onUpdate, boring
    public void onUpdate() {
        if (mc.world == null || mc.player == null || mc.player.isDead || stopAC) return;

        // Clear what we are displaying
        toDisplay.clear();

        // Update counters
        updateCounters();

        // If entityPredict, lets update highest id
        if (entityPredict.getValue())
            updateHighestID();

        // If we have to stop because of gapple
        if (stopGapple(true))
            return;

        // Start ca (And here we go! It's 02:30am, i want to sleep. Kinda feel alone ngl)
        /*
            These days have been really hard and ya, i missed coding.
            Coding and reading help me dealing with all the shits are happening,
            they help me distracting myself.
         */
        try {
            switch (logic.getValue()) {
                case "Place->Break":
                    if (!placeCrystals() || !oneStop.getValue())
                        breakCrystals();
                    break;
                case "Break->Place":
                    if (!breakCrystals() || !oneStop.getValue())
                        placeCrystals();
                    break;
                case "Place":
                    placeCrystals();
                    if (bestBreak.crystal != null)
                        bestBreak = new CrystalInfo.NewBreakInfo(0, null, null, 0);
                    break;
                case "Break":
                    breakCrystals();
                    if (bestPlace.crystal != null)
                        bestPlace = new CrystalInfo.PlaceInfo(0, null, null, 0);
                    break;
            }
        }catch (Exception e) {
            PistonCrystal.printDebug("Prevented a crash from the ca. If this repet, spam me in dm", true);
            final Logger LOGGER = LogManager.getLogger("GameSense");
            LOGGER.error("[AutoCrystalRewrite] error during the creation of the structure.");
            if (e.getMessage() != null)
                LOGGER.error("[AutoCrystalRewrite] error message: " + e.getClass().getName() + " " + e.getMessage());
            else
                LOGGER.error("[AutoCrystalRewrite] cannot find the cause");
            int i5 = 0;

            if (e.getStackTrace().length != 0) {
                LOGGER.error("[AutoCrystalRewrite] StackTrace Start");
                for (StackTraceElement errorMess : e.getStackTrace()) {
                    LOGGER.error("[AutoCrystalRewrite] " + errorMess.toString());
                }
                LOGGER.error("[AutoCrystalRewrite] StackTrace End");
            }
        }

        // Remember this slot. This is used for preventing the bug with normal switch (shit that only gs has patched lmao)

        oldSlot = mc.player.inventory.currentItem;

        PlacementUtil.stopSneaking();

    }

    // Display in the hud
    public String getHudInfo() {
        StringBuilder t = new StringBuilder();
        boolean place = false;

        // Just render things, nice looking come on
        if (bestPlace.target != null) {
            if (showPlaceName.getValue()) {
                t.append(ChatFormatting.GRAY + "[")
                 .append(ChatFormatting.WHITE + (!cleanPlace.getValue() ? "Place Name: " : ""))
                 .append(bestPlace.target.entity.getName());
                place = true;
            }

            if (showPlaceDamage.getValue()) {
                if (!place) {
                    t.append(ChatFormatting.GRAY + "[")
                     .append(ChatFormatting.WHITE + (!cleanPlace.getValue() ? "Place damage: " : ""))
                     .append((int) bestPlace.damage);
                    place = true;
                } else
                    t.append(!cleanPlace.getValue() ? " Damage: " : " ")
                     .append((int) bestPlace.damage);
            }

        }

        if (showPlaceCrystalsSecond.getValue()) {
            int temp;
            if ((temp = crystalSecondPlace.countCrystals()) > 0) {
                if (!place) {
                    t.append(ChatFormatting.GRAY + "[")
                            .append(ChatFormatting.WHITE + (cleanPlace.getValue() ? "Place c/s: " : ""))
                            .append(temp);
                    place = true;
                } else t.append(cleanPlace.getValue() ? " c/s: " : " ")
                        .append(temp);
            }

        }

        if (bestBreak.target != null) {
            if (showBreakName.getValue()) {
                if (!place) {
                    t.append(ChatFormatting.GRAY + "[")
                            .append(ChatFormatting.WHITE + (!cleanBreak.getValue() ? "Break Name: " : ""))
                            .append(bestBreak.target.entity.getName());
                    place = true;
                } else
                    t.append(!cleanPlace.getValue() ? " Name: " : " ")
                            .append(bestBreak.target.entity.getName());
            }

            if (showBreakDamage.getValue()) {
                if (!place) {
                    t.append(ChatFormatting.GRAY + "[")
                            .append(ChatFormatting.WHITE + (!cleanBreak.getValue() ? "Break damage: " : ""))
                            .append((int) bestBreak.damage);
                    place = true;
                } else
                    t.append(!cleanPlace.getValue() ? " Damage: " : " ")
                            .append((int) bestBreak.damage);
            }

        }

        if (showBreakCrystalsSecond.getValue()) {
            int temp;
            if ((temp = crystalSecondBreak.countCrystals()) > 0) {
                if (!place) {
                    t.append(ChatFormatting.GRAY + "[")
                            .append(ChatFormatting.WHITE + (cleanBreak.getValue() ? "Break b/s: " : ""))
                            .append(temp);
                    place = true;
                } else t.append(cleanPlace.getValue() ? " b/s: " : " ")
                        .append(temp);
            }

        }

        if (place)
            t.append(ChatFormatting.GRAY + "]");

        return t.toString();
    }

    //endregion

    //region Calculate Place Crystal

    boolean isAnvilling = false;
    BlockPos crystalAnvil = null;
    // Main function for calculating the best crystal
    CrystalInfo.PlaceInfo getTargetPlacing(String mode) {

        // AnvilCity
        if (this.anvilCity.getText().length() > 0)
            // If we are pressing a button
            if (Keyboard.isKeyDown(KeyBoardClass.getKeyFromChar(this.anvilCity.getText().charAt(0))) && bestBreak.damage > 5) {

                if (crystalAnvil != null && blockCity != null) {
                    if (BlockUtil.getBlock(blockCity) instanceof BlockAir) {
                        int slot = InventoryUtil.findFirstBlockSlot(Blocks.ANVIL.getClass(), 0, 8);
                        if (slot != -1) { // 622 2 357
                            int oldSlot = mc.player.inventory.currentItem;
                            // Place anvil
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                            PlacementUtil.place(blockCity, EnumHand.MAIN_HAND, rotate.getValue(), false);
                            // Return back
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
                        }
                    }
                }

                if (crystalAnvil != null) {
                    if (mc.world.getLoadedEntityList().stream().filter(e -> e instanceof EntityEnderCrystal && e.getPosition() == crystalAnvil).findAny().isPresent()) {
                        crystalAnvil = null;
                    } else {
                        return new CrystalInfo.PlaceInfo(8, null, crystalAnvil, 6d);
                    }
                }
            } else {isAnvilling = false; crystalAnvil = null;}
        else {isAnvilling = false; crystalAnvil = null;}



        // All this mess for just a little improvement lmao
        PredictUtil.PredictSettings settings = new PredictUtil.PredictSettings(tickPredict.getValue(), calculateYPredict.getValue(), startDecrease.getValue(), exponentStartDecrease.getValue(), decreaseY.getValue(), exponentDecreaseY.getValue(), increaseY.getValue(), exponentIncreaseY.getValue(), splitXZ.getValue(), widthPredict.getValue(), debugPredict.getValue(), showPredictions.getValue(), manualOutHole.getValue(), aboveHoleManual.getValue(), stairPredict.getValue(), nStair.getValue(), speedActivationStair.getValue());
        int nThread = this.nThread.getValue();
        float armourPercent = armourFacePlace.getValue() / 100.0f;
        double minDamage = this.minDamagePlace.getValue();
        double minFacePlaceDamage = this.minFacePlaceDmg.getValue();
        double minFacePlaceHp =  this.facePlaceValue.getValue();
        if (forceFacePlace.getText().length() > 0) {
            // I swear lwjgl is really a pain.
            if (Keyboard.isKeyDown(KeyBoardClass.getKeyFromChar(forceFacePlace.getText().charAt(0))))
                minFacePlaceHp = 36;
        }
        double enemyRangeSQ = rangeEnemyPlace.getValue() * rangeEnemyPlace.getValue();
        double maxSelfDamage = this.maxSelfDamagePlace.getValue();
        double wallRangePlaceSQ = this.crystalWallPlace.getValue() * this.crystalWallPlace.getValue();
        boolean raytraceValue = raytrace.getValue();
        int maxYTarget = this.maxYTarget.getValue();
        int minYTarget = this.minYTarget.getValue();
        int placeTimeout = this.placeTimeout.getValue();
        boolean ignoreTerrainValue = false;
        if (ignoreTerrain.getValue())
            if (bindIgnoreTerrain.getValue()) {
                if (letterIgnoreTerrain.getText().length() > 0)
                    if (Keyboard.isKeyDown(KeyBoardClass.getKeyFromChar(letterIgnoreTerrain.getText().charAt(0))))
                        ignoreTerrainValue = true;
            } else ignoreTerrainValue = true;

        boolean relativeDamage = this.relativeDamagePlace.getValue();
        double valueRelativeDamage = this.relativeDamageValuePlace.getValue();
        // Prepare for after
        PlayerInfo player;
        List<List<PositionInfo>> possibleCrystals;
        PlayerInfo target;
        // Our result
        CrystalInfo.PlaceInfo bestPlace = new CrystalInfo.PlaceInfo(-100, null, null, 100d);
        // List of webs to replace
        ArrayList<BlockPos> webRemoved = new ArrayList<>();
        switch (mode) {
            // Lowest and Nearest use the same code with just 1 difference.
            case "Lowest":
            case "Nearest":
                // Get the target
                //noinspection ComparatorMethodParameterNotUsed
                EntityPlayer targetEP =
                        mode.equals("Lowest")
                        // Lowest
                        ? getBasicPlayers(enemyRangeSQ).min((x, y) -> (int) x.getHealth()).orElse(null)
                        // Nearest
                        : getBasicPlayers(enemyRangeSQ).min(Comparator.comparingDouble(x -> x.getDistanceSq(mc.player))).orElse(null);

                // If nobody found, return
                if (targetEP == null)
                    break;

                // If web, replace
                if (BlockUtil.getBlock(targetEP.posX, targetEP.posY, targetEP.posZ) instanceof BlockWeb) {
                    mc.world.setBlockToAir(new BlockPos(targetEP.posX, targetEP.posY, targetEP.posZ));
                    webRemoved.add(new BlockPos(targetEP.posX, targetEP.posY, targetEP.posZ));
                }

                // Get us information + predict if needed
                player = new PlayerInfo( predictSelfPlace.getValue() ? PredictUtil.predictPlayer(mc.player, settings) : mc.player, false,
                        mc.player.getTotalArmorValue(),
                        (float) mc.player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

                // Show self predict
                if (predictSelfPlace.getValue() && showSelfPredictPlace.getValue())
                    toDisplay.add(new display(player.entity.getEntityBoundingBox(), colorSelfPlace.getColor(), widthPredict.getValue()));

                // Get every possible crystals
                possibleCrystals = getPossibleCrystalsPlacing(player, maxSelfDamage, raytraceValue, wallRangePlaceSQ, ignoreTerrainValue);

                // If nothing is possible
                if (possibleCrystals == null)
                    break;

                // Get target info
                target = new PlayerInfo(targetEP, armourPercent,
                        targetEP.getTotalArmorValue(),
                        (float) targetEP.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

                // Calcualte best cr
                bestPlace = calcualteBestPlace(nThread, possibleCrystals, player.entity.posX, player.entity.posY, player.entity.posZ, target,
                        minDamage, minFacePlaceHp, minFacePlaceDamage, maxSelfDamage, maxYTarget, minYTarget, placeTimeout, new CrystalInfo.PlaceInfo(0, null, null, 0), ignoreTerrainValue, relativeDamage, valueRelativeDamage);

                break;
            case "Damage":
                // Get every possible players
                List<EntityPlayer> players = getBasicPlayers(enemyRangeSQ).sorted(new Sortbyroll()).collect(Collectors.toList());
                if (players.size() == 0)
                    break;

                // Replace web if needed
                for(EntityPlayer et : players) {
                    if (BlockUtil.getBlock(et.posX, et.posY, et.posZ) instanceof BlockWeb) {
                        mc.world.setBlockToAir(new BlockPos(et.posX, et.posY, et.posZ));
                        webRemoved.add(new BlockPos(et.posX, et.posY, et.posZ));
                    }
                }

                // If predict
                if (predictPlaceEnemy.getValue()) {
                    players = getPlayersThreaded(nThread, players, settings, predictPlaceTimeout.getValue());
                }

                // Get our information
                player = new PlayerInfo( predictSelfPlace.getValue() ?
                        PredictUtil.predictPlayer(mc.player, settings)
                        : mc.player, false,
                        mc.player.getTotalArmorValue(),
                        (float) mc.player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

                // If display predict
                if (predictSelfPlace.getValue() && showSelfPredictPlace.getValue())
                    toDisplay.add(new display(player.entity.getEntityBoundingBox(), colorSelfPlace.getColor(), widthPredict.getValue()));

                // If we are placing
                possibleCrystals = getPossibleCrystalsPlacing(player, maxSelfDamage, raytraceValue, wallRangePlaceSQ, ignoreTerrainValue);

                // If nothing is possible
                if (possibleCrystals == null)
                    break;

                // For every players
                int count = 0;

                // Iterate for every players
                for (EntityPlayer playerTemp : players) {
                    // If we reached max
                    if (count++ >= maxTarget.getValue())
                        break;

                    // Get target
                    target = new PlayerInfo(playerTemp, armourPercent,
                            playerTemp.getTotalArmorValue(),
                            (float) playerTemp.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
                    // Calculate
                    bestPlace = calcualteBestPlace(nThread, possibleCrystals, player.entity.posX, player.entity.posY, player.entity.posZ, target,
                            minDamage, minFacePlaceHp, minFacePlaceDamage, maxSelfDamage, maxYTarget, minYTarget, placeTimeout, bestPlace, ignoreTerrainValue, relativeDamage, valueRelativeDamage);
                }
        }

        // Just repalce every webs we removed
        for(BlockPos web : webRemoved)
            mc.world.setBlockState(web, Blocks.WEB.getDefaultState());

        if (bestPlace.target != null) {
            placeRender = 0;

            boolean found = false;
            for(renderClass rend : toRender)
                if (rend.reset(bestPlace.target.entity.entityId)) {
                    found = true;
                    break;
                }

            if (!found) {
                toRender.add(new renderClass(bestPlace.target.entity.entityId, life.getValue(), color.getValue(), circleRange.getValue(), desyncCircle.getValue(), stepRainbowCircle.getValue(), circleRange.getValue(), stepRainbowCircle.getValue(), increaseHeight.getValue(), speedIncrease.getValue()));
            }
        }

        // Oh well, lmao everything here is likely well commented

        return bestPlace;
    }

    // Function that call every thread for the calculating of the crystals
    // + return the best place
    CrystalInfo.PlaceInfo calcualteBestPlace(int nThread, List<List<PositionInfo>> possibleCrystals, double posX, double posY, double posZ,
                                             PlayerInfo target, double minDamage, double minFacePlaceHp, double minFacePlaceDamage, double maxSelfDamage,
                                             int maxYTarget, int minYTarget, int placeTimeout, CrystalInfo.PlaceInfo old, boolean ignoreTerrain, boolean relativeDamage, double valueRelativeDamage) {
        // For getting output of threading
        Collection<Future<?>> futures = new LinkedList<>();
        // Iterate for every thread we have
        for (int i = 0; i < nThread; i++) {
            int finalI = i;
            // Add them
            futures.add(executor.submit(() -> calculateBestPlaceTarget(possibleCrystals.get(finalI), posX, posY, posZ,
                    target, minDamage, minFacePlaceHp, minFacePlaceDamage, maxSelfDamage, maxYTarget, minYTarget, ignoreTerrain, relativeDamage, valueRelativeDamage)));
        }
        // Get stack for then collecting the results
        Stack<CrystalInfo.PlaceInfo> results = new Stack<>();
        // For every thread
        for (Future<?> future : futures) {
            try {
                // Get it
                CrystalInfo.PlaceInfo temp;
                temp = (CrystalInfo.PlaceInfo) future.get(placeTimeout, TimeUnit.MILLISECONDS);
                // If not null, add
                if (temp != null)
                    results.add(temp);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
        }

        // Get best result
        results.add(old);
        return getResultPlace(results);
    }

    // This return the best crystal
    CrystalInfo.PlaceInfo getResultPlace(Stack<CrystalInfo.PlaceInfo> result) {
        // Init returnValue
        CrystalInfo.PlaceInfo returnValue = new CrystalInfo.PlaceInfo(0, null, null, 100);
        // Check the best of everything
        while (!result.isEmpty()) {
            // Get value
            CrystalInfo.PlaceInfo now = result.pop();
            // If damage is the same
            if (now.damage == returnValue.damage) {
                // Check for distance
                if (now.distance < returnValue.distance) {
                    returnValue = now;
                }
                // If damage is higher
            } else if (now.damage > returnValue.damage)
                // Return
                returnValue = now;
        }

        return returnValue;
    }

    // This return a list of possible positions of the crystals
    List<List<PositionInfo>> getPossibleCrystalsPlacing(PlayerInfo self, double maxSelfDamage, boolean raytrace, double wallRangeSQ, boolean ignoreTerrain) {
        // Output
        List<PositionInfo> damagePos = new ArrayList<>();
        // Get crystals
        (includeCrystalMapping.getValue() ?
                CrystalUtil.findCrystalBlocksExcludingCrystals(placeRange.getValue().floatValue(), newPlace.getValue())
                :
                CrystalUtil.findCrystalBlocks(placeRange.getValue().floatValue(), newPlace.getValue()))
        // For every crystals, forEach
        .forEach(
                crystal -> {
                    // Get damage
                    float damage = DamageUtil.calculateDamageThreaded(crystal.getX() + .5D, crystal.getY() + 1D, crystal.getZ() + .5D, self, ignoreTerrain);
                    // If we can take that damage
                    if (damage < maxSelfDamage && (!antiSuicidepl.getValue() || damage < self.health)) {
                        // Raytrace. We have to calculate the raytrace for both wall and raytrace option
                        RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
                                new Vec3d(crystal.getX() + .5d, crystal.getY() + 1D, crystal.getZ() + .5d));
                        // If null, enter, if it's the same block, enter, if it's not raytrace and the distance is <, enter
                        if (result == null || sameBlockPos(result.getBlockPos(), crystal) || (!raytrace && mc.player.getDistanceSq(crystal) <= wallRangeSQ)) {
                            // Add to possible crystals
                            damagePos.add(new PositionInfo(crystal, damage));
                        }
                    }

                }
        );
        // Remove every crystals that deal more damage to us
        return splitList(damagePos, nThread.getValue());
    }

    // This split the list of positions in multiple list. Is used for multithreading
    List<List<PositionInfo>> splitList(List<PositionInfo> start, int nThreads) {
        // If we have only 1  thread, return only 1 thing (sad)
        if (nThreads == 1)
            return new ArrayList<List<PositionInfo>>() {
                {
                    add(start);
                }
            };
        // Get n^Possibilites
        int count;
        if ((count = start.size()) == 0)
            return null;
        // Output
        List<List<PositionInfo>> output = new ArrayList<>(nThreads);
        for (int i = 0; i < nThreads; i++) output.add(new ArrayList<>());

        // Add everything
        for (int i = 0; i < count; i++) {
            // i % nThreads allow us to iterate in an efficent way
            output.get(i % nThreads).add(start.get(i));
        }

        // Return
        return output;
    }

    // This calculate the best crystal given a list of possible positions and the enemy
    CrystalInfo.PlaceInfo calculateBestPlaceTarget(List<PositionInfo> possibleLocations, double x, double y, double z, PlayerInfo target,
                                                   double minDamage, double minFacePlaceHealth, double minFacePlaceDamage, double maxSelfDamage,
                                                   int maxYTarget, int minYTarget, boolean ignoreTerrain, boolean relativeDamage, double valueRelativeDamage) {
        // Start calculating damage
        PositionInfo best = new PositionInfo();
        for (PositionInfo crystal : possibleLocations) {
            // Calculate Y
            double temp;
            if ((temp = target.entity.posY - crystal.pos.getY() - 1) > 0 ? temp > minYTarget : temp < -maxYTarget)
                continue;

            // if player is out of range of this crystal, do nothing
            float currentDamage = DamageUtil.calculateDamageThreaded((double) crystal.pos.getX() + 0.5d, (double) crystal.pos.getY() + 1.0d, (double) crystal.pos.getZ() + 0.5d, target, ignoreTerrain);
            if (currentDamage == best.damage) {
                // this new crystal is closer
                // higher chance of being able to break it
                if (best.pos == null || ((temp = crystal.pos.distanceSq(x, y, z)) == best.distance || (currentDamage / maxSelfDamage) > best.rapp) || temp < best.distance) {
                    // Set new values
                    best = crystal;
                    best.setEnemyDamage(currentDamage);
                    best.distance = target.entity.getDistanceSq((double) crystal.pos.getX() + 0.5d, (double) crystal.pos.getY() + 1.0d, (double) crystal.pos.getZ() + 0.5d);
                    best.distancePlayer = mc.player.getDistanceSq((double) crystal.pos.getX() + 0.5d, (double) crystal.pos.getY() + 1.0d, (double) crystal.pos.getZ() + 0.5d);
                }
            } else if (currentDamage > best.damage) {
                if (relativeDamage) {
                    if (crystal.getSelfDamage() / currentDamage > valueRelativeDamage) {
                        continue;
                    }
                }
                // Set new values
                best = crystal;
                best.setEnemyDamage(currentDamage);
                best.distance = target.entity.getDistanceSq((double) crystal.pos.getX() + 0.5d, (double) crystal.pos.getY() + 1.0d, (double) crystal.pos.getZ() + 0.5d);
                best.distancePlayer = mc.player.getDistanceSq((double) crystal.pos.getX() + 0.5d, (double) crystal.pos.getY() + 1.0d, (double) crystal.pos.getZ() + 0.5d);
            }
        }


        // If we found something
        if (best.pos != null) {
            if (best.damage >= minDamage || ((target.health <= minFacePlaceHealth || target.lowArmour) && best.damage >= minFacePlaceDamage)) {
                // Return
                return new CrystalInfo.PlaceInfo((float) best.damage, target, best.pos, best.distancePlayer);
            }
        }
        return null;
    }

    //endregion

    //region Place Crystal
    boolean canStartPlacing() {

        switch (placeDelay.getValue()) {
            // Tick, check if tick == 0, else -1
            case "Tick":
                if (tickBeforePlace == 0)
                    return true;
                else tickBeforePlace--;
                break;
            case "Time":
                // Check if the time between time and now is >= timeDelayPlace
                if (!checkTimePlace)
                    return true;
                else if (System.currentTimeMillis() - timePlace >= timeDelayPlace.getValue()) {
                    checkTimePlace = false;
                    return true;
                }
                break;
            case "Vanilla":
                if (timerPlace.getTimePassed() / 50L >= 20 - vanillaSpeedPlace.getValue()) {
                    timerPlace.reset();
                    return true;
                }
                break;
        }
        // If we are not ready
        return false;
    }

    // Main function for placing crystals
    boolean placeCrystals() {

        // If we have placed a crystal before
        if (placedCrystal) {
            // Stop
            placedCrystal = false;
            return false;
        }

        // If we cannot place (place delay)
        if (!canStartPlacing())
            return false;

        /*
            Why are you reading comments? Weirdo!
         */

        // Get crystal hand
        EnumHand hand = getHandCrystal();
        // If no hand found
        if (hand == null)
            return false;

        // If we have to look a block
        if (crystalPlace != null) {

            // First, lets calculate the raytrace in case
            if (raytrace.getValue()) {
                RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ),
                        new Vec3d(crystalPlace.posCrystal.getX() + 0.5d, crystalPlace.posCrystal.getY() + .5d, crystalPlace.posCrystal.getZ() + 0.5d));

                // If blocked
                if (result == null || result.sideHit == null) {
                    crystalPlace = null;
                }
            }

            // If we have a crystal and we have to reculculate the damage
            if ( crystalPlace != null && recalculateDamage.getValue()) {
                // If nothing found, null
                if (isCrystalGood(crystalPlace.posCrystal) == null)
                    crystalPlace = null;
            }

            // If it's not null
            if (crystalPlace != null && crystalPlace.posCrystal != null) {
                // Check if the crystal is ready, if yes, null
                if (crystalPlace.isReady()) {
                    crystalPlace = null;
                }
                else {
                    // AutoWeb
                    if (isPlacingWeb())
                        return true;

                    // Else, place it (dont ask me why but sometimes crystalPlace become null
                    if (crystalPlace != null)
                        return placeCrystal(crystalPlace.posCrystal, hand, false);

                }
            }
        }

        // For debugging timeCalcPlacement
        long inizio = 0;
        if (timeCalcPlacement.getValue())
            // Get time
            inizio = System.currentTimeMillis();
        boolean instaPlaceBol = false;
        // Get target
        if (forcePlaceCrystal != null && forcePlace.getValue()) {
            bestPlace = new CrystalInfo.PlaceInfo(forcePlaceDamage, forcePlaceTarget, forcePlaceCrystal, -10);
            placeRender = 0;
            instaPlaceBol = true;
        }
        else {
            bestPlace = getTargetPlacing(targetPlacing.getValue());
            if (forcePlaceCrystal != null && bestPlace.crystal != null)
                if (sameBlockPos(forcePlaceCrystal, bestPlace.crystal))
                    // This is basically useless lmao
                    instaPlaceBol = true;
            placeRender = 0;
        }


        // For debugging timeCalcPlacemetn
        if (timeCalcPlacement.getValue()) {
            // Get duration
            long fine = System.currentTimeMillis();
            durationsPlace.add(fine - inizio);
            // If we reached last
            if (durationsPlace.size() > nCalc.getValue()) {
                double sum = durationsPlace.stream()
                        .mapToDouble(a -> a)
                        .sum();
                sum /= nCalc.getValue();
                durationsPlace.clear();
                PistonCrystal.printDebug(String.format("N: %d Value: %f", nCalc.getValue(), sum), false);
            }
        }

        if (instaPlace.getValue() && bestPlace.target == null && forcePlaceCrystal != null) {
            // Idk instaplace useless thing
            bestPlace = new CrystalInfo.PlaceInfo(forcePlaceDamage, forcePlaceTarget, forcePlaceCrystal, -10);
            instaPlaceBol = true;
            placeRender = 0;
        }

        forcePlaceCrystal = null;

        // Display crystal
        if (bestPlace.crystal != null) {
            if (showTextpl.getValue())
                toDisplay.add(new display(String.valueOf((int) bestPlace.damage), bestPlace.crystal, colorPlaceText.getValue(), textYPlace.getValue()));
            if (predictPlaceEnemy.getValue())
                toDisplay.add(new display(bestPlace.getTarget().getEntityBoundingBox(), showColorPredictEnemyPlace.getColor(), outlineWidthpl.getValue()));

            // Oh well, webs are useless but okay
            if (isPlacingWeb())
                return true;

            // Place crystal
            return placeCrystal(bestPlace.crystal, hand, instaPlaceBol);
        } else {
            // This is normal switchBack lol
            if (switchBack.getValue() && oldSlotObby != -1)
                // Simple logic, i dont think i have to explain this lmao
                if (tickSwitch > 0)
                    --tickSwitch;
                else
                    if (tickSwitch == 0) {
                        mc.player.inventory.currentItem = oldSlotObby;
                        tickSwitch = -1;
                    }
        }
        return false;
    }

    boolean isPlacingWeb() {
        // AutoWeb
        if (autoWeb.getValue() && bestPlace != null && bestPlace.target != null && (!onlyAutoWebActive.getValue() || ModuleManager.isModuleEnabled(AutoWeb.class))) {
            // If the enemy is in air
            if (BlockUtil.getBlock(bestPlace.getTarget().posX, bestPlace.getTarget().posY, bestPlace.getTarget().posZ) instanceof BlockAir) {
                // Place it
                //noinspection RedundantIfStatement
                if (placeWeb(new BlockPos(bestPlace.getTarget().posX, bestPlace.getTarget().posY, bestPlace.getTarget().posZ)) && stopCrystal.getValue())
                    return true;
            }
        // Webs are useless
        } else if (oldSlotBackWeb != -1) {
            mc.player.inventory.currentItem = oldSlotBackWeb;
            oldSlotBackWeb = -1;
        }

        return false;
    }

    // Simple function for placing a web on a target. Why? They are fucking useless
    boolean placeWeb(BlockPos target) {

        // If it's possible to place
        EnumFacing side = BlockUtil.getPlaceableSide(target);
        if (side == null)
            return false;

        BlockPos neighbour = target.offset(side);
        EnumFacing opposite = side.getOpposite();

        // If that block cannot be clicked, false
        if (!BlockUtil.canBeClicked(neighbour)) {
            return false;
        }

        // Here we basically manage the web slot.
        int oldSlot = -1;
        // If we dont have a web in our hand
        if (!(mc.player.inventory.getCurrentItem().getItem() instanceof ItemBlock &&
                ((ItemBlock) mc.player.inventory.getCurrentItem().getItem()).getBlock() == Blocks.WEB)) {
            // Get slot of the web
            int slot = InventoryUtil.findFirstBlockSlot(Blocks.WEB.getClass(), 0, 8);
            oldSlot = mc.player.inventory.currentItem;
            // If nothing found, return false
            if (slot == -1)
                return false;
            // If something found and silentSwitch, change slot
            else if (silentSwitchWeb.getValue()) {mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));mc.playerController.updateController();}
            // If we have to switch back at the end, normal switch + set oldSlotBack to oldSlot
            else if (switchBackEnd.getValue()) {
                oldSlotBackWeb = oldSlot;
                mc.player.inventory.currentItem = slot;
                oldSlot = -1;
            }
            // If normal switch but with switch back at the end
            else if (switchBackWeb.getValue()) mc.player.inventory.currentItem = slot;
            // If just switch
            else if (switchWeb.getValue()) {
                mc.player.inventory.currentItem = slot;
                oldSlot = -1;
            } else return false;
        }

        // Get hitVec
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();

        // If preRotate
        if (preRotate.getValue()) {
            BlockUtil.faceVectorPacketInstant(hitVec, true);
        }

        // If focusWeb
        if (focusWebRotate.getValue()) {
            lastHitVec = hitVec;
            tick = 0;
        }

        // If that block can be opened
        boolean isSneaking = false;
        if (BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock)) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            isSneaking = true;
        }

        // Place the block
        mc.playerController.processRightClickBlock(mc.player, mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);

        // If sneaking, stop
        if (isSneaking)
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));

        // If oldSlot is != -1, then we have to switchBack
        if (oldSlot != -1)
            // SilentSwitch
            if (silentSwitchWeb.getValue()) mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
            // Normal switchBack
            else mc.player.inventory.currentItem = oldSlot;
        mc.playerController.updateController();
        return true;
    }

    EntityPlayer isCrystalGood(BlockPos crystal) {

        // Check for the damafge
        float damage;
        if ( (damage = DamageUtil.calculateDamage(crystal.getX() + .5D, crystal.getY() + 1D, crystal.getZ() + .5D, mc.player, ignoreTerrain.getValue()))
                >= maxSelfDamagePlace.getValue() && (!antiSuicidepl.getValue() || damage < PlayerUtil.getHealth()))
            return null;

        // Get rangeSQ
        double rangeSQ = rangeEnemyPlace.getValue() * rangeEnemyPlace.getValue();

        // Stream every players
        Optional<EntityPlayer> a = mc.world.playerEntities.stream()
                // Basic checks
                .filter(entity -> !EntityUtil.basicChecksEntity(entity))
                // Not dead
                .filter(entity -> entity.getHealth() > 0.0f)
                // Distance is ok
                .filter(entity -> mc.player.getDistanceSq(entity) <= rangeSQ)
                // Damage is ok
                .filter(entity -> DamageUtil.calculateDamage(crystal.getX() + .5D, crystal.getY() + 1D, crystal.getZ() + .5D, entity, ignoreTerrain.getValue()) >= minDamagePlace.getValue())
                // Find any. We cares if at least 1 player is affected
                .findAny();

        // Return a, if not a, null
        return a.orElse(null);

    }

    // Get hand of breaking. Return null if no crystals
    EnumHand getHandCrystal() {
        // Reset slotChange (We do this because we are going to switch only when placing, not before)
        slotChange = -1;
        // Check offhand
        if (mc.player.getHeldItemOffhand().getItem() instanceof ItemEndCrystal)
            return EnumHand.OFF_HAND;
        else {
            // Check mainhand
            if (mc.player.getHeldItemMainhand().getItem() instanceof ItemEndCrystal) {
                return EnumHand.MAIN_HAND;
            }
            else if (switchHotbar.getValue()) {
                // Get slot
                int slot = InventoryUtil.findFirstItemSlot(ItemEndCrystal.class, 0, 8);
                // If found
                if (slot != -1) {
                    if (waitGappleSwitch.getValue() && mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem).getItem() == Items.GOLDEN_APPLE && oldSlot == slot)
                        return null;
                    slotChange = slot;
                    return EnumHand.MAIN_HAND;
                }
            }
        }
        return null;
    }

    // This actually place the crystal
    boolean placeCrystal(BlockPos pos, EnumHand handSwing, boolean instaPlace) {
        // If there is a crystal, stop
        if ( !placeOnCrystal.getValue() && !isCrystalHere(pos) && !instaPlace)
            return false;

        // Get position
        BlockPos posUp = pos.up();

        // Get box
        AxisAlignedBB box = new AxisAlignedBB(
                posUp.getX(), posUp.getY(), posUp.getZ(),
                posUp.getX() + 1.0, posUp.getY() + 2.0, posUp.getZ() + 1.0
        );

        // Check for entity
        List<Entity> a = mc.world.getEntitiesWithinAABB(Entity.class, box, entity -> entity instanceof EntityEnderCrystal && !sameBlockPos(entity.getPosition().add(0, -1, 0), pos));

        // If there is a crystal near us
        if (a.size() > 0) {
            // If we can break it
            if (breakNearCrystal.getValue()) {
                // Break the first we see and force the break
                forceBreak = (EntityEnderCrystal) a.get(0);
                forceBreakPlace = pos;
            }
            return false;
        }

        // If this pos is in wait
        if (listCrystalsPlaced.CrystalExists(pos) != -1)
            return false;

        // Rotate
        if (rotate.getValue()) {
            // New lastHitVec
            lastHitVec = new Vec3d(pos).add(0.5, 1, 0.5);
            // New tick
            tick = 0;
            // If preRotate
            // If we have to check or yaw or pitch (-28  14)
            if (yawCheck.getValue() || pitchCheck.getValue()) {
                // Get the rotation we want
                Vec2f rotationWanted = RotationUtil.getRotationTo(lastHitVec);
                // If we are not rotating, set new values
                if ( !blockRotation.getValue() || !isRotating) {
                    // Shits i dont like, yawStep and pitchStep bruh
                    /*
                        So, if we have to check pitch/yaw, first we check if the variable
                        is Double.MAX_VALUE, if yes our start is where we are looking
                        if not then we are looking another thing, so we have to reasume from it
                     */
                    yPlayerRotation = pitchCheck.getValue()
                            ? (
                            yPlayerRotation == Double.MAX_VALUE ?
                                    mc.player.getPitchYaw().x
                                    : yPlayerRotation
                    )
                            : Double.MIN_VALUE;
                    xPlayerRotation = yawCheck.getValue()
                            ? (
                            xPlayerRotation == Double.MAX_VALUE ?
                                    RotationUtil.normalizeAngle(mc.player.getPitchYaw().y)
                                    : xPlayerRotation
                    )
                            : Double.MIN_VALUE;
                    isRotating = true;
                }



                // If we allow to predict the place (so place when we are near that block)
                if (rotate.getValue() && placeStrictDirection.getValue()) {

                    // Check yaw
                    if (yawCheck.getValue()) {
                        // Get first if + or -
                        double distanceDo = rotationWanted.x - xPlayerRotation;
                        if (Math.abs(distanceDo) > 180) {
                            distanceDo = RotationUtil.normalizeAngle(distanceDo);
                        }
                        // Check if distance is > of what we want
                        if (Math.abs(distanceDo) > yawStep.getValue()) {
                            return true;
                        }
                    }

                    // Check pitch
                    if (pitchCheck.getValue()) {
                        // Get first if + or -
                        double distanceDo = rotationWanted.y - yPlayerRotation;
                        // Check if distance is > of what we want
                        if (Math.abs(distanceDo) > pitchStep.getValue()) {
                            return true;
                        }
                    }

                } else if (!(xPlayerRotation == rotationWanted.x && yPlayerRotation == rotationWanted.y))
                    return true;
            }
        }

        // If the slot is different, we have to silent switch first because, else, we'll place or obby
        // Or we wont place (We are working with packet, mc.player.inventory is too slow)
        if (handSwing == EnumHand.MAIN_HAND) {

            // If we have to change
            if (slotChange != -1) {
                // Silent switch
                if (silentSwitch.getValue()) {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(slotChange));
                } else {
                    // Normal switch
                    if (slotChange != mc.player.inventory.currentItem) {
                        if (switchBack.getValue()) {
                            tickSwitch = tickSwitchBack.getValue();
                            oldSlotObby = mc.player.inventory.currentItem;
                        }
                        mc.player.inventory.currentItem = slotChange;
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                        mc.playerController.updateController();
                    }

                }
            } else {
                // Change to crystal
                if (oldSlot != mc.player.inventory.currentItem) {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                    mc.playerController.updateController();
                }
            }
        }

        // Raytrace
        if (raytrace.getValue()) {
            // get enumFacing
            EnumFacing enumFacing;
            RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.getX() + 0.5d, pos.getY() + .5d, pos.getZ() + 0.5d));
            // If not, return
            if (result == null || result.sideHit == null) {
                return false;
            } else {
                // Else, enumFacing is the side we hit
                enumFacing = result.sideHit;
            }

            if ( rotate.getValue() && preRotate.getValue()) {
                Vec2f rot = RotationUtil.getRotationTo(lastHitVec);
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rot.x, rot.y, mc.player.onGround));
            }

            // Place
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, enumFacing, handSwing, 0, 0, 0));
        } else if (placeStrictDirection.getValue()) {

            /// Here we have to understand the facing we are going to place, idk on 2bstrict it requires this
            // If we are above, 100% up
            EnumFacing result;
            if (mc.player.posY + .63 > pos.getY()) {
                result = EnumFacing.UP;
            }
            // Else if we are down
            else {
                double  xDiff = pos.getX() - mc.player.posX + .5,
                        zDiff = pos.getZ() - mc.player.posZ + .5;
                result = Math.abs(xDiff) > Math.abs(zDiff)
                            ? (xDiff > 0 ? EnumFacing.WEST : EnumFacing.EAST)
                            : (zDiff > 0 ? EnumFacing.NORTH : EnumFacing.SOUTH);
            }

            if ( rotate.getValue() && preRotate.getValue()) {
                Vec2f rot = RotationUtil.getRotationTo(lastHitVec);
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rot.x, rot.y, mc.player.onGround));
            }

            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, result, handSwing, 0, 0, 0));

        } else {
            if ( rotate.getValue() && preRotate.getValue()) {
                Vec2f rot = RotationUtil.getRotationTo(lastHitVec);
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rot.x, rot.y, mc.player.onGround));
            }
            // Normal placing
            if (pos.getY() == 255) {
                // For Hoosiers. This is how we do build height. If the target block (q) is at Y 255. Then we send a placement packet to the bottom part of the block. Thus the EnumFacing.DOWN.
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.DOWN, handSwing, 0, 0, 0));
            } else {
                // Normal placing
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.UP, handSwing, 0, 0, 0));
            }

        }

        // If he want to swing
        if (!swingModepl.getValue().equals("None"))
            swingArm(swingModepl.getValue(), hideClientpl.getValue(), handSwing);

        // For silent switch
        if (slotChange != -1) {
            if (silentSwitch.getValue()) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                mc.playerController.updateController();
            }
        }

        // For limiting place packets
        tickBeforePlace = tickDelayPlace.getValue();
        checkTimePlace = true;
        timePlace = System.currentTimeMillis();
        // Switch
        switch (limitPacketPlace.getValue()) {
            // Tick, add new wait in tick
            case "Tick":
                listCrystalsPlaced.addCrystal(pos, 0, limitTickPlace.getValue());
                break;
            // Time, add new wat as time
            case "Time":
                listCrystalsPlaced.addCrystal(pos, limitTickTime.getValue());
                break;
        }

        // Add that we placed that crystal
        endCrystalPlaced.addCrystal(pos);

        // If we have to show how many crystals we have placed, add
        if (showPlaceCrystalsSecond.getValue())
            listCrystalsSecondWait.addCrystal(pos, 2000);

        // For continuing facing the crystal
        if (crystalPlace == null)
            // Focus switch
            switch(focusPlaceType.getValue()) {
                // If tick
                case "Tick":
                    crystalPlace = new crystalTime(pos, 0, tickWaitFocusPlace.getValue());
                    break;
                // If time
                case "Time":
                    crystalPlace = new crystalTime(pos, timeWaitFocusPlace.getValue());
                    break;
            }
        return true;
    }

    // Given a pos, say if there is a crystal
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isCrystalHere(BlockPos pos) {
        // Get position
        BlockPos posUp = pos.up();

        // Get box
        AxisAlignedBB box = new AxisAlignedBB(
                posUp.getX(), posUp.getY(), posUp.getZ(),
                posUp.getX() + 1.0, posUp.getY() + 2.0, posUp.getZ() + 1.0
        );

        // Check for entity
        return mc.world.getEntitiesWithinAABB(Entity.class, box, entity -> entity instanceof EntityEnderCrystal && sameBlockPos(entity.getPosition(), pos)).isEmpty();
    }

    //endregion

    //region Calculate Break Crystal

    CrystalInfo.NewBreakInfo getTargetBreaking(String mode) {
        // My cat is washing himself lol, now that i remember i have to talk to one of my friend lmao, i forgot to text her
        // Ok done
        PredictUtil.PredictSettings settings = new PredictUtil.PredictSettings(tickPredict.getValue(), calculateYPredict.getValue(), startDecrease.getValue(), exponentStartDecrease.getValue(), decreaseY.getValue(), exponentDecreaseY.getValue(), increaseY.getValue(), exponentIncreaseY.getValue(), splitXZ.getValue(), widthPredict.getValue(), debugPredict.getValue(), showPredictions.getValue(), manualOutHole.getValue(), aboveHoleManual.getValue(), stairPredict.getValue(), nStair.getValue(), speedActivationStair.getValue());
        int nThread = this.nThread.getValue();
        double enemyRangeSQ = rangeEnemyBreaking.getValue() * rangeEnemyBreaking.getValue();
        double maxSelfDamage = maxSelfDamageBreak.getValue();
        boolean rayTrace = raytrace.getValue();
        double wallRangeSQ = wallrangeBreak.getValue() * wallrangeBreak.getValue();
        float armourPercent = armourFacePlace.getValue() / 100.0f;
        int maxYTarget = this.maxYTarget.getValue();
        int minYTarget = this.minYTarget.getValue();
        double minFacePlaceHp =  this.facePlaceValue.getValue();
        if (forceFacePlace.getText().length() > 0) {
            if (Keyboard.isKeyDown(KeyBoardClass.getKeyFromChar(forceFacePlace.getText().charAt(0))))
                minFacePlaceHp = 36;
        }
        double minFacePlaceDamage = this.minFacePlaceDmg.getValue();
        double minDamage = this.minDamageBreak.getValue();
        double rangeSQ = this.breakRange.getValue() * this.breakRange.getValue();
        int breakTimeout = this.breakTimeout.getValue();
        boolean relativeDamage = this.relativeDamageBreak.getValue();
        double valueRelativeDamage = this.relativeDamageValueBreak.getValue();
        boolean ignoreTerrainValue = false;
        boolean antiSuicide = this.antiSuicidebr.getValue();
        if (ignoreTerrain.getValue())
            if (bindIgnoreTerrain.getValue()) {
                if (letterIgnoreTerrain.getText().length() > 0)
                    if (Keyboard.isKeyDown(KeyBoardClass.getKeyFromChar(letterIgnoreTerrain.getText().charAt(0))))
                        ignoreTerrainValue = true;
            } else ignoreTerrainValue = true;

        // Prepare for after
        PlayerInfo player;
        List<List<PositionInfo>> possibleCrystals;
        PlayerInfo target;
        // Our result
        CrystalInfo.NewBreakInfo bestBreak = new CrystalInfo.NewBreakInfo(-100, null, null, 100d);
        ArrayList<BlockPos> webRemoved = new ArrayList<>();
        switch (mode) {

            case "Nearest":
            case "Lowest":
                // Get the target
                EntityPlayer targetEP =
                        mode.equals("Lowest")
                                // Lowest
                                ? getBasicPlayers(enemyRangeSQ).min((x, y) -> (int) x.getHealth()).orElse(null)
                                // Nearest
                                : getBasicPlayers(enemyRangeSQ).min(Comparator.comparingDouble(x -> x.getDistanceSq(mc.player))).orElse(null);

                // If nobody found, return
                if (targetEP == null)
                    break;

                // If the player is in a web
                if (BlockUtil.getBlock(targetEP.posX, targetEP.posY, targetEP.posZ) instanceof BlockWeb) {
                    // Set it to air
                    mc.world.setBlockToAir(new BlockPos(targetEP.posX, targetEP.posY, targetEP.posZ));
                    // Replace it after
                    webRemoved.add(new BlockPos(targetEP.posX, targetEP.posY, targetEP.posZ));
                }

                // Get our informations. If we have to predict, predict coordinates
                player = new PlayerInfo( predictSelfDBreaking.getValue() ? PredictUtil.predictPlayer(mc.player, settings) : mc.player, false,
                        mc.player.getTotalArmorValue(),
                        (float) mc.player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

                // If self predict and show it, add a box
                if (predictSelfDBreaking.getValue() && showSelfPredictBreaking.getValue())
                    toDisplay.add(new display(player.entity.getEntityBoundingBox(), colorSelfBreaking.getColor(), widthPredict.getValue()));

                // Get every possible crystals that you could break
                possibleCrystals = getPossibleCrystalsBreaking(player, maxSelfDamage, rayTrace, wallRangeSQ, rangeSQ, antiSuicide, ignoreTerrainValue);

                if (possibleCrystals == null)
                    break;

                // Get target info
                target = new PlayerInfo(targetEP, armourPercent,
                        targetEP.getTotalArmorValue(),
                        (float) targetEP.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());


                // Calcualte best cr
                bestBreak = calcualteBestBreak(nThread, possibleCrystals, player.entity.posX, player.entity.posY, player.entity.posZ, target,
                        minDamage, minFacePlaceHp, minFacePlaceDamage, maxSelfDamage, maxYTarget, minYTarget, breakTimeout, new CrystalInfo.NewBreakInfo(0, null, null, 0), ignoreTerrainValue, relativeDamage, valueRelativeDamage);


                break;

            case "Damage":
                // Get every possible players
                List<EntityPlayer> players = getBasicPlayers(enemyRangeSQ).sorted(new Sortbyroll()).collect(Collectors.toList());
                if (players.size() == 0)
                    break;

                // For every players, if there is web, remove it
                for(EntityPlayer et : players) {
                    if (BlockUtil.getBlock(et.posX, et.posY, et.posZ) instanceof BlockWeb) {
                        mc.world.setBlockToAir(new BlockPos(et.posX, et.posY, et.posZ));
                        webRemoved.add(new BlockPos(et.posX, et.posY, et.posZ));
                    }
                }

                // If predict
                if (predictPlaceEnemy.getValue()) {
                    players = getPlayersThreaded(nThread, players, settings, predictBreakTimeout.getValue());
                }

                // Get our information
                player = new PlayerInfo( predictSelfDBreaking.getValue() ?
                        PredictUtil.predictPlayer(mc.player, settings)
                        : mc.player, false,
                        mc.player.getTotalArmorValue(),
                        (float) mc.player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

                // If we have to show the predict
                if (predictSelfDBreaking.getValue() && showSelfPredictBreaking.getValue())
                    toDisplay.add(new display(player.entity.getEntityBoundingBox(), colorSelfBreaking.getColor(), widthPredict.getValue()));

                // Get possible crystals to break
                possibleCrystals = getPossibleCrystalsBreaking(player, maxSelfDamage, rayTrace, wallRangeSQ, rangeSQ, antiSuicide, ignoreTerrainValue);

                // If nothing found, rip
                if (possibleCrystals == null)
                    break;

                // For every players
                int count = 0;

                // Iterate for every players
                for (EntityPlayer playerTemp : players) {
                    // If we reached max
                    if (count++ >= maxTarget.getValue())
                        break;

                    // Get target
                    target = new PlayerInfo(playerTemp, armourPercent,
                            playerTemp.getTotalArmorValue(),
                            (float) playerTemp.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

                    // Calculate
                    bestBreak = calcualteBestBreak(nThread, possibleCrystals, player.entity.posX, player.entity.posY, player.entity.posZ, target,
                            minDamage, minFacePlaceHp, minFacePlaceDamage, maxSelfDamage, maxYTarget, minYTarget, breakTimeout, bestBreak, ignoreTerrainValue, relativeDamage, valueRelativeDamage);

                }

                break;

        }
        // Replace webs
        for(BlockPos web : webRemoved)
            mc.world.setBlockState(web, Blocks.WEB.getDefaultState());

        if (bestBreak.target != null)
            breakRender = 0;

        return bestBreak;

    }

    List<List<PositionInfo>> getPossibleCrystalsBreaking(PlayerInfo self, double maxSelfDamage, boolean raytrace, double wallRangeSQ, double rangeSQ, boolean antiSuicide, boolean ignoreTerrain) {
        // Our output
        List<PositionInfo> damagePos = new ArrayList<>();
        // 571 1 564
        // For every entity
        mc.world.loadedEntityList.stream()
                // Take only endCrystals
                .filter(entity -> entity instanceof EntityEnderCrystal
                        && !breakPacketLimit.crystalIdExists(entity.entityId)
                        && mc.player.getDistanceSq(entity) <= rangeSQ
                        && existsCrystal.CrystalExists(entity.getPosition().add(0, -1, 0)) == -1)
                // Transform list in list of endCrystals
                .map(entity -> (EntityEnderCrystal) entity).collect(Collectors.toList())
                // Iterate
                .forEach(
                        crystal -> {
                            // Damage
                            float damage = Float.MIN_VALUE;
                            // Since forEach does not allow continue, i have to use a boolean
                            boolean continueFor = true;

                            // If antiSuicide
                            if (antiSuicide ) {
                                // Get damage
                                damage = DamageUtil.calculateDamageThreaded(crystal.posX, crystal.posY, crystal.posZ, self, ignoreTerrain);
                                // If >, stop
                                if (damage >= self.health) {
                                    continueFor = false;
                                }
                            }

                            // If we can continue
                            if (continueFor) {
                                // Raytrace. We have to calculate the raytrace for both wall and raytrace option
                                RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
                                        new Vec3d(crystal.posX, crystal.posY + 1, crystal.posZ));
                                // If raytrace is ok
                                if (result == null || (!raytrace && mc.player.getDistanceSq(crystal) <= wallRangeSQ)) {

                                    // Calculate damage if before we havent calculated it
                                    if (damage == Float.MIN_VALUE)
                                        damage = DamageUtil.calculateDamageThreaded(crystal.posX, crystal.posY, crystal.posZ, self, ignoreTerrain);

                                    // For every types of break
                                    switch (chooseCrystal.getValue()) {
                                        case "All":
                                            // Add everything
                                            damagePos.add(new PositionInfo(crystal, damage));
                                            break;
                                        case "Own":
                                            // Add only our
                                            if (endCrystalPlaced.hasCrystal(crystal))
                                                damagePos.add(new PositionInfo(crystal, damage));
                                            break;
                                        case "Smart":
                                            // Add only crystals that deal less damage
                                            if (damage < maxSelfDamage)
                                                damagePos.add(new PositionInfo(crystal, damage));
                                            break;
                                    }
                                }
                            }

                        }
                );

        // Return splitted list
        return splitList(damagePos, nThread.getValue());
    }

    // Function that call every thread for the calculating of the crystals
    // + return the best place
    CrystalInfo.NewBreakInfo calcualteBestBreak(int nThread, List<List<PositionInfo>> possibleCrystals, double posX, double posY, double posZ,
                                                PlayerInfo target, double minDamage, double minFacePlaceHp, double minFacePlaceDamage, double maxSelfDamage,
                                                int maxYTarget, int minYTarget, int placeTimeout, CrystalInfo.NewBreakInfo oldBreak, boolean ignoreTerrain, boolean relativeDamage, double valueRelativeDamage) {
        // For getting output of threading
        Collection<Future<?>> futures = new LinkedList<>();
        // Iterate for every thread we have
        for (int i = 0; i < nThread; i++) {
            int finalI = i;

            // Add them
            futures.add(executor.submit(() -> calculateBestBreakTarget(possibleCrystals.get(finalI), posX, posY, posZ,
                    target, minDamage, minFacePlaceHp, minFacePlaceDamage, maxSelfDamage, maxYTarget, minYTarget, ignoreTerrain, relativeDamage, valueRelativeDamage)));
        }
        // Get stack for then collecting the results
        Stack<CrystalInfo.NewBreakInfo> results = new Stack<>();
        // For every thread
        for (Future<?> future : futures) {
            try {
                // Get it
                CrystalInfo.NewBreakInfo temp;
                temp = (CrystalInfo.NewBreakInfo) future.get(placeTimeout, TimeUnit.MILLISECONDS);
                // If not null, add
                if (temp != null)
                    results.add(temp);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
        }
        // Get best result
        results.add(oldBreak);
        // Return the result
        return getResultBreak(results);
    }


    // This calculate the best crystal given a list of possible positions and the enemy
    CrystalInfo.NewBreakInfo calculateBestBreakTarget(List<PositionInfo> possibleLocations, double x, double y, double z, PlayerInfo target,
                                                   double minDamage, double minFacePlaceHealth, double minFacePlaceDamage, double maxSelfDamage,
                                                   int maxYTarget, int minYTarget, boolean ignoreTerrain, boolean relativeDamage, double valueRelativeDamage) {
        // Start calculating damage
        PositionInfo best = new PositionInfo();
        for (PositionInfo crystal : possibleLocations) {
            // Calculate Y
            double temp;
            if ((temp = target.entity.posY - crystal.crystal.posY - 1) > 0 ? temp > minYTarget : temp < -maxYTarget)
                continue;

            // if player is out of range of this crystal, do nothing
            float currentDamage = DamageUtil.calculateDamageThreaded(crystal.crystal.posX, crystal.crystal.posY, crystal.crystal.posZ, target, ignoreTerrain);
            if (currentDamage == best.damage) {
                // this new crystal is closer
                // higher chance of being able to break it
                if (best.crystal == null || ((temp = crystal.crystal.getDistanceSq(x, y, z)) == best.distance || (currentDamage / maxSelfDamage) > best.rapp) || temp < best.distance) {
                    // Set new values
                    best = crystal;
                    best.setEnemyDamage(currentDamage);
                    best.distance = target.entity.getDistanceSq(crystal.crystal.posX, crystal.crystal.posY, crystal.crystal.posZ);
                    best.distancePlayer = mc.player.getDistanceSq(crystal.crystal.posX, crystal.crystal.posY, crystal.crystal.posZ);
                }
            } else if (currentDamage > best.damage) {
                if (relativeDamage) {
                    if (crystal.getSelfDamage() / currentDamage > valueRelativeDamage) {
                        continue;
                    }
                }
                // Set new values
                best = crystal;
                best.setEnemyDamage(currentDamage);
                best.distance = target.entity.getDistanceSq(crystal.crystal.posX, crystal.crystal.posY, crystal.crystal.posZ);
                best.distancePlayer = mc.player.getDistanceSq(crystal.crystal.posX, crystal.crystal.posY, crystal.crystal.posZ);
            }
        }


        // If we found something
        if (best.crystal != null) {
            if (best.damage >= minDamage || ((target.health <= minFacePlaceHealth || target.lowArmour) && best.damage >= minFacePlaceDamage)) {
                // Return
                return new CrystalInfo.NewBreakInfo((float) best.damage, target, best.crystal, best.distancePlayer);
            }
        }
        // return null
        return null;
    }


    // This return the best crystal
    CrystalInfo.NewBreakInfo getResultBreak(Stack<CrystalInfo.NewBreakInfo> result) {
        // Init returnValue
        CrystalInfo.NewBreakInfo returnValue = new CrystalInfo.NewBreakInfo(0, null, null, 100);
        // Check the best of everything
        while (!result.isEmpty()) {
            // Get value
            CrystalInfo.NewBreakInfo now = result.pop();
            // If damage is the same
            if (now.damage == returnValue.damage) {
                // Check for distance
                if (now.distance < returnValue.distance) {
                    returnValue = now;
                }
                // If damage is higher
            } else if (now.damage > returnValue.damage)
                // New values
                returnValue = now;
        }

        // Return output
        return returnValue;
    }

    //endregion

    // region Break Crystal

    boolean canStartBreaking() {
        switch (breakDelay.getValue()) {
            // Tick, check if tick == 0, else --
            case "Tick":
                if (tickBeforeBreak == 0)
                    return true;
                else tickBeforeBreak--;
                break;
            case "Time":
                // Check if the time between time and now is >= timeDelayPlace
                if (!checkTimeBreak)
                    return true;
                else if (System.currentTimeMillis() - timeBreak >= timeDelayBreak.getValue()) {
                    checkTimeBreak = false;
                    return true;
                }
                break;
            case "Vanilla":
                // Vanilla speed, idk how this works but it break a crystal N times per second (1 second = 20 ticks)
                if (timerBreak.getTimePassed() / 50L >= 20 - vanillaSpeedBreak.getValue()) {
                    timerBreak.reset();
                    return true;
                }
                break;
        }
        return false;
    }

    boolean breakCrystals() {

        // If we have placed a crystal before
        if (brokenCrystal) {
            // Stop
            brokenCrystal = false;
            return false;
        }

        if (!canStartBreaking())
            return false;

        if (antiCity.getValue()) {
            if (forceBreak == null) {
                forceBreak = possibleCrystal();
            }
        }


        /*
            Looking crystal break code
            Oh lmao how everything works without the code for looking the crystal
            Oh well.. Nevermind, for me these 3 lines do not exists since everything work fine
         */

        // For debugging timeCalcPlacement
        long inizio = 0;
        if (timeCalcBreaking.getValue())
            // Get time
            inizio = System.currentTimeMillis();
        // Get target
        if (forceBreak == null)
            bestBreak = getTargetBreaking(targetBreaking.getValue());
        // For debugging timeCalcPlacemetn
        if (timeCalcBreaking.getValue()) {
            // Get duration
            long fine = System.currentTimeMillis();
            durationsBreaking.add(fine - inizio);
            // If we reached last
            if (durationsPlace.size() > nCalc.getValue()) {
                double sum = durationsBreaking.stream()
                        .mapToDouble(a -> a)
                        .sum();
                sum /= nCalc.getValue();
                durationsBreaking.clear();
                PistonCrystal.printDebug(String.format("N: %d Value: %f", nCalc.getValue(), sum), false);
            }
        }

        // Break forceBreak
        if (forceBreak != null)
            return breakCrystal(forceBreak);
        // Display crystal
        else if (bestBreak.crystal != null) {
            if (showTextbr.getValue())
                toDisplay.add(new display(String.valueOf((int) bestBreak.damage), bestBreak.crystal.getPosition().add(0, -1, 0), colorBreakText.getValue(), textYBreak.getValue()));
            if (predictBreakingEnemy.getValue())
                toDisplay.add(new display(bestBreak.target.entity.getEntityBoundingBox(), showColorPredictEnemyBreaking.getColor(), outlineWidthpl.getValue()));
            // Break crystal
            if (listPlayersBreak.stream().noneMatch(e -> bestBreak.target.entity.getName().equals(e.name)) || isMoving(bestBreak.target.entity.getName()))
                return breakCrystal(bestBreak.crystal);
        }
        return false;
    }

    // If a player is moving given a name
    boolean isMoving(String name) {
        // Check for everyone
        for(EntityPlayer e : mc.world.playerEntities) {
            // If same name
            if (e.getName().equals(name)) {
                // Check speed
                if(Math.abs(e.posX - e.prevPosX) + Math.abs(e.posZ - e.prevPosZ) > speedActivation.getValue()) {
                    // If it is above, remove it from the list of players of slowBreak
                    listPlayersBreak.removeIf(f -> f.name.equals(name));
                    return true;
                } else return false;
            }
        }
        return false;
    }

    boolean breakCrystal(EntityEnderCrystal cr) {
        BlockPos pos = cr.getPosition();
        // Rotate
        if (rotate.getValue()) {
            // New lastHitVec
            lastHitVec = new Vec3d(pos).add(0.5, 0, 0.5);
            // New tick
            tick = 0;
            // If we have to check or yaw or pitch
            if (yawCheck.getValue() || pitchCheck.getValue()) {
                // Get the rotation we want
                Vec2f rotationWanted = RotationUtil.getRotationTo(lastHitVec);
                // If we are not rotating, set new values
                if ( !blockRotation.getValue() || !isRotating) {
                    // I wrote the explaination in place, go and read it lol
                    yPlayerRotation = pitchCheck.getValue()
                            ? (
                            yPlayerRotation == Double.MAX_VALUE ?
                                    mc.player.getPitchYaw().x
                                    : yPlayerRotation
                    )
                            : Double.MIN_VALUE;
                    xPlayerRotation = yawCheck.getValue()
                            ? (
                            xPlayerRotation == Double.MAX_VALUE ?
                                    RotationUtil.normalizeAngle(mc.player.getPitchYaw().y)
                                    : xPlayerRotation
                    )
                            : Double.MIN_VALUE;
                    isRotating = true;
                }


                // If we allow to predict the place (so place when we are near that block)
                if (placeStrictDirection.getValue()) {
                    boolean back = false;
                    // Check yaw
                    if (yawCheck.getValue()) {
                        // Get first if + or -
                        double distanceDo = rotationWanted.x - xPlayerRotation;
                        if (Math.abs(distanceDo) > 180) {
                            distanceDo = RotationUtil.normalizeAngle(distanceDo);
                        }
                        // Check if distance is > of what we want
                        if (Math.abs(distanceDo) > yawStep.getValue()) {
                            back = true;
                        }
                    }

                    // Check pitch
                    if (pitchCheck.getValue()) {
                        // Get first if + or -
                        double distanceDo = rotationWanted.y - yPlayerRotation;
                        // Check if distance is > of what we want

                        if (Math.abs(distanceDo) > pitchStep.getValue()) {
                            back = true;
                        }
                    }

                    if (back) {
                        if (predictBreakRotation.getValue()) {
                            if (lookingCrystal(cr))
                                return false;
                        } else return false;
                    }

                } else if (!(xPlayerRotation == rotationWanted.x && yPlayerRotation == rotationWanted.y))
                    return false;
            }
        }

        int switchBack = -1;
        // If weakness and we dont have strenght 2
        if (antiWeakness.getValue() && mc.player.isPotionActive(MobEffects.WEAKNESS)
            && mc.player.getActivePotionEffects().stream().noneMatch(e -> e.getEffectName().contains("damageBoost") && e.getAmplifier() > 0)) {
            // switch to sword
            int slotSword = InventoryUtil.findFirstItemSlot(ItemSword.class, 0, 8);
            if (slotSword == -1)
                return false;
            if (slotSword != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(slotSword));
                mc.player.inventory.currentItem = slotSword;
                // Apparently you have to wait 1 tick or else the sword would not count
                return false;
            }
        }

        if ( rotate.getValue() && preRotate.getValue()) {
            Vec2f rot = RotationUtil.getRotationTo(lastHitVec);
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rot.x, rot.y, mc.player.onGround));
        }

        // Swing
        if (!swingModebr.getValue().equals("None"))
            swingArm(swingModebr.getValue(), hideClientbr.getValue(), null);

        // Break
        if (breakTypeCrystal.getValue().equalsIgnoreCase("Swing")) {
            mc.playerController.attackEntity(mc.player, cr);
        } else {
            mc.player.connection.sendPacket(new CPacketUseEntity(cr));
        }
        // 143 5 159

        // Cancel crystal is useless, but whatever
        if (cancelCrystal.getValue()) {
            cr.setDead();
            mc.world.removeAllEntities();
            mc.world.getLoadedEntityList();
        }

        // Add limitBreak things
        switch(limitBreakPacket.getValue()) {
            case "Tick":
                breakPacketLimit.addCrystalId(cr.getPosition(), cr.entityId, 0, lomitBreakPacketTick.getValue());
                break;
            case "Time":
                breakPacketLimit.addCrystalId(cr.getPosition(), cr.entityId, limitBreakPacketTime.getValue());
                break;
        }

        // For limiting place packets
        tickBeforeBreak = tickDelayBreak.getValue();
        checkTimeBreak = true;
        timeBreak = System.currentTimeMillis();

        // placeAfter things
        if (placeAfterBreak.getValue()) {
            BlockPos position = forceBreak == null ? cr.getPosition().add(0, -1, 0) : (forceBreakPlace == null ? cr.getPosition().add(0, -1, 0) : forceBreakPlace);
            // instaPlace is fucking useless
            if (instaPlace.getValue()) {
                BlockPos crystal = null;
                if (checkinstaPlace.getValue())
                    crystal = getTargetPlacing(targetPlacing.getValue()).crystal;

                if (checkinstaPlace.getValue() && crystal != null) {
                    if (!sameBlockPos(position, crystal))
                        crystal = position;
                }
                // Ok, lets make instaPlace actual useful
                EnumHand hand = getHandCrystal();
                if (hand != null)
                    placeCrystal(crystal == null ? position : crystal, hand, true);

            } else {
                // ForcePlace is fine
                forcePlaceCrystal = position;
                if (forceBreak == null) {
                    forcePlaceDamage = bestBreak.damage;
                    forcePlaceTarget = bestBreak.target;
                } else {
                    forcePlaceDamage = 10;
                    forcePlaceTarget = new PlayerInfo(mc.player, 0);
                }
            }
        }
        // Reset forceBreak
        forceBreak = null;
        forceBreakPlace = null;

        // Ehm, isnt this always true? idk if u put this then it means sometimes somehow it crashed, lets not touch it
        if (bestBreak.target != null) {
            // Apparently this is for slowBreak, nice!
            if (Math.abs(bestBreak.target.entity.posX - bestBreak.target.entity.prevPosX) + Math.abs(bestBreak.target.entity.posZ - bestBreak.target.entity.prevPosZ) < speedActivation.getValue()) {
                switch (slowBreak.getValue()) {
                    case "Tick":
                        listPlayersBreak.add(new slowBreakPlayers(bestBreak.target.entity.getName(), tickSlowBreak.getValue(), false));
                        break;
                    case "Time":
                        listPlayersBreak.add(new slowBreakPlayers(bestBreak.target.entity.getName(), timeSlowBreak.getValue()));
                        break;
                }
            }
        }

        // for showing break crystal per second, this is temporany waiting the crystal to spawn
        if (showBreakCrystalsSecond.getValue())
            attempedCrystalBreak.addCrystalId(cr.getPosition(), cr.entityId, 500);


        // AnvilCity
        if (this.anvilCity.getText().length() > 0)
            // If we are pressing a button
            if (Keyboard.isKeyDown(KeyBoardClass.getKeyFromChar(this.anvilCity.getText().charAt(0))) && bestBreak.damage > 5) {
                // 618, 1, 366, 621 1 366
                boolean isCity = false;
                BlockPos anvilPosition = BlockPos.ORIGIN;
                final int[] endCrystalPositions = {(int) cr.posX, (int) cr.posY, (int) cr.posZ};
                // Check if the target is getting city
                for(Vec3i surround : new Vec3i[]{
                        new Vec3i(1, 0, 0),
                        new Vec3i(-1, 0, 0),
                        new Vec3i(0, 0, 1),
                        new Vec3i(0, 0, -1)
                }) { // 11, 2, -9
                    final int[] surroundPosition = new int[] {endCrystalPositions[0] + surround.x, endCrystalPositions[1], endCrystalPositions[2] + surround.z};
                    for(EntityPlayer t : getBasicPlayers(40.0).collect(Collectors.toList())) {
                        int[] playerPosition = new int[]{(int) t.posX, (int) t.posY, (int) t.posZ};
                        if (playerPosition[1] == surroundPosition[1]) {
                            if (playerPosition[0] == surroundPosition[0]) {
                                if (Math.abs(playerPosition[2] - surroundPosition[2]) == 1) {
                                    if (BlockUtil.getBlock(cr.getPosition().add(surround)) instanceof BlockAir) {
                                        isCity = true;
                                        anvilPosition = cr.getPosition().add(surround);
                                        break;
                                    }
                                }
                            } else if (playerPosition[2] == surroundPosition[2]) {
                                if (Math.abs(playerPosition[0] - surroundPosition[0]) == 1) {
                                    if (BlockUtil.getBlock(cr.getPosition().add(surround)) instanceof BlockAir) {
                                        isCity = true;
                                        anvilPosition = cr.getPosition().add(surround);
                                        break;
                                    }
                                }
                            }

                        }
                    }
                } //622 2 357

                if (isCity) {
                    // Get anvil
                    int slot = InventoryUtil.findFirstBlockSlot(Blocks.ANVIL.getClass(), 0, 8);
                    if (slot != -1) { // 622 2 357
                        isAnvilling = true;
                        java.util.Timer t = new java.util.Timer();
                        BlockPos finalCity = anvilPosition;
                        blockCity = anvilPosition;
                        crystalAnvil = cr.getPosition().add(0, -1, 0);
                        t.schedule(
                                new java.util.TimerTask() {
                                    @Override
                                    public void run() {
                                        int oldSlot = mc.player.inventory.currentItem;
                                        // Place anvil
                                        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                                        PlacementUtil.place(finalCity, EnumHand.MAIN_HAND, rotate.getValue(), false);
                                        // Return back
                                        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
                                        t.cancel();
                                    }
                                },
                                placeAnvil.getValue()
                        );
                    }
                }
            } else isAnvilling = false;
            // 621 1 366
        else isAnvilling = false;

        return true;
    }
    BlockPos blockCity = null;

    // Function swing arm
    private void swingArm(String swingMode, boolean hideClient, EnumHand handSwingDef) {
        EnumHand[] handSwing;
        if (handSwingDef == null) {
            switch (swingMode) {
                case "Both": {
                    handSwing = new EnumHand[]{
                            EnumHand.MAIN_HAND,
                            EnumHand.OFF_HAND
                    };
                    break;
                }
                case "Offhand": {
                    handSwing = new EnumHand[]{
                            EnumHand.OFF_HAND
                    };
                    break;
                }
                default: {
                    handSwing = new EnumHand[]{
                            EnumHand.MAIN_HAND,
                    };
                    break;
                }
            }
        }
        else handSwing = new EnumHand[] {handSwingDef};

        for(EnumHand hand : handSwing) {
            if (hideClient) {
                if (hideClientbr.getValue()) {
                    // Packet Swing
                    mc.player.connection.sendPacket(new CPacketAnimation(hand));
                } else {
                    // Packet swing + client side
                    mc.player.swingArm(hand);
                }
            } else {
                // Client side only
                ItemStack stack = mc.player.getHeldItem(hand);
                if (!stack.isEmpty() && stack.getItem().onEntitySwing(mc.player, stack)) {
                    return;
                }
                mc.player.swingProgressInt = -1;
                mc.player.isSwingInProgress = true;
                mc.player.swingingHand = hand;
            }
        }
    }

    // endregion

    // region antiCity

    EntityEnderCrystal possibleCrystal() {
        List<BlockPos> offsetPattern = this.getOffsets();

        for(BlockPos pos : offsetPattern) {
            for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
                if (entity instanceof EntityEnderCrystal && destroyCrystal.getValue()) {
                    return (EntityEnderCrystal) entity;
                }
            }

            if (destroyAboveCrystal.getValue()) {
                for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {
                    if (entity instanceof EntityEnderCrystal) {
                        if (sameBlockPos(entity.getPosition(), pos)) {
                            return (EntityEnderCrystal) entity;
                        }
                    }
                }
            }
        }
        return null;
    }

    List<BlockPos> getOffsets() {
        BlockPos playerPos = this.getPlayerPos();
        ArrayList<BlockPos> offsets = new ArrayList<BlockPos>();
        if (this.allowNon1x1.getValue()) {
            int z;
            int x;
            double decimalX = Math.abs(mc.player.posX) - Math.floor(Math.abs(mc.player.posX));
            double decimalZ = Math.abs(mc.player.posZ) - Math.floor(Math.abs(mc.player.posZ));
            int lengthXPos = this.calcLength(decimalX, false);
            int lengthXNeg = this.calcLength(decimalX, true);
            int lengthZPos = this.calcLength(decimalZ, false);
            int lengthZNeg = this.calcLength(decimalZ, true);
            ArrayList<BlockPos> tempOffsets = new ArrayList<BlockPos>();
            offsets.addAll(this.getOverlapPos());
            for (x = 1; x < lengthXPos + 1; ++x) {
                tempOffsets.add(this.addToPlayer(playerPos, x, 0.0, 1 + lengthZPos));
                tempOffsets.add(this.addToPlayer(playerPos, x, 0.0, -(1 + lengthZNeg)));
            }
            for (x = 0; x <= lengthXNeg; ++x) {
                tempOffsets.add(this.addToPlayer(playerPos, -x, 0.0, 1 + lengthZPos));
                tempOffsets.add(this.addToPlayer(playerPos, -x, 0.0, -(1 + lengthZNeg)));
            }
            for (z = 1; z < lengthZPos + 1; ++z) {
                tempOffsets.add(this.addToPlayer(playerPos, 1 + lengthXPos, 0.0, z));
                tempOffsets.add(this.addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, z));
            }
            for (z = 0; z <= lengthZNeg; ++z) {
                tempOffsets.add(this.addToPlayer(playerPos, 1 + lengthXPos, 0.0, -z));
                tempOffsets.add(this.addToPlayer(playerPos, -(1 + lengthXNeg), 0.0, -z));
            }
            for (BlockPos pos : tempOffsets) {
                if (getDown(pos)) {
                    offsets.add(pos.add(0, -1, 0));
                }
                offsets.add(pos);
            }
        } else {
            offsets.add(playerPos.add(0, -1, 0));
            for (int[] surround : new int[][]{
                    {1, 0},
                    {0, 1},
                    {-1, 0},
                    {0, -1}
            }) {
                if (getDown(playerPos.add(surround[0], 0, surround[1])))
                    offsets.add(playerPos.add(surround[0], -1, surround[1]));

                offsets.add(playerPos.add(surround[0], 0, surround[1]));
            }
        }
        return offsets;
    }

    public static boolean getDown(BlockPos pos) {

        for (EnumFacing e : EnumFacing.values())
            if (!mc.world.isAirBlock(pos.add(e.getDirectionVec())))
                return false;

        return true;

    }
    BlockPos addToPlayer(BlockPos playerPos, double x, double y, double z) {
        if (playerPos.getX() < 0) {
            x = -x;
        }
        if (playerPos.getY() < 0) {
            y = -y;
        }
        if (playerPos.getZ() < 0) {
            z = -z;
        }
        return playerPos.add(x, y, z);
    }

    List<BlockPos> getOverlapPos() {
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        double decimalX = mc.player.posX - Math.floor(mc.player.posX);
        double decimalZ = mc.player.posZ - Math.floor(mc.player.posZ);
        int offX = this.calcOffset(decimalX);
        int offZ = this.calcOffset(decimalZ);
        positions.add(this.getPlayerPos());
        for (int x = 0; x <= Math.abs(offX); ++x) {
            for (int z = 0; z <= Math.abs(offZ); ++z) {
                int properX = x * offX;
                int properZ = z * offZ;
                positions.add(this.getPlayerPos().add(properX, -1, properZ));
            }
        }
        return positions;
    }

    int calcOffset(double dec) {
        return dec >= 0.7 ? 1 : (dec <= 0.3 ? -1 : 0);
    }


    int calcLength(double decimal, boolean negative) {
        if (negative) {
            return decimal <= 0.3 ? 1 : 0;
        }
        return decimal >= 0.7 ? 1 : 0;
    }

    BlockPos getPlayerPos() {
        double decimalPoint = mc.player.posY - Math.floor(mc.player.posY);
        return new BlockPos(mc.player.posX, decimalPoint > 0.8 ? Math.floor(mc.player.posY) + 1.0 : Math.floor(mc.player.posY), mc.player.posZ);
    }

    // endregion

    //region predict

    // This function is called by a thread and, given a list of entity, it return
    // Every predict of every players
    List<EntityPlayer> getPredicts(List<EntityPlayer> players, PredictUtil.PredictSettings settings) {
        players.replaceAll(entity -> PredictUtil.predictPlayer(entity, settings));
        return players;
    }

    // Given a list of entity, it split the list in multiple list. Is used for multithreading
    List<List<EntityPlayer>> splitListEntity(List<EntityPlayer> start, int nThreads) {
        // If we have only1  thread, return only 1 thing
        if (nThreads == 1)
            return new ArrayList<List<EntityPlayer>>() {
                {
                    add(start);
                }
            };
        // Get n^Possibilites
        int count;
        if ((count = start.size()) == 0)
            return null;
        // Output
        List<List<EntityPlayer>> output = new ArrayList<>(nThreads);
        for (int i = 0; i < nThreads; i++) output.add(new ArrayList<>());

        // Add everything
        for (int i = 0; i < count; i++) {
            output.get(i % nThreads).add(start.get(i));
        }

        // Return
        return output;
    }

    //endregion

    //region utils

    Vec3d movingPlaceNow = new Vec3d(-1f, -1f, -1f);
    Vec3d movingBreakNow = new Vec3d(-1f, -1f, -1f);
    BlockPos lastBestPlace = null;
    BlockPos lastBestBreak = null;
    // This function is used for getting a basic list of possible players
    Stream<EntityPlayer> getBasicPlayers(double rangeEnemySQ) {
        try {
            return mc.world.playerEntities.stream()
                    .filter(entity -> entity.getDistanceSq(mc.player) <= rangeEnemySQ)
                    .filter(entity -> !EntityUtil.basicChecksEntity(entity))
                    .filter(entity -> entity.getHealth() > 0.0f);
        }catch (Exception e) {
            return new ArrayList<EntityPlayer>().stream();
        }
    }

    boolean lookingCrystal(EntityEnderCrystal cr) {
        Vec3d positionEyes = mc.player.getPositionEyes(mc.getRenderPartialTicks());
        Vec3d rotationEyes = new Vec3d(Math.cos(xPlayerRotation)*Math.cos(yPlayerRotation),
                                             Math.sin(xPlayerRotation)*Math.cos(yPlayerRotation),
                                        Math.sin(yPlayerRotation));
        // Precision
        int precision = 2;
        // Iterate for every blocks
        for (int i = 0; i < breakRange.getValue().intValue() + 1; i++) {
            // Iterate for the precision
            for (int j = precision; j > 0; j--) {
                // Iterate for all players
                // Get box of the player
                AxisAlignedBB playerBox = cr.getEntityBoundingBox();
                // Get coordinate of the vec3d
                double xArray = positionEyes.x + (rotationEyes.x * i) + rotationEyes.x / j;
                double yArray = positionEyes.y + (rotationEyes.y * i) + rotationEyes.y / j;
                double zArray = positionEyes.z + (rotationEyes.z * i) + rotationEyes.z / j;
                // If it's inside
                if (playerBox.maxY >= yArray && playerBox.minY <= yArray
                        && playerBox.maxX >= xArray && playerBox.minX <= xArray
                        && playerBox.maxZ >= zArray && playerBox.minZ <= zArray) {
                    return true;
                }
            }
        }
        return false;
    }

    // Say if two blockPos are the same
    boolean sameBlockPos(BlockPos first, BlockPos second) {
        if (first == null || second == null)
            return false;
        return first.getX() == second.getX() && first.getY() == second.getY() && first.getZ() == second.getZ();
    }

    // This is used for getting the box of a block
    AxisAlignedBB getBox(BlockPos centreBlock) {
        // Min + Max
        double minX = centreBlock.getX();
        double maxX = centreBlock.getX() + 1;
        double minZ = centreBlock.getZ();
        double maxZ = centreBlock.getZ() + 1;
        // Return box
        return new AxisAlignedBB(minX, centreBlock.getY(), minZ, maxX, centreBlock.getY() + 1, maxZ);
    }

    // This is used for getting the box of a block
    AxisAlignedBB getBox(double x, double y, double z) {
        // Min + Max
        double minX = x;
        double maxX = x + 1;
        double minZ = z;
        double maxZ = z + 1;
        // Return box
        return new AxisAlignedBB(minX, y, minZ, maxX, y + 1, maxZ);
    }


    // This function is for displaying things
    public void onWorldRender(RenderEvent event) {
        if (!this.isEnabled()) {
            bestPlace = null;
            bestBreak = null;
            managerRenderBlocks.blocks.clear();
            movingPlaceNow = new Vec3d(0f, 0f, 0f);
            movingBreakNow = new Vec3d(0f, 0f, 0f);
        }

        toRender.forEach(renderClass::render);

        managerRenderBlocks.render();

        // If we have a bestPlace
        if (bestPlace != null && bestPlace.crystal != null) {
            if (!movingPlace.getValue())
                drawBoxMain(typePlace.getValue(), bestPlace.crystal, placeDimension.getValue(), slabHeightPlace.getValue(), true, -1);
            else
                lastBestPlace = bestPlace.crystal;
            // If fadeCa, add it to render
            if (fadeCapl.getValue())
                managerRenderBlocks.addRender(true , bestPlace.crystal);
        }

        if (movingPlace.getValue() && lastBestPlace != null) {
            if (movingPlaceNow.y == -1 && movingBreakNow.x == -1 && movingPlaceNow.z == -1) {
                movingPlaceNow = new Vec3d((float) lastBestPlace.getX(), (float) lastBestPlace.getY(), (float) lastBestPlace.getZ());
            }

            movingPlaceNow = new Vec3d(
                    movingPlaceNow.x + (lastBestPlace.getX() - movingPlaceNow.x) * movingPlaceSpeed.getValue().floatValue(),
                    movingPlaceNow.y + (lastBestPlace.getY() - movingPlaceNow.y) * movingPlaceSpeed.getValue().floatValue(),
                    movingPlaceNow.z + (lastBestPlace.getZ() - movingPlaceNow.z) * movingPlaceSpeed.getValue().floatValue()
            );

            drawBoxMain(typePlace.getValue(), movingPlaceNow.x, movingPlaceNow.y, movingPlaceNow.z, placeDimension.getValue(), slabHeightPlace.getValue(), true, -1);

            if (Math.abs(movingPlaceNow.x - lastBestPlace.getX()) <= .125  && Math.abs(movingPlaceNow.y - lastBestPlace.getY()) <= .125 && Math.abs(movingPlaceNow.z - lastBestPlace.getZ()) <= .125) {
                lastBestPlace = null;
            }


        }


        // If we have a bestBreak
        if (bestBreak != null && bestBreak.crystal != null &&
                (!placeDominant.getValue() || (bestPlace != null && bestPlace.crystal != null && !sameBlockPos(bestPlace.crystal, bestBreak.crystal.getPosition().add(0, -1, 0))))) {
            if (!movingBreak.getValue()) {
                drawBoxMain(typeBreak.getValue(), bestBreak.crystal.getPosition().add(0, -1, 0), breakDimension.getValue(), slabHeightBreak.getValue(), false, -1);
            } else if (movingBreak.getValue()) {

                lastBestBreak = bestBreak.crystal.getPosition().add(0, -1, 0);

            }
            // If fadeCa, add it to render
            if (fadeCabr.getValue())
                managerRenderBlocks.addRender(false , bestBreak.crystal.getPosition().add(0, -1, 0));
        }
        if (movingBreak.getValue() && lastBestBreak != null) {
            if (movingBreakNow.y == -1 && movingBreakNow.x == -1 && movingBreakNow.z == -1) {
                BlockPos pos = bestBreak.crystal.getPosition().add(0, -1, 0);
                movingBreakNow = new Vec3d((float) pos.getX(), (float) pos.getY(), (float) pos.getZ());
            }

            movingBreakNow = new Vec3d(
                    movingBreakNow.x + (lastBestBreak.getX() - movingBreakNow.x) * movingBreakSpeed.getValue().floatValue(),
                    movingBreakNow.y + (lastBestBreak.getY() - movingBreakNow.y) * movingBreakSpeed.getValue().floatValue(),
                    movingBreakNow.z + (lastBestBreak.getZ() - movingBreakNow.z) * movingBreakSpeed.getValue().floatValue()
            );

            drawBoxMain(typeBreak.getValue(), movingBreakNow.x, movingBreakNow.y, movingBreakNow.z, breakDimension.getValue(), slabHeightBreak.getValue(), false, -1);

            if (Math.abs(movingBreakNow.x - lastBestBreak.getX()) <= .125  && Math.abs(movingBreakNow.y - lastBestBreak.getY()) <= .125 && Math.abs(movingBreakNow.z - lastBestBreak.getZ()) <= .125) {
                lastBestBreak = null;
            }
        }

        // Display everything else
        toDisplay.forEach(display::draw);

        if (predictSurround.getValue() && !predictPacketSurround.getValue())
            // Check every damaged blocks
            mc.renderGlobal.damagedBlocks.forEach((integer, destroyBlockProgress) -> {

                // If we are eating, dont do it lol
                if (stopGapple(false)) {
                    return;
                }

                // Get crystal hand
                EnumHand hand = getHandCrystal();
                // If no hand found
                if (hand == null)
                    return;

                // Minecraft is strange
                if (destroyBlockProgress != null) {

                    // If air (Minecraft is strange)
                    BlockPos blockPos = destroyBlockProgress.getPosition();

                    if (mc.world.getBlockState(blockPos).getBlock() == Blocks.AIR) return;

                    // Check distance
                    if (blockPos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) <= placeRange.getValue()) {
                        // If percent
                        if ( destroyBlockProgress.getPartialBlockDamage() / 2 * 25 >= percentSurround.getValue() ) {
                            placeSurroundBlock(blockPos, hand);
                        }
                    }
                }
            });

        if (placeRender++ > extendedPlace.getValue())
            bestPlace = new CrystalInfo.PlaceInfo(-100, null, null, 100d);
        if (breakRender++ > extendedBreak.getValue())
            bestBreak = new CrystalInfo.NewBreakInfo(-100, null, null, 100d);

    }

    void drawBoxMain(String type, BlockPos position, String dimension, double heightSlab, boolean place, int alpha) {
        // If we are drawing a circle, we have to follow this
        if (dimension.equals("Circle")) {

            // Get the real alpha
            int alphaValue = alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha;

            // Draw circle (thanks cosmos!)
            RenderUtil.drawCircle(position.x + .5F, position.getY() + 1, position.z + .5F, place ? rangeCirclePl.getValue() : rangeCircleBr.getValue(),
                    place ? new GSColor(firstVerticeOutlineBot.getColor(), alphaValue) :
                            new GSColor(firstVerticeOutlineBotbr.getColor(), alphaValue));
        } else {
            // Get box
            AxisAlignedBB box = getBox(position);
            int mask = GeometryMasks.Quad.ALL;
            // For custom dimensions
            if (dimension.equals("Flat")) {
                mask = GeometryMasks.Quad.UP;
                box = new AxisAlignedBB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
            } else if (dimension.equals("Slab")) {
                box = new AxisAlignedBB(box.minX, box.maxY - heightSlab, box.minZ, box.maxX, box.maxY, box.maxZ);
            }

            // Switch for types, isnt this really clean!?!? I mean, not the code above lol
            switch (type) {
                case "Outline": {
                    displayOutline(box, place, alpha);
                    break;
                }
                case "Fill": {
                    displayFill(box, mask, place, alpha);
                    break;
                }
                case "Both": {
                    displayFill(box, mask, place, alpha);

                    displayOutline(box, place, alpha);
                    break;
                }
            }
        }
    }

    void drawBoxMain(String type, double x, double y, double z, String dimension, double heightSlab, boolean place, int alpha) {
        // If we are drawing a circle, we have to follow this
        if (dimension.equals("Circle")) {

            // Get the real alpha
            int alphaValue = alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha;

            // Draw circle (thanks cosmos!)
            RenderUtil.drawCircle((float) (x + .5), (float) (y + 1), (float) (z + .5F), place ? rangeCirclePl.getValue() : rangeCircleBr.getValue(),
                    place ? new GSColor(firstVerticeOutlineBot.getColor(), alphaValue) :
                            new GSColor(firstVerticeOutlineBotbr.getColor(), alphaValue));
        } else {
            // Get box
            AxisAlignedBB box = getBox(x, y, z);
            int mask = GeometryMasks.Quad.ALL;
            // For custom dimensions
            if (dimension.equals("Flat")) {
                mask = GeometryMasks.Quad.UP;
                box = new AxisAlignedBB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
            } else if (dimension.equals("Slab")) {
                box = new AxisAlignedBB(box.minX, box.maxY - heightSlab, box.minZ, box.maxX, box.maxY, box.maxZ);
            }

            // Switch for types, isnt this really clean!?!? I mean, not the code above lol
            switch (type) {
                case "Outline": {
                    displayOutline(box, place, alpha);
                    break;
                }
                case "Fill": {
                    displayFill(box, mask, place, alpha);
                    break;
                }
                case "Both": {
                    displayFill(box, mask, place, alpha);

                    displayOutline(box, place, alpha);
                    break;
                }
            }
        }
    }

    void displayOutline(AxisAlignedBB box, boolean place, int alpha) {
        renderCustomOutline(box, place, alpha);
    }

    void displayFill(AxisAlignedBB box, int mask, boolean place, int alpha) {
        renderFillCustom(box, mask, place, alpha);
    }

    // If we can break that block
    private boolean canBreak(BlockPos pos) {
        final IBlockState blockState = mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();
        //noinspection deprecation
        return block.getBlockHardness(blockState, mc.world, pos) != -1;
    }

    // Listen for when we are breaking a block
    @EventHandler
    private final Listener<DamageBlockEvent> listener = new Listener<>(event -> {
        try {

            if (mc.world == null || mc.player == null || !predictPacketSurround.getValue()) return;
            if (!canBreak(event.getBlockPos()) || event.getBlockPos() == null) return;

            // If air (Minecraft is strange)
            BlockPos blockPos = event.getBlockPos();

            if (mc.world.getBlockState(blockPos).getBlock() == Blocks.AIR) return;

            if (packetsBlocks.stream().anyMatch(e -> sameBlockPos(e.block, blockPos)))
                return;

            // Check distance
            if (blockPos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) <= placeRange.getValue()) {
                // If percent
                float armourPercent = armourFacePlace.getValue() / 100.0f;

                // Check around the block
                for (Vec3i surround : new Vec3i[]{
                        new Vec3i(1, 0, 0),
                        new Vec3i(-1, 0, 0),
                        new Vec3i(0, 0, 1),
                        new Vec3i(0, 0, -1)
                }) {
                    // Get possible players that collide
                    List<Entity> players =
                            new ArrayList<>(mc.world.getEntitiesWithinAABBExcludingEntity(
                                    null, new AxisAlignedBB(blockPos.add(surround))));

                    PlayerInfo info = null;
                    // Iterate
                    for (Entity pl : players) {
                        // Remove us and remove players above
                        if (pl instanceof EntityPlayer && pl != mc.player && pl.posY + .5 >= blockPos.y) {
                            EntityPlayer temp;
                            // If we found 1, we are fine
                            info = new PlayerInfo((temp = (EntityPlayer) pl), armourPercent,
                                    temp.getTotalArmorValue(),
                                    (float) temp.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
                            break;
                        }
                    }

                    // This is for force quitting. Is used for not multiPlacing
                    boolean quit = false;
                    if (info != null) {
                        // Best values
                        BlockPos coords = null;
                        double damage = Double.MIN_VALUE;
                        // Set to air the block for calculating the damage
                        Block toReplace = BlockUtil.getBlock(blockPos);
                        mc.world.setBlockToAir(blockPos);
                        // Check around
                        for (Vec3i placement : new Vec3i[]{
                                new Vec3i(1, -1, 0),
                                new Vec3i(-1, -1, 0),
                                new Vec3i(0, -1, 1),
                                new Vec3i(0, -1, -1)
                        }) {
                            // If it's placable
                            BlockPos temp;
                            if (CrystalUtil.canPlaceCrystal((temp = blockPos.add(placement)), newPlace.getValue())) {

                                // Check the damage on us
                                if (DamageUtil.calculateDamage(temp.getX() + .5D, temp.getY() + 1D, temp.getZ() + .5D, mc.player, ignoreTerrain.getValue()) >= maxSelfDamageSur.getValue())
                                    continue;

                                // If there is a crystal, stop
                                if (!placeOnCrystal.getValue() && !isCrystalHere(temp)) {
                                    quit = true;
                                    break;
                                }

                                // Check damage on the target
                                float damagePlayer = DamageUtil.calculateDamageThreaded(temp.getX() + .5D, temp.getY() + 1D, temp.getZ() + .5D,
                                        info, ignoreTerrain.getValue());

                                // IF >, add
                                if (damagePlayer > damage) {
                                    damage = damagePlayer;
                                    coords = temp;
                                    quit = true;
                                }
                            }
                        }

                        // Reset block
                        mc.world.setBlockState(blockPos, toReplace.getDefaultState());

                        // Add to packet block
                        if (coords != null) {
                            packetsBlocks.add(new packetBlock(coords, tickPacketBreak.getValue(), tickMaxPacketBreak.getValue()));
                        }

                        // Quit
                        if (quit)
                            break;

                    }


                }

            }
        } catch (Exception ignored) {}


    });

    // Check for that block if it's possible to city, if yes place crystal
    void placeSurroundBlock(BlockPos blockPos, EnumHand hand) {
        float armourPercent = armourFacePlace.getValue() / 100.0f;

        // Check around block
        for (Vec3i surround : new Vec3i[]{
                new Vec3i(1, 0, 0),
                new Vec3i(-1, 0, 0),
                new Vec3i(0, 0, 1),
                new Vec3i(0, 0, -1)
        }) {

            // Get possible players
            List<Entity> players =
                    new ArrayList<>(mc.world.getEntitiesWithinAABBExcludingEntity(
                            null, new AxisAlignedBB(blockPos.add(surround))));

            // Find
            PlayerInfo info = null;
            for(Entity pl : players) {
                // If it's not us and if it's not above
                if (pl instanceof EntityPlayer && pl != mc.player && pl.posY + .5 >= blockPos.y) {
                    EntityPlayer temp;
                    // Add
                    info = new PlayerInfo( (temp = (EntityPlayer) pl), armourPercent,
                            temp.getTotalArmorValue(),
                            (float) temp.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
                    break;
                }
            }

            // For force quitting. Is used for non multiPlacing
            boolean quit = false;
            if (info != null) {
                // Best
                BlockPos coords = null;
                double damage = Double.MIN_VALUE;
                // For calculating the damage, set to air
                Block toReplace = BlockUtil.getBlock(blockPos);
                mc.world.setBlockToAir(blockPos);
                // Check around
                for (Vec3i placement : new Vec3i[]{
                        new Vec3i(1, -1, 0),
                        new Vec3i(-1, -1, 0),
                        new Vec3i(0, -1, 1),
                        new Vec3i(0, -1, -1)
                }) {
                    // If we can place the crystal
                    BlockPos temp;
                    if (CrystalUtil.canPlaceCrystal((temp = blockPos.add(placement)), newPlace.getValue()) ) {

                        // Check damage
                        if (DamageUtil.calculateDamage(temp.getX() + .5D, temp.getY() + 1D, temp.getZ() + .5D, mc.player, ignoreTerrain.getValue()) >= maxSelfDamageSur.getValue() )
                            continue;

                        // If there is a crystal, stop
                        if ( !placeOnCrystal.getValue() && !isCrystalHere(temp)) {
                            quit = true;
                            break;
                        }

                        // Calculate damage
                        float damagePlayer = DamageUtil.calculateDamageThreaded(temp.getX() + .5D, temp.getY() + 1D, temp.getZ() + .5D,
                                info, ignoreTerrain.getValue());
                        // If best
                        if (damagePlayer > damage) {
                            damage = damagePlayer;
                            coords = temp;
                            quit = true;
                        }
                    }
                }

                // Reset surround
                mc.world.setBlockState(blockPos, toReplace.getDefaultState());

                // Place crystal
                if (coords != null) {
                    placeCrystal(coords, hand, false);
                    placedCrystal = true;
                }

                // Quit
                if (quit)
                    break;

            }


        }
    }

    // This is used for creating the box gradient
    private void renderCustomOutline(AxisAlignedBB hole, boolean place, int alpha) {

        ArrayList<GSColor> colors = new ArrayList<>();

        /*
            That's a really long code. It's not complex, just long
         */

        if (place) {
            switch (NVerticesOutlineBot.getValue()) {
                case "1":
                    colors.add(new GSColor(firstVerticeOutlineBot.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeOutlineBot.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeOutlineBot.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeOutlineBot.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                    break;
                case "2":
                    if (direction2OutLineBot.getValue().equals("X")) {
                        colors.add(new GSColor(firstVerticeOutlineBot.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineBot.getValue(), alpha == -1 ? secondVerticeOutlineBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeOutlineBot.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineBot.getValue(), alpha == -1 ? secondVerticeOutlineBot.getColor().getAlpha() : alpha));
                    } else {
                        colors.add(new GSColor(firstVerticeOutlineBot.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeOutlineBot.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineBot.getValue(), alpha == -1 ? secondVerticeOutlineBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineBot.getValue(), alpha == -1 ? secondVerticeOutlineBot.getColor().getAlpha() : alpha));
                    }
                    break;
                case "4":
                    colors.add(new GSColor(firstVerticeOutlineBot.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(secondVerticeOutlineBot.getValue(), alpha == -1 ? secondVerticeOutlineBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(thirdVerticeOutlineBot.getValue(), alpha == -1 ? thirdVerticeOutlineBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(fourVerticeOutlineBot.getValue(), alpha == -1 ? fourVerticeOutlineBot.getColor().getAlpha() : alpha));
                    break;
            }
            switch (NVerticesOutlineTop.getValue()) {
                case "1":
                    colors.add(new GSColor(firstVerticeOutlineTop.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeOutlineTop.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeOutlineTop.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeOutlineTop.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                    break;
                case "2":
                    if (direction2OutLineTop.getValue().equals("X")) {
                        colors.add(new GSColor(firstVerticeOutlineTop.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineTop.getValue(), alpha == -1 ? secondVerticeOutlineBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeOutlineTop.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineTop.getValue(), alpha == -1 ? secondVerticeOutlineBot.getColor().getAlpha() : alpha));
                    } else {
                        colors.add(new GSColor(firstVerticeOutlineTop.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeOutlineTop.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineTop.getValue(), alpha == -1 ? secondVerticeOutlineBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineTop.getValue(), alpha == -1 ? secondVerticeOutlineBot.getColor().getAlpha() : alpha));
                    }
                    break;
                case "4":
                    colors.add(new GSColor(firstVerticeOutlineTop.getValue(), alpha == -1 ? firstVerticeOutlineBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(secondVerticeOutlineTop.getValue(), alpha == -1 ? secondVerticeOutlineBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(thirdVerticeOutlineTop.getValue(), alpha == -1 ? thirdVerticeOutlineBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(fourVerticeOutlineTop.getValue(), alpha == -1 ? fourVerticeOutlineBot.getColor().getAlpha() : alpha));
                    break;
            }
        }
        else {
            switch (NVerticesOutlineBotbr.getValue()) {
                case "1":
                    colors.add(new GSColor(firstVerticeOutlineBotbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeOutlineBotbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeOutlineBotbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeOutlineBotbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    break;
                case "2":
                    if (direction2OutLineBotbr.getValue().equals("X")) {
                        colors.add(new GSColor(firstVerticeOutlineBotbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineBotbr.getValue(), alpha == -1 ? secondVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeOutlineBotbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineBotbr.getValue(), alpha == -1 ? secondVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    } else {
                        colors.add(new GSColor(firstVerticeOutlineBotbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeOutlineBotbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineBotbr.getValue(), alpha == -1 ? secondVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineBotbr.getValue(), alpha == -1 ? secondVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    }
                    break;
                case "4":
                    colors.add(new GSColor(firstVerticeOutlineBotbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(secondVerticeOutlineBotbr.getValue(), alpha == -1 ? secondVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(thirdVerticeOutlineBotbr.getValue(), alpha == -1 ? thirdVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(fourVerticeOutlineBotbr.getValue(), alpha == -1 ? fourVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    break;
            }
            switch (NVerticesOutlineTopbr.getValue()) {
                case "1":
                    colors.add(new GSColor(firstVerticeOutlineTopbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeOutlineTopbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeOutlineTopbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeOutlineTopbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    break;
                case "2":
                    if (direction2OutLineTopbr.getValue().equals("X")) {
                        colors.add(new GSColor(firstVerticeOutlineTopbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineTopbr.getValue(), alpha == -1 ? secondVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeOutlineTopbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineTopbr.getValue(), alpha == -1 ? secondVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    } else {
                        colors.add(new GSColor(firstVerticeOutlineTopbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeOutlineTopbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineTopbr.getValue(), alpha == -1 ? secondVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeOutlineTopbr.getValue(), alpha == -1 ? secondVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    }
                    break;
                case "4":
                    colors.add(new GSColor(firstVerticeOutlineTopbr.getValue(), alpha == -1 ? firstVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(secondVerticeOutlineTopbr.getValue(), alpha == -1 ? secondVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(thirdVerticeOutlineTopbr.getValue(), alpha == -1 ? thirdVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(fourVerticeOutlineTopbr.getValue(), alpha == -1 ? fourVerticeOutlineBotbr.getColor().getAlpha() : alpha));
                    break;
            }
        }

        RenderUtil.drawBoundingBox(hole, outlineWidthpl.getValue(), colors.toArray(new GSColor[7]));
    }

    // This is used for the filling the box gradient
    void renderFillCustom(AxisAlignedBB hole, int mask, boolean place, int alpha) {

        ArrayList<GSColor> colors = new ArrayList<>();

        /*
            Long but not complex
         */

        if (place) {
            switch (NVerticesFillBot.getValue()) {
                case "1":
                    colors.add(new GSColor(firstVerticeFillBot.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeFillBot.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeFillBot.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeFillBot.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                    break;
                case "2":
                    if (direction2FillBot.getValue().equals("X")) {
                        colors.add(new GSColor(firstVerticeFillBot.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillBot.getValue(), alpha == -1 ? secondVerticeFillBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeFillBot.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillBot.getValue(), alpha == -1 ? secondVerticeFillBot.getColor().getAlpha() : alpha));
                    } else {
                        colors.add(new GSColor(firstVerticeFillBot.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeFillBot.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillBot.getValue(), alpha == -1 ? secondVerticeFillBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillBot.getValue(), alpha == -1 ? secondVerticeFillBot.getColor().getAlpha() : alpha));
                    }
                    break;
                case "4":
                    colors.add(new GSColor(firstVerticeFillBot.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(secondVerticeFillBot.getValue(), alpha == -1 ? secondVerticeFillBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(thirdVerticeFillBot.getValue(), alpha == -1 ? thirdVerticeFillBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(fourVerticeFillBot.getValue(), alpha == -1 ? fourVerticeFillBot.getColor().getAlpha() : alpha));
                    break;
            }
            switch (NVerticesFillTop.getValue()) {
                case "1":
                    colors.add(new GSColor(firstVerticeFillTop.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeFillTop.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeFillTop.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeFillTop.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                    break;
                case "2":
                    if (direction2FillTop.getValue().equals("X")) {
                        colors.add(new GSColor(firstVerticeFillTop.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillTop.getValue(), alpha == -1 ? secondVerticeFillBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeFillTop.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillTop.getValue(), alpha == -1 ? secondVerticeFillBot.getColor().getAlpha() : alpha));
                    } else {
                        colors.add(new GSColor(firstVerticeFillTop.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeFillTop.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillTop.getValue(), alpha == -1 ? secondVerticeFillBot.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillTop.getValue(), alpha == -1 ? secondVerticeFillBot.getColor().getAlpha() : alpha));
                    }
                    break;
                case "4":
                    colors.add(new GSColor(firstVerticeFillTop.getValue(), alpha == -1 ? firstVerticeFillBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(secondVerticeFillTop.getValue(), alpha == -1 ? secondVerticeFillBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(thirdVerticeFillTop.getValue(), alpha == -1 ? thirdVerticeFillBot.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(fourVerticeFillTop.getValue(), alpha == -1 ? fourVerticeFillBot.getColor().getAlpha() : alpha));
                    break;
            }
        }
        else {
            switch (NVerticesFillBotbr.getValue()) {
                case "1":
                    colors.add(new GSColor(firstVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    break;
                case "2":
                    if (direction2FillBotbr.getValue().equals("X")) {
                        colors.add(new GSColor(firstVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    } else {
                        colors.add(new GSColor(firstVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    }
                    break;
                case "4":
                    colors.add(new GSColor(firstVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(secondVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(thirdVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(fourVerticeFillBotbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    break;
            }
            switch (NVerticesFillTopbr.getValue()) {
                case "1":
                    colors.add(new GSColor(firstVerticeFillTopbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeFillTopbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeFillTopbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(firstVerticeFillTopbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    break;
                case "2":
                    if (direction2FillTopbr.getValue().equals("X")) {
                        colors.add(new GSColor(firstVerticeFillTopbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillTopbr.getValue(), alpha == -1 ? secondVerticeFillBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeFillTopbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillTopbr.getValue(), alpha == -1 ? secondVerticeFillBotbr.getColor().getAlpha() : alpha));
                    } else {
                        colors.add(new GSColor(firstVerticeFillTopbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(firstVerticeFillTopbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillTopbr.getValue(), alpha == -1 ? secondVerticeFillBotbr.getColor().getAlpha() : alpha));
                        colors.add(new GSColor(secondVerticeFillTopbr.getValue(), alpha == -1 ? secondVerticeFillBotbr.getColor().getAlpha() : alpha));
                    }
                    break;
                case "4":
                    colors.add(new GSColor(firstVerticeFillTopbr.getValue(), alpha == -1 ? firstVerticeFillBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(secondVerticeFillTopbr.getValue(), alpha == -1 ? secondVerticeFillBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(thirdVerticeFillTopbr.getValue(), alpha == -1 ? thirdVerticeFillBotbr.getColor().getAlpha() : alpha));
                    colors.add(new GSColor(fourVerticeFillTopbr.getValue(), alpha == -1 ? fourVerticeFillBotbr.getColor().getAlpha() : alpha));
                    break;
            }
        }

        RenderUtil.drawBoxProva2(hole, true, 1, colors.toArray(new GSColor[7]), mask, true);
    }

    // Given a list of players, this return every players predictions
    List<EntityPlayer> getPlayersThreaded(int nThread, List<EntityPlayer> players, PredictUtil.PredictSettings settings, int timeOut) {
        // Split list of entity
        List<List<EntityPlayer>> list = splitListEntity(players, nThread);

        // Clear players, we are going to replace it with the prediciton
        List<EntityPlayer> output = new ArrayList<>();
        // Output
        Collection<Future<?>> futures = new LinkedList<>();

        // Start multithreading
        for (int i = 0; i < nThread; i++) {
            int finalI = i;
            // Add them
            futures.add(executor.submit(() -> getPredicts(list.get(finalI), settings) ));
        }

        // For every thread
        for (Future<?> future : futures) {
            try {
                // Get it
                List<EntityPlayer> temp;
                //noinspection unchecked
                temp = (List<EntityPlayer>) future.get(timeOut, TimeUnit.MILLISECONDS);
                // If not null, add
                if (temp != null)
                    output.addAll(temp);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
        }

        return output;
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        this.highestId = -10000;
    }

    void updateHighestID() {
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity.getEntityId() <= highestId) continue;
            highestId = entity.getEntityId();
        }
    }

    void checkID(int id) {
        if (id > highestId)
            highestId = id;
    }

    void attackID(BlockPos pos, int id) {
        try {
            Entity entity = mc.world.getEntityByID(id);
            if (entity == null || entity instanceof EntityEnderCrystal) {
                CPacketUseEntity attack = new CPacketUseEntity();
                ((AccessorCPacketAttack) attack).setId(id);
                ((AccessorCPacketAttack) attack).setAction(CPacketUseEntity.Action.ATTACK);
                mc.player.connection.sendPacket((Packet) attack);
                mc.player.connection.sendPacket((Packet) new CPacketAnimation(EnumHand.MAIN_HAND));
            }
        }catch(Exception e) {
            PistonCrystal.printDebug("Prevented a crash from the ca. If this repet, spam me in dm", true);
            final Logger LOGGER = LogManager.getLogger("GameSense");
            LOGGER.error("[AutoCrystalRewrite] error during the creation of the structure.");
            if (e.getMessage() != null)
                LOGGER.error("[AutoCrystalRewrite] error message: " + e.getClass().getName() + " " + e.getMessage());
            else
                LOGGER.error("[AutoCrystalRewrite] cannot find the cause");
            int i5 = 0;

            if (e.getStackTrace().length != 0) {
                LOGGER.error("[AutoCrystalRewrite] StackTrace Start");
                for (StackTraceElement errorMess : e.getStackTrace()) {
                    LOGGER.error("[AutoCrystalRewrite] " + errorMess.toString());
                }
                LOGGER.error("[AutoCrystalRewrite] StackTrace End");
            }
        }
    }


    //endregion

    //region Packet management

    // This function is used for the rotation
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {
        // If we dont have to rotate
        if (event.getPhase() != Phase.PRE || !rotate.getValue() || lastHitVec == null || mc.world == null || mc.player == null) return;

        // If we reached the last point (Delay)
        if (tick++ > tickAfterRotation.getValue()) {
            lastHitVec = null;
            tick = 0;
            isRotating = false;
            yPlayerRotation = xPlayerRotation = Double.MAX_VALUE;
        } else {
            // If we have to rotate
            Vec2f rotationWanted = RotationUtil.getRotationTo(lastHitVec);
            Vec2f nowRotation;

            // If we have to check or one or the other
            if (yawCheck.getValue() || pitchCheck.getValue()) {

                // This should never happened, but it happened sometimes somehow.
                if (yPlayerRotation == Double.MIN_VALUE)
                    yPlayerRotation = rotationWanted.y;
                else {
                    // Get first if + or -
                    double distanceDo = rotationWanted.y - yPlayerRotation;
                    int direction = distanceDo > 0 ? 1 : -1;

                    // Check if distance is > of what we want
                    if (Math.abs(distanceDo) > pitchStep.getValue()) {
                        // We have to continue the smooth rotation
                        yPlayerRotation = RotationUtil.normalizeAngle(yPlayerRotation + pitchStep.getValue() * direction);
                        // ofc we have to reset tick, we are still rotating
                        tick = 0;
                    } else {
                        // Go straight
                        yPlayerRotation = rotationWanted.y;
                    }
                }
                // Like before
                if (xPlayerRotation == Double.MIN_VALUE)
                    xPlayerRotation = rotationWanted.x;
                else {
                    // Get first if + or -
                    double distanceDo = rotationWanted.x - xPlayerRotation;
                    if (Math.abs(distanceDo) > 180) {
                        distanceDo = RotationUtil.normalizeAngle(distanceDo);
                    }
                    int direction = distanceDo > 0 ? 1 : -1;
                    // Check if distance is > of what we want

                    if (Math.abs(distanceDo) > yawStep.getValue()) {
                        xPlayerRotation = RotationUtil.normalizeAngle(xPlayerRotation + yawStep.getValue() * direction);
                        tick = 0;
                    } else {
                        xPlayerRotation = rotationWanted.x;
                    }
                }
                nowRotation = new Vec2f((float) xPlayerRotation, (float) yPlayerRotation);
            } else {
                nowRotation = rotationWanted;
            }

            PlayerPacket packet = new PlayerPacket(this, nowRotation);
            PlayerPacketManager.INSTANCE.addPacket(packet);
        }
    });

    @EventHandler
    private final Listener<PacketEvent.Send> packetSendListener = new Listener<>(event -> {
        if (mc.world == null || mc.player == null)
            return;
        if  ( entityPredict.getValue() && event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock)event.getPacket();
            if ( bestPlace.crystal != null && sameBlockPos(packet.getPos(), bestPlace.crystal)) {
                int idx = 0;
                for(int i = 1 - offset.getValue(); i <= tryAttack.getValue(); i++) {
                    updateHighestID();
                    java.util.Timer t = new java.util.Timer();
                    int finalI = i;
                    t.schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    attackID(packet.getPos(), highestId + finalI);
                                    t.cancel();
                                }
                            },
                            delayAttacks.getValue() + (++idx * midDelayAttacks.getValue())
                    );

                }

            }
        }
    });

    // This is used for packet recive thing
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Receive> packetReceiveListener = new Listener<>(event -> {
        try {
            if (mc.world == null || mc.player == null)
                return;
            // Spawn object
            if (event.getPacket() instanceof SPacketSpawnObject) {
                // Get it
                SPacketSpawnObject SpawnObject = (SPacketSpawnObject) event.getPacket();
                if (entityPredict.getValue())
                    checkID(SpawnObject.getEntityID());
                // Idk why 51
                if (SpawnObject.getType() == 51) {
                    double[] positions = {
                            SpawnObject.getX() - .5D,
                            SpawnObject.getY() - .5D,
                            SpawnObject.getZ() - .5D
                    };
                    // If limitPacketPlace, remove the crystal
                    if (!limitPacketPlace.getValue().equals("None"))
                        listCrystalsPlaced.removeCrystal(positions[0], positions[1], positions[2]);
                    // If crystalPlace is not null
                    if (crystalPlace != null)
                        // Check if it's it
                        if (sameBlockPos(new BlockPos(positions[0], positions[1], positions[2]), crystalPlace.posCrystal)) {
                            // If yes, remove it
                            crystalPlace = null;
                        }

                    // If we have to check for crystal seconds
                    if (showPlaceCrystalsSecond.getValue())
                        // Check if we tried to place something
                        if (listCrystalsSecondWait.removeCrystal(positions[0], positions[1], positions[2]))
                            // If yes, add
                            crystalSecondPlace.addCrystal(null, 1000);

                    // firstHit thing for the break
                    switch (firstHit.getValue()) {
                        case "Tick":
                            existsCrystal.addCrystal(new BlockPos(positions[0], positions[1], positions[2]), 0, firstHitTick.getValue());
                            break;
                        case "Time":
                            existsCrystal.addCrystal(new BlockPos(positions[0], positions[1], positions[2]), fitstHitTime.getValue());
                            break;
                    }

                    if (predictHit.getValue()) {
                        boolean hit = false;
                        switch (chooseCrystal.getValue()) {
                            case "All":
                                hit = true;
                                break;
                            case "Own":
                                if (endCrystalPlaced.hasCrystal(new BlockPos(positions[0], positions[1], positions[2])))
                                    hit = true;
                                break;
                            case "Smart":
                                if (sameBlockPos(getTargetPlacing(targetPlacing.getValue()).crystal, new BlockPos(positions[0], positions[1], positions[2])))
                                    hit = true;
                                break;
                        }

                        if (hit) {
                            java.util.Timer t = new java.util.Timer();
                            t.schedule(
                                    new java.util.TimerTask() {
                                        @Override
                                        public void run() {
                                            CPacketUseEntity attack = new CPacketUseEntity();
                                            ((AccessorCPacketAttack) attack).setId(SpawnObject.getEntityID());
                                            ((AccessorCPacketAttack) attack).setAction(CPacketUseEntity.Action.ATTACK);
                                            mc.player.connection.sendPacket((Packet) attack);
                                            mc.player.connection.sendPacket((Packet) new CPacketAnimation(EnumHand.MAIN_HAND));
                                            t.cancel();
                                        }
                                    },
                                    predictHitDelay.getValue()
                            );

                        }
                    }
                }
            } else if (event.getPacket() instanceof SPacketSoundEffect) {
                // Sound predict
                final SPacketSoundEffect packetSoundEffect = (SPacketSoundEffect) event.getPacket();
                if (packetSoundEffect.getCategory() == SoundCategory.BLOCKS && packetSoundEffect.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                    for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {
                        if (entity instanceof EntityEnderCrystal) {
                            // SetDead, that's just visual lol
                            if (setDead.getValue() && entity.getDistanceSq(packetSoundEffect.getX(), packetSoundEffect.getY(), packetSoundEffect.getZ()) <= 36.0f) {
                                try {
                                    entity.setDead();
                                } catch (Exception ignored) {}
                            }

                            // For not spamming of packets lol
                            if (attempedCrystalBreak.removeCrystal(packetSoundEffect.getX(), packetSoundEffect.getY(), packetSoundEffect.getZ()))
                                crystalSecondBreak.addCrystal(null, 1000);

                            if (crystalAnvil != null && sameBlockPos(crystalAnvil, new BlockPos(packetSoundEffect.getX(), packetSoundEffect.getY(), packetSoundEffect.getZ()))) {
                                int slot = InventoryUtil.findFirstBlockSlot(Blocks.ANVIL.getClass(), 0, 8);
                                if (slot != -1) {
                                    if (BlockUtil.getBlock(blockCity) instanceof BlockAir) {
                                        int oldSlot = mc.player.inventory.currentItem;
                                        // Place anvil
                                        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                                        PlacementUtil.place(blockCity, EnumHand.MAIN_HAND, rotate.getValue(), false);
                                        // Return back
                                        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
                                    }
                                }

                            }
                        }
                    }
                    breakPacketLimit.removeCrystal(packetSoundEffect.getX(), packetSoundEffect.getY(), packetSoundEffect.getZ());
                }
            } else if (event.getPacket() instanceof SPacketSpawnExperienceOrb) {
                this.checkID(((SPacketSpawnExperienceOrb) event.getPacket()).getEntityID());
            } else if (event.getPacket() instanceof SPacketSpawnPlayer) {
                this.checkID(((SPacketSpawnPlayer) event.getPacket()).getEntityID());
            } else if (event.getPacket() instanceof SPacketSpawnGlobalEntity) {
                this.checkID(((SPacketSpawnGlobalEntity) event.getPacket()).getEntityId());
            } else if (event.getPacket() instanceof SPacketSpawnPainting) {
                this.checkID(((SPacketSpawnPainting) event.getPacket()).getEntityID());
            } else if (event.getPacket() instanceof SPacketSpawnMob) {
                this.checkID(((SPacketSpawnMob) event.getPacket()).getEntityID());
            }
        }catch(ConcurrentModificationException e) {
            PistonCrystal.printDebug("Prevented a crash from the ca. If this repet, spam me in dm", true);
            final Logger LOGGER = LogManager.getLogger("GameSense");
            LOGGER.error("[AutoCrystalRewrite] error during the creation of the structure.");
            if (e.getMessage() != null)
                LOGGER.error("[AutoCrystalRewrite] error message: " + e.getClass().getName() + " " + e.getMessage());
            else
                LOGGER.error("[AutoCrystalRewrite] cannot find the cause");
            int i5 = 0;

            if (e.getStackTrace().length != 0) {
                LOGGER.error("[AutoCrystalRewrite] StackTrace Start");
                for (StackTraceElement errorMess : e.getStackTrace()) {
                    LOGGER.error("[AutoCrystalRewrite] " + errorMess.toString());
                }
                LOGGER.error("[AutoCrystalRewrite] StackTrace End");
            }
        }

    });




    //endregion

}

/*
    Oh wow we are at the end! That's a really looong file
 */