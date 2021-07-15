/*
    Author: TechAle
    Description: Place and break crystals
    Created: 06/28/21
 */
package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.Phase;
import com.gamesense.api.event.events.OnUpdateWalkingPlayerEvent;
import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlayerPacket;
import com.gamesense.api.util.player.PredictUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
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
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;

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
    ModeSetting targetPlacing = registerMode("Target Placing", Arrays.asList("Nearest", "Lowest", "Damage"), "Nearest", () -> logicTarget.getValue());
    ModeSetting targetBreaking = registerMode("Target Breaking", Arrays.asList("Nearest", "Lowest", "Damage"), "Nearest", () -> logicTarget.getValue());
    BooleanSetting stopGapple = registerBoolean("Stop Gapple", false, () -> logicTarget.getValue());
    IntegerSetting tickWaitEat = registerInteger("Tick Wait Eat", 4, 0, 10,
            () -> logicTarget.getValue() && stopGapple.getValue());
    BooleanSetting chorusFruit = registerBoolean("Stop Chorus", false,
            () -> logicTarget.getValue() && stopGapple.getValue());
    BooleanSetting newPlace = registerBoolean("1.13 mode", false, () -> logicTarget.getValue());
    //endregion

    //region Ranges
    BooleanSetting ranges = registerBoolean("Range Section", false);
    DoubleSetting rangeEnemyPlace = registerDouble("Range Enemy Place", 7, 0, 12, () -> ranges.getValue());
    DoubleSetting placeRange = registerDouble("Place Range", 6, 0, 8, () -> ranges.getValue());
    DoubleSetting crystalWallPlace = registerDouble("Wall Range Place", 3.5, 0, 8, () -> ranges.getValue());
    IntegerSetting maxYTargetPlace = registerInteger("Max Y Place", 3, 0, 5, () -> ranges.getValue());
    IntegerSetting minYTargetPlace = registerInteger("Min Y Place", 3, 0, 5, () -> ranges.getValue());
    //endregion

    //region Place
    BooleanSetting place = registerBoolean("Place Section", false);
    ModeSetting placeDelay = registerMode("Place Delay", Arrays.asList("Tick", "Time"), "Tick", () -> place.getValue());
    IntegerSetting tickDelayPlace = registerInteger("Tick Delay Place", 0, 0, 20,
            () -> place.getValue() && placeDelay.getValue().equals("Tick"));
    IntegerSetting timeDelayPlace = registerInteger("TIme Delay Place", 0, 0, 2000,
            () -> place.getValue() && placeDelay.getValue().equals("Time"));
    DoubleSetting minDamagePlace = registerDouble("Min Damage Place", 5, 0, 30, () -> place.getValue());
    DoubleSetting maxSelfDamagePlace = registerDouble("Max Self Damage Place", 12, 0, 30, () -> place.getValue());
    IntegerSetting armourFacePlace = registerInteger("Armour Health%", 20, 0, 100, () -> place.getValue());
    IntegerSetting facePlaceValue = registerInteger("FacePlace HP", 8, 0, 36, () -> place.getValue());
    DoubleSetting minFacePlaceDmg = registerDouble("FacePlace Dmg", 2, 0, 10, () -> place.getValue());
    BooleanSetting antiSuicide = registerBoolean("AntiSuicide", true, () -> place.getValue());
    BooleanSetting includeCrystalMapping = registerBoolean("Include Crystal Mapping", true, () -> place.getValue());
    ModeSetting limitPacketPlace = registerMode("Limit Packet Place", Arrays.asList("None", "Tick", "Time"), "None",
            () -> place.getValue());
    IntegerSetting limitTickPlace = registerInteger("Limit Tick Place", 0, 0, 20,
            () -> place.getValue() && limitPacketPlace.getValue().equals("Tick"));
    IntegerSetting limitTickTime = registerInteger("Limit Time Place", 0, 0, 2000,
            () -> place.getValue() && limitPacketPlace.getValue().equals("Time"));
    BooleanSetting swingPlace = registerBoolean("Swing Place", false, () -> place.getValue());
    //endregion

    //region Misc
    BooleanSetting misc = registerBoolean("Misc Section", false);
    ModeSetting type = registerMode("Render", Arrays.asList("Outline", "Fill", "Both"), "Both", () -> misc.getValue());

    //region outline custom
    // Custom outline
    BooleanSetting OutLineSection = registerBoolean("OutLine Section Custom", false,
            () ->  (type.getValue().equals("Outline") || type.getValue().equals("Both")) && misc.getValue());
    IntegerSetting outlineWidth = registerInteger("Outline Width", 1, 1, 5,
            () -> (type.getValue().equals("Outline") || type.getValue().equals("Both")) && misc.getValue() && OutLineSection.getValue());
    // Bottom
    ModeSetting NVerticesOutlineBot = registerMode("N^ Vertices Outline Bot", Arrays.asList("1", "2", "4"), "4",
            () -> (type.getValue().equals("Outline") || type.getValue().equals("Both")) && (OutLineSection.getValue() && misc.getValue()));
    ModeSetting direction2OutLineBot = registerMode("Direction Outline Bot", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && misc.getValue()) && NVerticesOutlineBot.getValue().equals("2"));
    ColorSetting firstVerticeOutlineBot = registerColor("1 Vert Out Bot", new GSColor(255, 16, 19, 50),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && misc.getValue())
            , true);
    ColorSetting secondVerticeOutlineBot = registerColor("2 Vert Out Bot", new GSColor(0, 0, 255, 50),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && misc.getValue())
                    && (NVerticesOutlineBot.getValue().equals("2") || NVerticesOutlineBot.getValue().equals("4")), true);
    ColorSetting thirdVerticeOutlineBot = registerColor("3 Vert Out Bot", new GSColor(0, 255, 128, 50),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && misc.getValue())
                    && NVerticesOutlineBot.getValue().equals("4"), true);
    ColorSetting fourVerticeOutlineBot = registerColor("4 Vert Out Bot", new GSColor(255, 255, 2, 50),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && misc.getValue())
                    && NVerticesOutlineBot.getValue().equals("4"), true);
    // Top
    ModeSetting NVerticesOutlineTop = registerMode("N^ Vertices Outline Top", Arrays.asList("1", "2", "4"), "4",
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && misc.getValue()));
    ModeSetting direction2OutLineTop = registerMode("Direction Outline Top", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && misc.getValue()) && NVerticesOutlineTop.getValue().equals("2"));
    ColorSetting firstVerticeOutlineTop = registerColor("1 Vert Out Top", new GSColor(255, 16, 19, 50),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && misc.getValue()), true);
    ColorSetting secondVerticeOutlineTop = registerColor("2 Vert Out Top", new GSColor(0, 0, 255, 50),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && misc.getValue())
                    && (NVerticesOutlineTop.getValue().equals("2") || NVerticesOutlineTop.getValue().equals("4")), true);
    ColorSetting thirdVerticeOutlineTop = registerColor("3 Vert Out Top", new GSColor(0, 255, 128, 50),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && misc.getValue())
                    && NVerticesOutlineTop.getValue().equals("4"), true);
    ColorSetting fourVerticeOutlineTop = registerColor("4 Vert Out Top", new GSColor(255, 255, 2, 50),
            () ->   (type.getValue().equals("Outline") || type.getValue().equals("Both")) &&
                    (OutLineSection.getValue() && misc.getValue())
                    && NVerticesOutlineTop.getValue().equals("4"), true);
    //endregion
    // region fill custom
    BooleanSetting FillSection = registerBoolean("Fill Section Custom", false,
            () ->  (type.getValue().equals("Fill") || type.getValue().equals("Both")) && misc.getValue());
    // Bottom
    ModeSetting NVerticesFillBot = registerMode("N^ Vertices Fill Bot", Arrays.asList("1", "2", "4"), "4",
            () -> (type.getValue().equals("Fill") || type.getValue().equals("Both")) && FillSection.getValue());
    ModeSetting direction2FillBot = registerMode("Direction Fill Bot", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue() && NVerticesFillBot.getValue().equals("2"));
    ColorSetting firstVerticeFillBot = registerColor("1 Vert Fill Bot", new GSColor(255, 16, 19, 50),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
            , true);
    ColorSetting secondVerticeFillBot = registerColor("2 Vert Fill Bot", new GSColor(0, 0, 255, 50),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
                    && (NVerticesFillBot.getValue().equals("2") || NVerticesFillBot.getValue().equals("4")), true);
    ColorSetting thirdVerticeFillBot = registerColor("3 Vert Fill Bot", new GSColor(0, 255, 128, 50),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
                    && NVerticesFillBot.getValue().equals("4"), true);
    ColorSetting fourVerticeFillBot = registerColor("4 Vert Fill Bot", new GSColor(255, 255, 2, 50),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
                    && NVerticesFillBot.getValue().equals("4"), true);
    // Top
    ModeSetting NVerticesFillTop = registerMode("N^ Vertices Fill Top", Arrays.asList("1", "2", "4"), "4",
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue());
    ModeSetting direction2FillTop = registerMode("Direction Fill Top", Arrays.asList("X", "Z"), "X",
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue() && NVerticesFillTop.getValue().equals("2"));
    ColorSetting firstVerticeFillTop = registerColor("1 Vert Fill Top", new GSColor(255, 16, 19, 50),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue(), true);
    ColorSetting secondVerticeFillTop = registerColor("2 Vert Fill Top", new GSColor(0, 0, 255, 50),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
                    && (NVerticesFillTop.getValue().equals("2") || NVerticesFillTop.getValue().equals("4")), true);
    ColorSetting thirdVerticeFillTop = registerColor("3 Vert Fill Top", new GSColor(0, 255, 128, 50),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
                    && NVerticesFillTop.getValue().equals("4"), true);
    ColorSetting fourVerticeFillTop = registerColor("4 Vert Fill Top", new GSColor(255, 255, 2, 50),
            () ->   (type.getValue().equals("Fill") || type.getValue().equals("Both")) &&
                    FillSection.getValue()
                    && NVerticesFillTop.getValue().equals("4"), true);
    //endregion

    BooleanSetting showText = registerBoolean("Show text", true, () -> misc.getValue());
    ColorSetting colorPlaceText = registerColor("Color Place Text", new GSColor(0, 255, 255),
            () -> misc.getValue() && showText.getValue(), true);
    BooleanSetting switchHotbar = registerBoolean("Switch Crystal", false, () -> misc.getValue());
    BooleanSetting switchBack = registerBoolean("Switch Back", false,
            () -> misc.getValue() && switchHotbar.getValue());
    IntegerSetting tickSwitchBack = registerInteger("Tick Switch Back", 5, 0, 50,
            () -> misc.getValue() && switchHotbar.getValue() && switchBack.getValue());
    BooleanSetting silentSwitch = registerBoolean("Silent Switch", false,
            () -> misc.getValue() && switchHotbar.getValue());
    //endregion

    //region Predict
    BooleanSetting predictSection = registerBoolean("Predict Section", false);
    ModeSetting predictTeleport = registerMode("Predict Teleport", Arrays.asList("Disabled", "Packet", "Sound"), "Disabled",
            () -> predictSection.getValue());
    BooleanSetting predictSelfPlace = registerBoolean("Predict Self Place", false, () -> predictSection.getValue());
    BooleanSetting showSelfPredict = registerBoolean("Show Self Predict", false,
            () -> predictSection.getValue() && predictSelfPlace.getValue() );
    ColorSetting colorSelf = registerColor("Color Self Place", new GSColor(0, 255, 255),
            () -> predictSection.getValue() && predictSelfPlace.getValue() && showSelfPredict.getValue());
    BooleanSetting predictPlaceEnemy = registerBoolean("Predict Place Enemy", false, () -> predictSection.getValue());
    ColorSetting showColorPredictEnemy = registerColor("Color Place Predict Enemy", new GSColor(255, 160, 0),
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
    //endregion

    //region Threading
    BooleanSetting threading = registerBoolean("Threading Section", false);
    IntegerSetting nThread = registerInteger("N Thread", 4, 1, 20, () -> threading.getValue());
    IntegerSetting maxTarget = registerInteger("Max Target", 5, 1, 30, () -> threading.getValue());
    IntegerSetting placeTimeout = registerInteger("Place Timeout", 100, 0, 1000, () -> threading.getValue());
    IntegerSetting predictPlaceTimeout = registerInteger("Predict Place Timeout", 100, 0, 1000, () -> threading.getValue());
    //endregion

    //region Strict
    BooleanSetting strict = registerBoolean("Strict Section", false);
    BooleanSetting raytrace = registerBoolean("Raytrace", false, () -> strict.getValue());
    BooleanSetting rotate = registerBoolean("Rotate", false, () -> strict.getValue());
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
            () -> strict.getValue());
    IntegerSetting pitchStep = registerInteger("Pitch Step", 40, 0, 180,
            () -> strict.getValue() && pitchCheck.getValue());
    BooleanSetting placeStrictPredict = registerBoolean("Place Strict Predict", false,
            () -> strict.getValue() && (pitchCheck.getValue() || yawCheck.getValue()));

    //endregion

    //region Debug
    BooleanSetting debugMenu = registerBoolean("Debug Section", false);
    BooleanSetting timeCalcPlacement = registerBoolean("Calc Placement Time", false, () -> debugMenu.getValue());
    IntegerSetting nCalc = registerInteger("N Calc", 100, 1, 1000, () -> debugMenu.getValue());
    BooleanSetting debugPredict = registerBoolean("Debug Predict", false, () -> debugMenu.getValue());
    BooleanSetting showPredictions = registerBoolean("Show Predictions", false, () -> debugMenu.getValue() && debugPredict.getValue());
    //endregion
    //endregion

    //region Global variables

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

        // Draw box (hitbox)
        public display(AxisAlignedBB box, GSColor color, int width) {
            this.box = box;
            this.color = color;
            this.width = width;
            this.type = 0;
        }

        // Draw text
        public display(String text, BlockPos block, GSColor color) {
            this.text = new String[]{text};
            this.block = block;
            this.color = color;
            this.type = 1;
        }

        // Function for drawing
        void draw() {
            switch (type) {
                case 0:
                    RenderUtil.drawBoundingBox(box, width, color);
                    break;
                case 1:
                    RenderUtil.drawNametag((double) this.block.getX() + 0.5d, (double) this.block.getY() + 0.5d, (double) this.block.getZ() + 0.5d, this.text, this.color, 1);
                    break;
            }
        }
    }

    class crystalPlaceWait {

        // Here we have every crystals
        ArrayList<crystalTime> listWait = new ArrayList<>();

        // Add new crystal with time delay
        void addCrystal(BlockPos cryst, int finish) {
            listWait.add(new crystalTime(cryst,  finish));
        }

        // Add new crystal with tickd elay
        void addCrystal(BlockPos cryst, int tick, int tickFinish) {
            listWait.add(new crystalTime(cryst,  tick, tickFinish));
        }

        // If exists, remove crystal at x y z
        void removeCrystal(Double x, Double y, Double z) {
            int i = CrystalExists(new BlockPos(x - .5, y - .5, z - .5));
            if (i != -1)
                listWait.remove(i);
        }

        // Return the index of the crystal in the array. -1 if it doesnt exists
        int CrystalExists(BlockPos pos) {
            for(int i = 0; i < listWait.size(); i++)
                if (sameBlockPos(pos, listWait.get(i).posCrystal))
                    return i;
            return -1;
        }

        // Update every crystals timers
        void updateCrystals() {
            for(int i = 0; i < listWait.size(); i++) {
                if (listWait.get(i).isReady()) {
                    listWait.remove(i);
                    i--;
                }
            }
        }

    }

    // Class of the crystal time
    static class crystalTime {
        final BlockPos posCrystal;
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

    /// Global variables sorted by type
    public static boolean stopAC = false;

    boolean checkTime, placedCrystal = false, isRotating;

    int oldSlot, tick = 0, tickBeforePlace = 0, slotChange, tickSwitch, oldSlotBack;

    double xPlayer, yPlayer;


    long time = 0;

    Vec3d lastHitVec;

    crystalPlaceWait listCrystalsPlaced = new crystalPlaceWait();
    crystalTime crystalPlace = null;


    ArrayList<display> toDisplay = new ArrayList<>();
    ArrayList<Long> durations = new ArrayList<>();

    ThreadPoolExecutor executor =
            (ThreadPoolExecutor) Executors.newCachedThreadPool();

    CrystalInfo.PlaceInfo bestPlace = new CrystalInfo.PlaceInfo(-100, null, null, 100d);

    //endregion

    //region Gamesense call

    public void onEnable() {
        // Just reset some variables
        tickBeforePlace = tick = 0;
        time = 0;
        oldSlotBack = tickSwitch = -1;
        checkTime = isRotating = false;
    }

    int tickEat = 0;

    // Simple onUpdate
    public void onUpdate() {
        if (mc.world == null || mc.player == null || mc.player.isDead || stopAC) return;

        // Clear what we are displaying
        toDisplay.clear();

        // If we are eating, stop
        if (stopGapple.getValue()) {
            Item item;
            if (
                mc.player.isHandActive() && (
                    (item = mc.player.getHeldItemMainhand().getItem()) == Items.GOLDEN_APPLE || item == Items.CHORUS_FRUIT
                    || (item = mc.player.getHeldItemOffhand().getItem()) == Items.GOLDEN_APPLE || item == Items.CHORUS_FRUIT)) {
                tickEat = tickWaitEat.getValue();
                return;
            }
            if (tickEat > 0) {
                tickEat--;
                return;
            }
        }

        // Start ca
        switch (logic.getValue()) {
            case "Place->Break":
                placeCrystals();
                breakCrystals();
                break;
            case "Break->Place":
                breakCrystals();
                placeCrystals();
                break;
            case "Place":
                placeCrystals();
                break;
            case "Break":
                breakCrystals();
                break;
        }

        // Remember this slot. This is used for preventing the bug with normal switch
        oldSlot = mc.player.inventory.currentItem;

    }

    // Display in the hud
    public String getHudInfo() {
        String t = "";

        return t;
    }

    //endregion

    //region Calculate Place Crystal

    // Main function for calculating the best crystal
    void getTargetPlacing(String mode) {
        PredictUtil.PredictSettings settings = new PredictUtil.PredictSettings(tickPredict.getValue(), calculateYPredict.getValue(), startDecrease.getValue(), exponentStartDecrease.getValue(), decreaseY.getValue(), exponentDecreaseY.getValue(), increaseY.getValue(), exponentIncreaseY.getValue(), splitXZ.getValue(), widthPredict.getValue(), debugPredict.getValue(), showPredictions.getValue(), manualOutHole.getValue(), aboveHoleManual.getValue());
        int nThread = this.nThread.getValue();
        float armourPercent = armourFacePlace.getValue() / 100.0f;
        double minDamage = this.minDamagePlace.getValue();
        double minFacePlaceHp = this.facePlaceValue.getValue();
        double minFacePlaceDamage = this.minFacePlaceDmg.getValue();
        double enemyRangeSQ = rangeEnemyPlace.getValue() * rangeEnemyPlace.getValue();
        double maxSelfDamage = this.maxSelfDamagePlace.getValue();
        double wallRangePlaceSQ = this.crystalWallPlace.getValue() * this.crystalWallPlace.getValue();
        boolean raytraceValue = raytrace.getValue();
        int maxYTarget = this.maxYTargetPlace.getValue();
        int minYTarget = this.minYTargetPlace.getValue();
        int placeTimeout = this.placeTimeout.getValue();
        // Prepare for after
        PlayerInfo player;
        List<List<PositionInfo>> possibleCrystals;
        PlayerInfo target;
        // Our result
        bestPlace = new CrystalInfo.PlaceInfo(-100, null, null, 100d);
        switch (mode) {
            // Lowest and Nearest use the same code with just 1 difference.
            case "Lowest":
            case "Nearest":
                // Get the target
                EntityPlayer targetEP =
                        mode.equals("Lowest")
                        // Lowest
                        ? getBasicPlayers(enemyRangeSQ).min((x, y) -> (int) x.getHealth()).orElse(null)
                        // Nearest
                        : getBasicPlayers(enemyRangeSQ).min(Comparator.comparingDouble(x -> x.getDistanceSq(mc.player))).orElse(null);

                // If nobody found, return
                if (targetEP == null)
                    return;

                player = new PlayerInfo( predictSelfPlace.getValue() ? PredictUtil.predictPlayer(mc.player, settings) : mc.player, false);

                // Show self predict
                if (predictSelfPlace.getValue() && showSelfPredict.getValue())
                    toDisplay.add(new display(player.entity.getEntityBoundingBox(), colorSelf.getColor(), widthPredict.getValue()));

                // Get every possible crystals
                possibleCrystals = getPossibleCrystals(player, maxSelfDamage, raytraceValue, wallRangePlaceSQ);

                // If nothing is possible
                if (possibleCrystals == null)
                    return;

                // Get target info
                target = new PlayerInfo(targetEP, armourPercent);

                // Calcualte best cr
                calcualteBest(nThread, possibleCrystals, player.entity.posX, player.entity.posY, player.entity.posZ, target,
                        minDamage, minFacePlaceHp, minFacePlaceDamage, maxSelfDamage, maxYTarget, minYTarget, placeTimeout);

                return;
            case "Damage":
                // Get every possible players
                List<EntityPlayer> players = getBasicPlayers(enemyRangeSQ).sorted(new Sortbyroll()).collect(Collectors.toList());
                if (players.size() == 0)
                    return;
                // If predict
                if (predictPlaceEnemy.getValue()) {
                    // Split list of entity
                    List<List<EntityPlayer>> list = splitListEntity(players, nThread);

                    // Clear players, we are going to replace it with the prediciton
                    players.clear();
                    // Output
                    Collection<Future<?>> futures = new LinkedList<>();

                    int predictPlaceTimeout = this.predictPlaceTimeout.getValue();
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
                            temp = (List<EntityPlayer>) future.get(predictPlaceTimeout, TimeUnit.MILLISECONDS);
                            // If not null, add
                            if (temp != null)
                                players.addAll(temp);
                        } catch (ExecutionException | InterruptedException | TimeoutException e) {
                            e.printStackTrace();
                        }
                    }

                }


                player = new PlayerInfo( predictSelfPlace.getValue() ? PredictUtil.predictPlayer(mc.player, settings) : mc.player, false);

                if (predictSelfPlace.getValue() && showSelfPredict.getValue())
                    toDisplay.add(new display(player.entity.getEntityBoundingBox(), colorSelf.getColor(), widthPredict.getValue()));

                // If we are placing
                possibleCrystals = getPossibleCrystals(player, maxSelfDamage, raytraceValue, wallRangePlaceSQ);

                // If nothing is possible
                if (possibleCrystals == null)
                    return;

                // For every players
                int count = 0;

                // Iterate for every players
                for (EntityPlayer playerTemp : players) {
                    // If we reached max
                    if (count++ >= maxTarget.getValue())
                        break;

                    // Get target
                    target = new PlayerInfo(playerTemp, armourPercent);
                    // Calculate
                    calcualteBest(nThread, possibleCrystals, player.entity.posX, player.entity.posY, player.entity.posZ, target,
                            minDamage, minFacePlaceHp, minFacePlaceDamage, maxSelfDamage, maxYTarget, minYTarget, placeTimeout);
                }
        }
    }

    // Function that call every thread for the calculating of the crystals
    // + return the best place
    void calcualteBest(int nThread, List<List<PositionInfo>> possibleCrystals, double posX, double posY, double posZ,
                       PlayerInfo target, double minDamage, double minFacePlaceHp, double minFacePlaceDamage, double maxSelfDamage,
                       int maxYTarget, int minYTarget, int placeTimeout) {
        // For getting output of threading
        Collection<Future<?>> futures = new LinkedList<>();
        // Iterate for every thread we have
        for (int i = 0; i < nThread; i++) {
            int finalI = i;
            // Add them
            futures.add(executor.submit(() -> calculateBestPositionTarget(possibleCrystals.get(finalI), posX, posY, posZ,
                    target, minDamage, minFacePlaceHp, minFacePlaceDamage, maxSelfDamage, maxYTarget, minYTarget)));
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
        results.add(bestPlace);
        if (results.size() != 1)
            bestPlace = getResult(results);
    }

    // This return the best crystal
    CrystalInfo.PlaceInfo getResult(Stack<CrystalInfo.PlaceInfo> result) {
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
    List<List<PositionInfo>> getPossibleCrystals(PlayerInfo self, double maxSelfDamage, boolean raytrace, double wallRangeSQ) {
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
                    float damage = DamageUtil.calculateDamageThreaded(crystal.getX() + .5D, crystal.getY() + 1D, crystal.getZ() + .5D, self);
                    // If we can take that damage
                    if (damage < maxSelfDamage && (!antiSuicide.getValue() || damage < self.health)) {
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
        // If we have only 1  thread, return only 1 thing
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
    CrystalInfo.PlaceInfo calculateBestPositionTarget(List<PositionInfo> possibleLocations, double x, double y, double z, PlayerInfo target,
                                                      double minDamage, double minFacePlaceHealth, double minFacePlaceDamage, double maxSelfDamage,
                                                      int maxYTarget, int minYTarget) {
        // Start calculating damage
        PositionInfo best = new PositionInfo();
        for (PositionInfo crystal : possibleLocations) {

            // Calculate Y
            double temp;
            //noinspection ConstantConditions
            if ((temp = target.entity.posY - crystal.pos.getY() - 1) > 0 ? temp > minYTarget : temp < -maxYTarget)
                continue;

            // if player is out of range of this crystal, do nothing
            float currentDamage = DamageUtil.calculateDamageThreaded((double) crystal.pos.getX() + 0.5d, (double) crystal.pos.getY() + 1.0d, (double) crystal.pos.getZ() + 0.5d, target);
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
                if (!checkTime)
                    return true;
                else if (System.currentTimeMillis() - time >= timeDelayPlace.getValue()) {
                    checkTime = false;
                    return true;
                }
                break;
        }
        // If we are not ready
        return false;
    }

    // Main function for placing crystals
    void placeCrystals() {

        // Update every crystals timers
        listCrystalsPlaced.updateCrystals();

        // If we have placed a crystal before
        if (placedCrystal) {
            // Stop
            placedCrystal = false;
            return;
        }

        // If we cannot place (place delay)
        if (!canStartPlacing())
            return;

        // Get crystal hand
        EnumHand hand = getHandCrystal();
        // If no hand found
        if (hand == null)
            return;

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
            if (crystalPlace != null) {
                // Check if the crystal is ready, if yes, null
                if (crystalPlace.isReady())
                    crystalPlace = null;
                else {
                    // Else, place it
                    placeCrystal(crystalPlace.posCrystal, hand);
                    return;
                }
            }
        }

        // For debugging timeCalcPlacement
        long inizio = 0;
        if (timeCalcPlacement.getValue())
            // Get time
            inizio = System.currentTimeMillis();
        // Get target
        getTargetPlacing(targetPlacing.getValue());
        // For debugging timeCalcPlacemetn
        if (timeCalcPlacement.getValue()) {
            // Get duration
            long fine = System.currentTimeMillis();
            durations.add(fine - inizio);
            // If we reached last
            if (durations.size() > nCalc.getValue()) {
                double sum = durations.stream()
                        .mapToDouble(a -> a)
                        .sum();
                sum /= nCalc.getValue();
                durations.clear();
                PistonCrystal.printDebug(String.format("N: %d Value: %f", nCalc.getValue(), sum), false);
            }
        }

        // Display crystal
        if (bestPlace.crystal != null) {
            //toDisplay.add(new display(bestPlace.crystal, new GSColor(colorPlace.getValue(), colorPlace.getValue().getAlpha())));
            toDisplay.add(new display(String.valueOf((int) bestPlace.damage), bestPlace.crystal, colorPlaceText.getValue()));
            if (predictPlaceEnemy.getValue())
                toDisplay.add(new display(bestPlace.getTarget().getEntityBoundingBox(), showColorPredictEnemy.getColor(), outlineWidth.getValue()));
            placeCrystal(bestPlace.crystal, hand);
        } else {
            if (switchBack.getValue() && oldSlotBack != -1)
                if (tickSwitch > 0)
                    --tickSwitch;
                else
                    if (tickSwitch == 0) {
                        mc.player.inventory.currentItem = oldSlotBack;
                        tickSwitch = -1;
                    }
        }
    }

    EntityPlayer isCrystalGood(BlockPos crystal) {

        // Check for the damafge
        float damage;
        if ( (damage = DamageUtil.calculateDamage(crystal.getX() + .5D, crystal.getY() + 1D, crystal.getZ() + .5D, mc.player))
                >= maxSelfDamagePlace.getValue() && (!antiSuicide.getValue() || damage < PlayerUtil.getHealth()))
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
                .filter(entity -> DamageUtil.calculateDamage(crystal.getX() + .5D, crystal.getY() + 1D, crystal.getZ() + .5D, entity) >= minDamagePlace.getValue())
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
                    slotChange = slot;
                    return EnumHand.MAIN_HAND;
                }
            }
        }
        return null;
    }

    // This actually place the crystal
    void placeCrystal(BlockPos pos, EnumHand handSwing) {
        // If there is a crystal, stop
        if (!isCrystalHere(pos))
            return;

        // If this pos is in wait
        if (listCrystalsPlaced.CrystalExists(pos) != -1)
            return;

        // Rotate
        if (rotate.getValue()) {
            // New lastHitVec
            lastHitVec = new Vec3d(pos).add(0.5, 1, 0.5);
            // New tick
            tick = 0;
            if (yawCheck.getValue() || pitchCheck.getValue()) {
                Vec2f rotationWanted = RotationUtil.getRotationTo(lastHitVec);
                if (!isRotating) {
                    yPlayer = pitchCheck.getValue()
                            ? mc.player.getPitchYaw().x
                            : Double.MIN_VALUE;
                    xPlayer = yawCheck.getValue()
                            ? RotationUtil.normalizeAngle(mc.player.getPitchYaw().y)
                            : Double.MIN_VALUE;
                    isRotating = true;
                }

                if (placeStrictPredict.getValue()) {

                    if (yawCheck.getValue()) {
                        // Get first if + or -
                        double distanceDo = rotationWanted.x - xPlayer;
                        if (Math.abs(distanceDo) > 180) {
                            distanceDo = RotationUtil.normalizeAngle(distanceDo);
                        }
                        int direction = distanceDo > 0 ? 1 : -1;
                        // Check if distance is > of what we want

                        if (Math.abs(distanceDo) > yawStep.getValue()) {
                            return;
                        }
                    }

                    if (pitchCheck.getValue()) {
                        // Get first if + or -
                        double distanceDo = rotationWanted.y - yPlayer;
                        int direction = distanceDo > 0 ? 1 : -1;
                        // Check if distance is > of what we want

                        if (Math.abs(distanceDo) > pitchStep.getValue()) {
                            return;
                        }
                    }

                } else if (!(xPlayer == rotationWanted.x && yPlayer == rotationWanted.y))
                    return;
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
                            oldSlotBack = mc.player.inventory.currentItem;
                        }
                        mc.player.inventory.currentItem = slotChange;
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                    }

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
                return;
            } else {
                // Else, enumFacing is the side we hit
                enumFacing = result.sideHit;
            }

            // Place
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, enumFacing, handSwing, 0, 0, 0));
        } else {
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
        if (swingPlace.getValue())
            mc.player.swingArm(handSwing);

        // For silent switch
        if (slotChange != -1) {
            if (silentSwitch.getValue())
                mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
        }

        // For limiting place packets
        tickBeforePlace = tickDelayPlace.getValue();
        checkTime = true;
        time = System.currentTimeMillis();
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

    }

    // Given a pos, say if there is a crystal
    boolean isCrystalHere(BlockPos pos) {
        // Get position
        BlockPos posUp = pos.up();

        // Get box
        AxisAlignedBB box = new AxisAlignedBB(
                posUp.getX(), posUp.getY(), posUp.getZ(),
                posUp.getX() + 1.0, posUp.getY() + 2.0, posUp.getZ() + 1.0
        );

        // Check for entity
        return mc.world.getEntitiesWithinAABB(Entity.class, box, entity -> entity instanceof EntityEnderCrystal).isEmpty();
    }

    //endregion

    //region Calculate Break Crystal

    void breakCrystals() {

    }

    //endregion

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

    // This function is used for getting a basic list of possible players
    Stream<EntityPlayer> getBasicPlayers(double rangeEnemySQ) {
        return mc.world.playerEntities.stream()
                .filter(entity -> entity.getDistanceSq(mc.player) <= rangeEnemySQ)
                .filter(entity -> !EntityUtil.basicChecksEntity(entity))
                .filter(entity -> entity.getHealth() > 0.0f);
    }

    // Say if two blockPos are the same
    boolean sameBlockPos(BlockPos first, BlockPos second) {
        return first.getX() == second.getX() && first.getY() == second.getY() && first.getZ() == second.getZ();
    }

    // This is used for the rendering for choosing if it's 1 vertices or multiple
    boolean isOne(boolean outline) {
        return outline ?
            NVerticesOutlineBot.getValue().equals("1") && NVerticesOutlineTop.getValue().equals("1")
                 :
            NVerticesFillBot.getValue().equals("1") && NVerticesFillTop.getValue().equals("1");
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

    // This function is for displaying things
    public void onWorldRender(RenderEvent event) {

        // If we have a bestPlace
        if (bestPlace != null && bestPlace.crystal != null) {
            // Switch for types
            switch (type.getValue()) {
                case "Outline": {
                    // If 1 vertice
                    if (isOne(true))
                        // Old rendering
                        RenderUtil.drawBoundingBox(getBox(bestPlace.crystal), widthPredict.getValue(), firstVerticeOutlineBot.getColor(), firstVerticeOutlineBot.getColor().getAlpha());
                    // Else, new rendering
                    else renderCustomOutline(getBox(bestPlace.crystal));
                    break;
                }
                case "Fill": {
                    // If 1 vertice
                    if (isOne(false))
                        // Old rendering
                        RenderUtil.drawBox(getBox(bestPlace.crystal), true, 1, firstVerticeFillBot.getColor(), firstVerticeFillBot.getValue().getAlpha(), GeometryMasks.Quad.ALL);
                    // Else, new rendering
                    else renderFillCustom(getBox(bestPlace.crystal));
                    break;
                }
                case "Both": {
                    // If 1 vertice
                    if (isOne(false))
                        // Old rendering
                        RenderUtil.drawBox(getBox(bestPlace.crystal), true, 1, firstVerticeFillBot.getColor(), firstVerticeFillBot.getValue().getAlpha(), GeometryMasks.Quad.ALL);
                    // Else, new
                    else renderFillCustom(getBox(bestPlace.crystal));
                    // If 1 vertice
                    if (isOne(true))
                        // Old rendering
                        RenderUtil.drawBoundingBox(getBox(bestPlace.crystal), widthPredict.getValue(), firstVerticeOutlineBot.getColor(), firstVerticeOutlineBot.getColor().getAlpha());
                    // Else, new
                    else renderCustomOutline(getBox(bestPlace.crystal));
                    break;
                }
            }
        }

        // Display everything else
        toDisplay.forEach(display::draw);
    }

    // This is used for creating the box gradient
    private void renderCustomOutline(AxisAlignedBB hole) {

        ArrayList<GSColor> colors = new ArrayList<>();

        switch (NVerticesOutlineBot.getValue()) {
            case "1":
                colors.add(firstVerticeOutlineBot.getValue());
                colors.add(firstVerticeOutlineBot.getValue());
                colors.add(firstVerticeOutlineBot.getValue());
                colors.add(firstVerticeOutlineBot.getValue());
                break;
            case "2":
                if (direction2OutLineBot.getValue().equals("X")) {
                    colors.add(firstVerticeOutlineBot.getValue());
                    colors.add(secondVerticeOutlineBot.getValue());
                    colors.add(firstVerticeOutlineBot.getValue());
                    colors.add(secondVerticeOutlineBot.getValue());
                } else {
                    colors.add(firstVerticeOutlineBot.getValue());
                    colors.add(firstVerticeOutlineBot.getValue());
                    colors.add(secondVerticeOutlineBot.getValue());
                    colors.add(secondVerticeOutlineBot.getValue());
                }
                break;
            case "4":
                colors.add(firstVerticeOutlineBot.getValue());
                colors.add(secondVerticeOutlineBot.getValue());
                colors.add(thirdVerticeOutlineBot.getValue());
                colors.add(fourVerticeOutlineBot.getValue());
                break;
        }
        switch (NVerticesOutlineTop.getValue()) {
            case "1":
                colors.add(firstVerticeOutlineTop.getValue());
                colors.add(firstVerticeOutlineTop.getValue());
                colors.add(firstVerticeOutlineTop.getValue());
                colors.add(firstVerticeOutlineTop.getValue());
                break;
            case "2":
                if (direction2OutLineTop.getValue().equals("X")) {
                    colors.add(firstVerticeOutlineTop.getValue());
                    colors.add(secondVerticeOutlineTop.getValue());
                    colors.add(firstVerticeOutlineTop.getValue());
                    colors.add(secondVerticeOutlineTop.getValue());
                } else {
                    colors.add(firstVerticeOutlineTop.getValue());
                    colors.add(firstVerticeOutlineTop.getValue());
                    colors.add(secondVerticeOutlineTop.getValue());
                    colors.add(secondVerticeOutlineTop.getValue());
                }
                break;
            case "4":
                colors.add(firstVerticeOutlineTop.getValue());
                colors.add(secondVerticeOutlineTop.getValue());
                colors.add(thirdVerticeOutlineTop.getValue());
                colors.add(fourVerticeOutlineTop.getValue());
                break;
        }

        RenderUtil.drawBoundingBox(hole, widthPredict.getValue(), colors.toArray(new GSColor[7]));
    }

    // This is used for the filling the box gradient
    void renderFillCustom(AxisAlignedBB hole) {

        int mask = GeometryMasks.Quad.ALL;


        ArrayList<GSColor> colors = new ArrayList<>();
        switch (NVerticesFillBot.getValue()) {
            case "1":
                colors.add(firstVerticeFillBot.getValue());
                colors.add(firstVerticeFillBot.getValue());
                colors.add(firstVerticeFillBot.getValue());
                colors.add(firstVerticeFillBot.getValue());
                break;
            case "2":
                if (direction2FillBot.getValue().equals("X")) {
                    colors.add(firstVerticeFillBot.getValue());
                    colors.add(secondVerticeFillBot.getValue());
                    colors.add(firstVerticeFillBot.getValue());
                    colors.add(secondVerticeFillBot.getValue());
                } else {
                    colors.add(firstVerticeFillBot.getValue());
                    colors.add(firstVerticeFillBot.getValue());
                    colors.add(secondVerticeFillBot.getValue());
                    colors.add(secondVerticeFillBot.getValue());
                }
                break;
            case "4":
                colors.add(firstVerticeFillBot.getValue());
                colors.add(secondVerticeFillBot.getValue());
                colors.add(thirdVerticeFillBot.getValue());
                colors.add(fourVerticeFillBot.getValue());
                break;
        }
        switch (NVerticesFillTop.getValue()) {
            case "1":
                colors.add(firstVerticeFillTop.getValue());
                colors.add(firstVerticeFillTop.getValue());
                colors.add(firstVerticeFillTop.getValue());
                colors.add(firstVerticeFillTop.getValue());
                break;
            case "2":
                if (direction2FillTop.getValue().equals("X")) {
                    colors.add(firstVerticeFillTop.getValue());
                    colors.add(secondVerticeFillTop.getValue());
                    colors.add(firstVerticeFillTop.getValue());
                    colors.add(secondVerticeFillTop.getValue());
                } else {
                    colors.add(firstVerticeFillTop.getValue());
                    colors.add(firstVerticeFillTop.getValue());
                    colors.add(secondVerticeFillTop.getValue());
                    colors.add(secondVerticeFillTop.getValue());
                }
                break;
            case "4":
                colors.add(firstVerticeFillTop.getValue());
                colors.add(secondVerticeFillTop.getValue());
                colors.add(thirdVerticeFillTop.getValue());
                colors.add(fourVerticeFillTop.getValue());
                break;
        }

        RenderUtil.drawBoxProva2(hole, true, 1, colors.toArray(new GSColor[7]), mask, true);
    }

    //endregion

    //region Packet management

    // This function is used for the rotation
    double xPlayerRotation, yPlayerRotation;
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {
        // If we dont have to rotate
        if (event.getPhase() != Phase.PRE || !rotate.getValue() || lastHitVec == null) return;

        // If we reached the last point (Delay)
        if (tick++ > tickAfterRotation.getValue()) {
            lastHitVec = null;
            tick = 0;
            isRotating = false;
        } else {
            // If we have to rotate
            Vec2f rotationWanted = RotationUtil.getRotationTo(lastHitVec);
            Vec2f nowRotation;

            if (yawCheck.getValue() || pitchCheck.getValue()) {

                if (yPlayer == Double.MIN_VALUE)
                    yPlayer = rotationWanted.y;
                else {
                    // Get first if + or -
                    double distanceDo = rotationWanted.y - yPlayer;
                    int direction = distanceDo > 0 ? 1 : -1;
                    // Check if distance is > of what we want

                    if (Math.abs(distanceDo) > pitchStep.getValue()) {
                        yPlayer = RotationUtil.normalizeAngle(yPlayer + pitchStep.getValue() * direction);
                    } else {
                        yPlayer = rotationWanted.y;
                    }
                }
                if (xPlayer == Double.MIN_VALUE)
                    xPlayer = rotationWanted.x;
                else {
                    // Get first if + or -
                    double distanceDo = rotationWanted.x - xPlayer;
                    if (Math.abs(distanceDo) > 180) {
                        distanceDo = RotationUtil.normalizeAngle(distanceDo);
                    }
                    int direction = distanceDo > 0 ? 1 : -1;
                    // Check if distance is > of what we want

                    if (Math.abs(distanceDo) > yawStep.getValue()) {
                        xPlayer = RotationUtil.normalizeAngle(xPlayer + yawStep.getValue() * direction);
                    } else {
                        xPlayer = rotationWanted.x;
                    }
                }
                nowRotation = new Vec2f((float) xPlayer, (float) yPlayer);
            } else {
                nowRotation = rotationWanted;
            }

            PlayerPacket packet = new PlayerPacket(this, nowRotation);
            PlayerPacketManager.INSTANCE.addPacket(packet);
            /*
            PistonCrystal.printDebug(String.format("Yaw go: %f Yaw player: %f",
                    rotation.x, RotationUtil.normalizeAngle(mc.player.getPitchYaw().y)), false);*/

            /*
            Nel rotation, yaw-pitch
            Nel pitchYaw, pitch-yaw
            Sono invertiti
            mc.player.setPositionAndRotation(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch)
             */
        }
    });

    // This is used for packet recive thing
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Receive> packetReceiveListener = new Listener<>(event -> {

        // Spawn object
        if (event.getPacket() instanceof SPacketSpawnObject) {
            // Get it
            SPacketSpawnObject SpawnObject = (SPacketSpawnObject)event.getPacket();
            // Idk why 51
            if (SpawnObject.getType() == 51 ) {
                // If limitPacketPlace, remove the crystal
                if (!limitPacketPlace.getValue().equals("None"))
                    listCrystalsPlaced.removeCrystal(SpawnObject.getX(), SpawnObject.getY(), SpawnObject.getZ());
                // If crystalPlace is not null
                if (crystalPlace != null)
                    // Check if it's it
                    if (sameBlockPos(new BlockPos(SpawnObject.getX() - .5, SpawnObject.getY() - .5, SpawnObject.getY() - .5), crystalPlace.posCrystal)) {
                        // If yes, remove it
                        crystalPlace = null;
                    }
            }
        }

        /// W+3 Moment
        // Teleportation predict
        if (event.getPacket() instanceof SPacketEntityTeleport) {
            SPacketEntityTeleport p = (SPacketEntityTeleport) event.getPacket();
            Entity e = mc.world.getEntityByID(p.getEntityId());
            if (e instanceof EntityPlayer && predictTeleport.getValue().equals("Packet")) {
                e.setEntityBoundingBox(e.getEntityBoundingBox().offset(p.getX(), p.getY(), p.getZ()));
            }
        }

        // Chorus predict
        if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect p = (SPacketSoundEffect) event.getPacket();
            if (p.getSound() == SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT && predictTeleport.getValue().equals("Sound")) {
                SPacketSoundEffect pa = (SPacketSoundEffect) event.getPacket();
                mc.world.loadedEntityList.spliterator().forEachRemaining(player -> {
                    if (player instanceof EntityPlayer) {
                        if (player.getDistance(pa.getX(), pa.getY(), pa.getZ()) <= rangeEnemyPlace.getValue()) {
                            player.setEntityBoundingBox(player.getEntityBoundingBox().offset(pa.getX(), pa.getY(), pa.getZ()));
                        }
                    }
                });
            }
        }


    });

    //endregion
}