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
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.api.util.world.combat.CrystalUtil;
import com.gamesense.api.util.world.combat.DamageUtil;
import com.gamesense.api.util.world.combat.ac.CrystalInfo;
import com.gamesense.api.util.world.combat.ac.PlayerInfo;
import com.gamesense.api.util.world.combat.ac.PositionInfo;
import com.gamesense.client.manager.managers.PlayerPacketManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.mojang.authlib.GameProfile;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
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
    BooleanSetting newPlace = registerBoolean("1.13 mode", false, () -> logicTarget.getValue());
    BooleanSetting ranges = registerBoolean("Range Section", false);
    //endregion

    //region Ranges
    DoubleSetting rangeEnemy = registerDouble("RangeEnemy", 7, 0, 12, () -> ranges.getValue());
    DoubleSetting placeRange = registerDouble("Place Range", 6, 0, 8, () -> ranges.getValue());
    DoubleSetting crystalRangeEnemy = registerDouble("Crytal Range Enemey", 6, 0, 8, () -> ranges.getValue());
    IntegerSetting maxYTarget = registerInteger("Max Y Target", 1, 0, 3, () -> ranges.getValue());
    IntegerSetting minYTarget = registerInteger("Min Y Target", 3, 0, 5, () -> ranges.getValue());
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
    ColorSetting colorPlace = registerColor("Color Place", new GSColor(255, 255, 255), () -> misc.getValue());
    IntegerSetting alphaPlace = registerInteger("Alpha place", 55, 0, 255, () -> misc.getValue());
    BooleanSetting switchHotbar = registerBoolean("Switch Crystal", false, () -> misc.getValue());
    BooleanSetting silentSwitch = registerBoolean("Silent Switch", false,
            () -> misc.getValue() && switchHotbar.getValue());
    //endregion

    //region Predict
    BooleanSetting predictSection = registerBoolean("Predict Section", false);
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
    IntegerSetting expnentStartDecrease = registerInteger("Exponent Start", 2, 1, 5,
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
    IntegerSetting width = registerInteger("Line Width", 2, 1, 5, () -> predictSection.getValue());
    BooleanSetting justOnce = registerBoolean("Just Once", false, () -> predictSection.getValue());
    BooleanSetting manualOutHole = registerBoolean("Manual Out Hole", false, () -> predictSection.getValue());
    BooleanSetting aboveHoleManual = registerBoolean("Above Hole Manual", false,
            () -> predictSection.getValue() && manualOutHole.getValue() && manualOutHole.getValue());
    //endregion

    //region Threading
    BooleanSetting threading = registerBoolean("Threading Section", false);
    IntegerSetting nThread = registerInteger("N Thread", 4, 1, 20, () -> threading.getValue());
    IntegerSetting maxTarget = registerInteger("Max Target", 5, 1, 30, () -> threading.getValue());
    //endregion

    //region Strict
    BooleanSetting strict = registerBoolean("Strict Section", false);
    BooleanSetting raytrace = registerBoolean("Raytrace", false, () -> strict.getValue());
    BooleanSetting rotate = registerBoolean("Rotate", false, () -> strict.getValue());
    IntegerSetting tickForceRotation = registerInteger("Tick Force Rotation", 3, 0, 10,
            () -> strict.getValue() && rotate.getValue());
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

        public display(AxisAlignedBB box, GSColor color, int width) {
            this.box = box;
            this.color = color;
            this.width = width;
            this.type = 0;
        }

        public display(BlockPos box, GSColor color) {
            block = box;
            this.color = color;
            this.type = 1;
        }

        void draw() {
            switch (type) {
                case 0:
                    RenderUtil.drawBoundingBox(box, width, color);
                    break;
                case 1:
                    RenderUtil.drawBox(block, 1, color, 63);
            }
        }
    }

    class crystalPlaceWait {

        ArrayList<crystalTime> listWait = new ArrayList<>();

        void addCrystal(BlockPos cryst, int finish) {
            listWait.add(new crystalTime(cryst,  finish));
        }

        void addCrystal(BlockPos cryst, int tick, int tickFinish) {
            listWait.add(new crystalTime(cryst,  tick, tickFinish));
        }

        void removeCrystal(Double x, Double y, Double z) {
            int i = CrystalExists(new BlockPos(x - .5, y - .5, z - .5));
            if (i != -1)
                listWait.remove(i);
        }

        int CrystalExists(BlockPos pos) {
            for(int i = 0; i < listWait.size(); i++)
                if (sameBlockPos(pos, listWait.get(i).posCrystal))
                    return i;
            return -1;
        }

        void updateCrystals() {
            for(int i = 0; i < listWait.size(); i++) {
                if (listWait.get(i).isReady()) {
                    listWait.remove(i);
                    i--;
                }
            }
        }

    }

    static class crystalTime {
        final BlockPos posCrystal;
        final int type;
        int tick;
        int finishTick;
        long start;
        int finish;

        public crystalTime(BlockPos posCrystal, int tick, int finishTick) {
            this.posCrystal = posCrystal;
            this.tick = tick;
            this.type = 0;
            this.finishTick = finishTick;
        }

        public crystalTime(BlockPos posCrystal, int finish) {
            this.posCrystal = posCrystal;
            this.start = System.currentTimeMillis();
            this.finish = finish;
            this.type = 1;
        }

        boolean isReady() {
            switch (type) {
                case 0:
                    return ++tick >= this.finishTick;
                case 1:
                    return System.currentTimeMillis() - this.start >= this.finish;
            }
            return true;
        }


    }

    public static boolean stopAC = false;
    boolean isSilentSwitching, checkTime;

    int oldSlot, tick = 0, tickBeforePlace = 0;

    long time = 0;

    Vec3d lastHitVec;

    crystalPlaceWait listCrystalsPlaced = new crystalPlaceWait();


    ArrayList<display> toDisplay = new ArrayList<>();

    ArrayList<Long> durations = new ArrayList<>();



    ThreadPoolExecutor executor =
            (ThreadPoolExecutor) Executors.newCachedThreadPool();

    CrystalInfo.PlaceInfo bestPlace = new CrystalInfo.PlaceInfo(-100, null, null, 100d);

    //endregion

    //region Gamesense call

    public void onEnable() {
        tickBeforePlace = tick = 0;
        time = 0;
        checkTime = false;
    }

    // Simple onUpdate
    public void onUpdate() {
        if (mc.world == null || mc.player == null || mc.player.isDead || stopAC) return;

        toDisplay.clear();

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
    void getTarget(String mode, boolean placing) {
        int nThread = this.nThread.getValue();
        float armourPercent = armourFacePlace.getValue() / 100.0f;
        double minDamage = this.minDamagePlace.getValue();
        double minFacePlaceHp = this.facePlaceValue.getValue();
        double minFacePlaceDamage = this.minFacePlaceDmg.getValue();
        double enemyRangeCrystalSQ = crystalRangeEnemy.getValue() * crystalRangeEnemy.getValue();
        double enemyRangeSQ = rangeEnemy.getValue() * rangeEnemy.getValue();
        double maxSelfDamage = this.maxSelfDamagePlace.getValue();
        boolean raytraceValue = raytrace.getValue();
        int maxYTarget = this.maxYTarget.getValue();
        int minYTarget = this.minYTarget.getValue();
        PlayerInfo player;

        List<List<PositionInfo>> possibleCrystals;
        bestPlace = new CrystalInfo.PlaceInfo(-100, null, null, 100d);
        PlayerInfo target;
        switch (mode) {
            case "Lowest":
            case "Nearest":
                // Get the target
                EntityPlayer targetEP;
                if (mode.equals("Lowest"))
                    targetEP = getBasicPlayers(enemyRangeSQ).min((x, y) -> (int) x.getHealth()).orElse(null);
                else
                    targetEP = getBasicPlayers(enemyRangeSQ).min(Comparator.comparingDouble(x -> x.getDistanceSq(mc.player))).orElse(null);

                if (targetEP == null)
                    return;

                player = new PlayerInfo( predictSelfPlace.getValue() ? predictPlayer(mc.player) : mc.player, false);

                if (predictSelfPlace.getValue() && showSelfPredict.getValue())
                    toDisplay.add(new display(player.entity.getEntityBoundingBox(), colorSelf.getColor(), width.getValue()));

                // Get every possible crystals
                possibleCrystals = getPossibleCrystals(player, maxSelfDamage, raytraceValue);

                // If nothing is possible
                if (possibleCrystals == null)
                    return;

                // Get target info
                target = new PlayerInfo(targetEP, armourPercent);

                // Calcualte best cr
                calcualteBest(nThread, possibleCrystals, player.entity.posX, player.entity.posY, player.entity.posZ, target,
                        minDamage, minFacePlaceHp, minFacePlaceDamage, enemyRangeCrystalSQ, maxSelfDamage, maxYTarget, minYTarget);

                return;
            case "Damage":
                // Get every possible players
                List<EntityPlayer> players = getBasicPlayers(enemyRangeSQ).sorted(new Sortbyroll()).collect(Collectors.toList());
                if (players.size() == 0)
                    return;
                // If predict
                if (predictPlaceEnemy.getValue()) {
                    List<List<EntityPlayer>> list = splitListEntity(players, nThread);

                    players.clear();

                    Collection<Future<?>> futures = new LinkedList<>();

                    for (int i = 0; i < nThread; i++) {
                        int finalI = i;
                        // Add them
                        futures.add(executor.submit(() -> getPredicts(list.get(finalI)) ));
                    }

                    try {
                        // For every thread
                        for (Future<?> future : futures) {
                            // Get it
                            List<EntityPlayer> temp;
                            temp = (List<EntityPlayer>) future.get();
                            // If not null, add
                            if (temp != null)
                                players.addAll(temp);
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }

                    //players.replaceAll(entity -> predictPlayer(entity));
                }


                player = new PlayerInfo( predictSelfPlace.getValue() ? predictPlayer(mc.player) : mc.player, false);

                if (predictSelfPlace.getValue() && showSelfPredict.getValue())
                    toDisplay.add(new display(player.entity.getEntityBoundingBox(), colorSelf.getColor(), width.getValue()));


                // If we are placing
                possibleCrystals = getPossibleCrystals(player, maxSelfDamage, raytraceValue);

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
                            minDamage, minFacePlaceHp, minFacePlaceDamage, enemyRangeCrystalSQ, maxSelfDamage, maxYTarget, minYTarget);
                }
                return;
        }
    }

    // Function that call every thread for the calculating of the crystals
    // + return the best place
    void calcualteBest(int nThread, List<List<PositionInfo>> possibleCrystals, double posX, double posY, double posZ,
                       PlayerInfo target, double minDamage, double minFacePlaceHp, double minFacePlaceDamage, double enemyRangeCrystalSQ, double maxSelfDamage,
                       int maxYTarget, int minYTarget) {
        // For getting output of threading
        Collection<Future<?>> futures = new LinkedList<>();
        // Iterate for every thread we have
        for (int i = 0; i < nThread; i++) {
            int finalI = i;
            // Add them
            futures.add(executor.submit(() -> calculateBestPositionTarget(possibleCrystals.get(finalI), posX, posY, posZ,
                    target, minDamage, minFacePlaceHp, minFacePlaceDamage, enemyRangeCrystalSQ, maxSelfDamage, maxYTarget, minYTarget)));
        }
        // Get stack for then collecting the results
        Stack<CrystalInfo.PlaceInfo> results = new Stack<>();
        try {
            // For every thread
            for (Future<?> future : futures) {
                // Get it
                CrystalInfo.PlaceInfo temp;
                temp = (CrystalInfo.PlaceInfo) future.get();
                // If not null, add
                if (temp != null)
                    results.add(temp);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
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
    List<List<PositionInfo>> getPossibleCrystals(PlayerInfo self, double maxSelfDamage, boolean raytrace) {
        // Get every possibilites
        List<BlockPos> possibilites =
                includeCrystalMapping.getValue() ?
                        CrystalUtil.findCrystalBlocksExcludingCrystals(placeRange.getValue().floatValue(), newPlace.getValue())
                        :
                        CrystalUtil.findCrystalBlocks(placeRange.getValue().floatValue(), newPlace.getValue());
        // Output with position and damage
        List<PositionInfo> damagePos = new ArrayList<>();
        for (BlockPos crystal : possibilites) {
            float damage = DamageUtil.calculateDamageThreaded(crystal.getX() + .5D, crystal.getY() + 1D, crystal.getZ() + .5D, self);
            RayTraceResult result;
            // Exclude useless crystals and non-visible in case of raytrace   (1276 56 996) (-1275, 56, 996)
            if (damage < maxSelfDamage
                    && (!raytrace || (!((result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
                    new Vec3d(crystal.getX() + .5d, crystal.getY() + 1D, crystal.getZ() + .5d))) != null && result.typeOfHit != RayTraceResult.Type.ENTITY
            ) || sameBlockPos(result.getBlockPos(), crystal)))) {
                damagePos.add(new PositionInfo(crystal, damage));
            }
        }
        // Remove every crystals that deal more damage to us
        return splitList(damagePos, nThread.getValue());
    }

    // This split the list of positions in multiple list. Is used for multithreading
    List<List<PositionInfo>> splitList(List<PositionInfo> start, int nThreads) {
        // If we have only1  thread, return only 1 thing
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
            output.get(i % nThreads).add(start.get(i));
        }

        // Return
        return output;
    }

    // This calculate the best crystal given a list of possible positions and the enemy
    CrystalInfo.PlaceInfo calculateBestPositionTarget(List<PositionInfo> possibleLocations, double x, double y, double z, PlayerInfo target,
                                                      double minDamage, double minFacePlaceHealth, double minFacePlaceDamage, double enemyRangeSq, double maxSelfDamage,
                                                      int maxYTarget, int minYTarget) {
        // Start calculating damage
        PositionInfo best = new PositionInfo();
        for (PositionInfo crystal : possibleLocations) {

            // Calculate Y
            double temp;
            if ((temp = target.entity.posY - crystal.pos.getY() - 1) > 0 ? temp > minYTarget : temp < -maxYTarget)
                continue;

            double distance;
            // if player is out of range of this crystal, do nothing
            if ((distance = target.entity.getDistanceSq((double) crystal.pos.getX() + 0.5d, (double) crystal.pos.getY() + 1.0d, (double) crystal.pos.getZ() + 0.5d)) <= enemyRangeSq) {
                float currentDamage = DamageUtil.calculateDamageThreaded((double) crystal.pos.getX() + 0.5d, (double) crystal.pos.getY() + 1.0d, (double) crystal.pos.getZ() + 0.5d, target);
                if (currentDamage == best.damage) {
                    // this new crystal is closer
                    // higher chance of being able to break it
                    if (best.pos == null || ((temp = crystal.pos.distanceSq(x, y, z)) == best.distance || (currentDamage / maxSelfDamage) > best.rapp) || temp < best.distance) {
                        // Set new values
                        best = crystal;
                        best.setEnemyDamage(currentDamage);
                        best.distance = distance;
                        best.distancePlayer = mc.player.getDistanceSq((double) crystal.pos.getX() + 0.5d, (double) crystal.pos.getY() + 1.0d, (double) crystal.pos.getZ() + 0.5d);
                    }
                } else if (currentDamage > best.damage) {
                    // Set new values
                    best = crystal;
                    best.setEnemyDamage(currentDamage);
                    best.distance = distance;
                    best.distancePlayer = mc.player.getDistanceSq((double) crystal.pos.getX() + 0.5d, (double) crystal.pos.getY() + 1.0d, (double) crystal.pos.getZ() + 0.5d);
                }
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
            case "Tick":
                if (tickBeforePlace == 0)
                    return true;
                else tickBeforePlace--;
                break;
            case "Time":
                if (!checkTime)
                    return true;
                else if (System.currentTimeMillis() - time >= timeDelayPlace.getValue()) {
                    checkTime = false;
                    return true;
                }
                break;
        }

        return false;
    }

    // Main function for placing crystals
    void placeCrystals() {

        listCrystalsPlaced.updateCrystals();

        if (!canStartPlacing())
            return;

        // Get crystal hand
        EnumHand hand = getHandCrystal();
        if (hand == null)
            return;

        // For debugging timeCalcPlacement
        long inizio = 0;
        if (timeCalcPlacement.getValue())
            // Get time
            inizio = System.currentTimeMillis();
        // Get target
        getTarget(targetPlacing.getValue(), true);
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
            toDisplay.add(new display(bestPlace.crystal, new GSColor(colorPlace.getValue(), alphaPlace.getValue())));
            toDisplay.add(new display(bestPlace.getTarget().getEntityBoundingBox(), showColorPredictEnemy.getColor(), width.getValue()));

            placeCrystal(bestPlace.crystal, hand);

        }

    }

    // Get hand of breaking. Return null if no crystals
    EnumHand getHandCrystal() {
        isSilentSwitching = false;
        // Check offhand
        if (mc.player.getHeldItemOffhand().getItem() instanceof ItemEndCrystal)
            return EnumHand.OFF_HAND;
        else {
            // Check mainhand
            if (mc.player.getHeldItemMainhand().getItem() instanceof ItemEndCrystal) {
                // If you switch, it will place the block you had in your hand before
                if (oldSlot != mc.player.inventory.currentItem)
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                return EnumHand.MAIN_HAND;
            }
            else if (switchHotbar.getValue()) {
                // Get slot
                int slot = InventoryUtil.findFirstItemSlot(ItemEndCrystal.class, 0, 8);
                // If found
                if (slot != -1) {
                    // Silent switch
                    if (silentSwitch.getValue()) {
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                        isSilentSwitching = true;
                    }
                    // Normal switch
                    else {
                        mc.player.inventory.currentItem = slot;
                        mc.playerController.updateController();
                    }
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
            lastHitVec = new Vec3d(pos).add(0.5, 1, 0.5);
            tick = 0;
        }

        // Raytrace
        if (raytrace.getValue()) {

            EnumFacing enumFacing = null;
            RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.getX() + 0.5d, pos.getY() + .5d, pos.getZ() + 0.5d));
            if (result == null || result.sideHit == null) {
                return;
            } else {
                enumFacing = result.sideHit;
            }


            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, enumFacing, handSwing, 0, 0, 0));
        } else {
            // Normal placing
            if (pos.getY() == 255) {
                // For Hoosiers. This is how we do build height. If the target block (q) is at Y 255. Then we send a placement packet to the bottom part of the block. Thus the EnumFacing.DOWN.
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.DOWN, handSwing, 0, 0, 0));
            } else {
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.UP, handSwing, 0, 0, 0));
            }

        }

        if (swingPlace.getValue())
            mc.player.swingArm(handSwing);

        // Return back in case of silent switch
        if (isSilentSwitching)
            mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));

        tickBeforePlace = tickDelayPlace.getValue();
        checkTime = true;
        time = System.currentTimeMillis();
        switch (limitPacketPlace.getValue()) {
            case "Tick":
                listCrystalsPlaced.addCrystal(pos, 0, limitTickPlace.getValue());
                break;
            case "Time":
                listCrystalsPlaced.addCrystal(pos, limitTickTime.getValue());
                break;
        }
    }

    // Given a pos, say if there is a crystal
    boolean isCrystalHere(BlockPos pos) {
        BlockPos posUp = pos.up();

        AxisAlignedBB box = new AxisAlignedBB(
                posUp.getX(), posUp.getY(), posUp.getZ(),
                posUp.getX() + 1.0, posUp.getY() + 2.0, posUp.getZ() + 1.0
        );

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
    List<EntityPlayer> getPredicts(List<EntityPlayer> players) {
        players.replaceAll(entity -> predictPlayer(entity));
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

    // Calculate the predict of the player
    EntityPlayer predictPlayer(EntityPlayer entity) {
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
        if (debugPredict.getValue())
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
                if (!entity.onGround && motionY != -0.0784000015258789 && motionY != 0) {
                    if (start) {
                        // If it's the first time, we have to check first if our motionY is == 0.
                        // MotionY is == 0 when we are jumping at the moment when we are going down
                        if (motionY == 0)
                            motionY = startDecrease.getValue() / Math.pow(10, expnentStartDecrease.getValue());
                        // Check if we are going up or down. We say > because of motionY
                        goingUp = false;
                        start = false;
                        if (debugPredict.getValue())
                            PistonCrystal.printDebug("Start motionY: " + motionY, false);
                    }
                    // Lets just add values to our motionY
                    motionY += goingUp ? increaseY.getValue() / Math.pow(10, exponentIncreaseY.getValue()) : decreaseY.getValue() / Math.pow(10, exponentDecreaseY.getValue());
                    // If the motionY is going too far, go down
                    if (Math.abs(motionY) > startDecrease.getValue() / Math.pow(10, expnentStartDecrease.getValue())) {
                        goingUp = false;
                        if (debugPredict.getValue())
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
                            if (debugPredict.getValue())
                                down++;
                        }
                    }


                }
            }


            if (showPredictions.getValue())
                PistonCrystal.printDebug(String.format("Values: %f %f %f", posVec[0], posVec[1], posVec[2]), false);

        }
        if (debugPredict.getValue()) {
            PistonCrystal.printDebug(String.format("Player: %s Total ticks: %d Up: %d Down: %d", ((EntityPlayer) entity).getGameProfile().getName(), tickPredict.getValue(), up, down), false);
        }
        EntityOtherPlayerMP clonedPlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("fdee323e-7f0c-4c15-8d1c-0f277442342a"), "Fit"));
        clonedPlayer.setPosition(posVec[0], posVec[1], posVec[2]);
        return clonedPlayer;
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

    // This function is for displaying things
    public void onWorldRender(RenderEvent event) {
        toDisplay.forEach(display -> display.draw());
    }

    //endregion

    //region Packet management

    // This function is used for the rotation
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {
        if (event.getPhase() != Phase.PRE || !rotate.getValue() || lastHitVec == null) return;

        if (tick++ >= tickForceRotation.getValue()) {
            tick = 0;
            lastHitVec = null;
            return;
        }

        Vec2f rotation = RotationUtil.getRotationTo(lastHitVec);
        PlayerPacket packet = new PlayerPacket(this, rotation);
        PlayerPacketManager.INSTANCE.addPacket(packet);
    });

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Receive> packetReceiveListener = new Listener<>(event -> {
        if (event.getPacket() instanceof SPacketSpawnObject) {
            SPacketSpawnObject SpawnObject = (SPacketSpawnObject)event.getPacket();
            if (SpawnObject.getType() == 51 ) {
                if (!limitPacketPlace.getValue().equals("None"))
                    listCrystalsPlaced.removeCrystal(SpawnObject.getX(), SpawnObject.getY(), SpawnObject.getZ());
            }
        }
    });

    //endregion
}