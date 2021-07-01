package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.OnUpdateWalkingPlayerEvent;
import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.combat.CrystalUtil;
import com.gamesense.api.util.world.combat.DamageUtil;
import com.gamesense.api.util.world.combat.ac.CrystalInfo;
import com.gamesense.api.util.world.combat.ac.PlayerInfo;
import com.gamesense.api.util.world.combat.ac.PositionInfo;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module.Declaration(name = "AutoCrystalRewrite", category = Category.Combat, priority = 100)
public class AutoCrystalRewrite extends Module {

    ModeSetting pages = registerMode("Pages", Arrays.asList("Target", "Place", "Break", "Predict", "Misc", "Threading"), "");
    ModeSetting logic = registerMode("Logic", Arrays.asList("Place->Break", "Break->Place", "Place", "Break"), "Place->Break");
    ModeSetting targetPlacing = registerMode("Target Placing", Arrays.asList("Nearest", "Lowest", "Damage"), "Nearest");
    ModeSetting targetBreaking = registerMode("Target Breaking", Arrays.asList("Nearest", "Lowest", "Damage"), "Nearest");
    DoubleSetting placeRange = registerDouble("Place Range", 6, 0, 8);
    BooleanSetting newPlace = registerBoolean("1.13 mode", false);
    DoubleSetting minDamage = registerDouble("Min Damage", 5, 0, 30);
    DoubleSetting maxSelfDamage = registerDouble("Max Self Damage", 12, 0, 30);
    DoubleSetting crystalRangeEnemy = registerDouble("Crytal Range Enemey", 6, 0, 8);
    IntegerSetting armourFacePlace = registerInteger("Armour Health%", 20, 0, 100);
    IntegerSetting facePlaceValue = registerInteger("FacePlace HP", 8, 0, 36);
    IntegerSetting maxYTarget = registerInteger("Max Y Target", 1, 0, 3);
    IntegerSetting minYTarget = registerInteger("Min Y Target", 3, 0, 5);
    IntegerSetting maxTarget = registerInteger("Max Target", 5, 1, 30);
    DoubleSetting minFacePlaceDmg = registerDouble("FacePlace Dmg", 2, 0, 10);
    BooleanSetting raytrace = registerBoolean("Raytrace", false);
    BooleanSetting antiSuicide = registerBoolean("AntiSuicide", true);
    DoubleSetting rangeEnemy = registerDouble("RangeEnemy", 7, 0, 12);
    IntegerSetting nThread = registerInteger("N Thread", 4, 1, 20, () -> pages.getValue().equals("Threading"));
    IntegerSetting nCalc = registerInteger("N Calc Aver", 100, 1, 1000, () -> pages.getValue().equals("Threading"));

    public static boolean stopAC = false;

    ThreadPoolExecutor executor =
            (ThreadPoolExecutor) Executors.newCachedThreadPool();


    CrystalInfo.PlaceInfo best = new CrystalInfo.PlaceInfo(-100, null, null, 100d);

    @Override
    public void onUpdate() {
        if (mc.world == null || mc.player == null || mc.player.isDead || stopAC) return;

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

    }
    /*
        long inizio = System.currentTimeMillis();
        *****
        long fine = System.currentTimeMillis();
        long durata = fine - inizio;
        System.out.format("Esecuzione terminata. Tempo impiegato: %d ms%n", durata);
     */

    ArrayList<Long> durations = new ArrayList<>();

    void placeCrystals() {
        long inizio = System.currentTimeMillis();
        getTarget(targetPlacing.getValue(), true);
        long fine = System.currentTimeMillis();
        durations.add(fine - inizio);
        if (durations.size() > nCalc.getValue()) {
            double sum = durations.stream()
                    .mapToDouble(a -> a)
                    .sum();
            sum /= nCalc.getValue();
            durations.clear();
            PistonCrystal.printDebug(String.format("N: %d Value: %f", nCalc.getValue(), sum), false);
        }
    }

    void breakCrystals() {

    }

    void getTarget(String mode, boolean placing) {
        int nThread = this.nThread.getValue();
        float armourPercent = armourFacePlace.getValue() / 100.0f;
        double minDamage = this.minDamage.getValue();
        double minFacePlaceHp = this.facePlaceValue.getValue();
        double minFacePlaceDamage = this.minFacePlaceDmg.getValue();
        double enemyRangeCrystalSQ = crystalRangeEnemy.getValue() * crystalRangeEnemy.getValue();
        double enemyRangeSQ = rangeEnemy.getValue() * rangeEnemy.getValue();
        double maxSelfDamage = this.maxSelfDamage.getValue();
        boolean raytraceValue = raytrace.getValue();
        int maxYTarget = this.maxYTarget.getValue();
        int minYTarget = this.minYTarget.getValue();
        PlayerInfo player = new PlayerInfo(mc.player, false);
        List<List<PositionInfo>> possibleCrystals;
        best = new CrystalInfo.PlaceInfo(-100, null, null, 100d);
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

                // Get every possible crystals
                possibleCrystals = getPossibleCrystals(player, maxSelfDamage, raytraceValue);

                // If nothing is possible
                if (possibleCrystals == null)
                    return;

                // Get target info
                target = new PlayerInfo(targetEP, armourPercent);

                // Calcualte best cr
                calcualteBest(nThread, possibleCrystals, mc.player.posX, mc.player.posY, mc.player.posZ, target,
                        minDamage, minFacePlaceHp, minFacePlaceDamage, enemyRangeCrystalSQ, maxSelfDamage, maxYTarget, minYTarget);

                return;
            case "Damage":
                // Get every possible players
                List<EntityPlayer> players = getBasicPlayers(enemyRangeSQ).sorted(new Sortbyroll()).collect(Collectors.toList());
                if (players.size() == 0)
                    return;
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
                    calcualteBest(nThread, possibleCrystals, mc.player.posX, mc.player.posY, mc.player.posZ, target,
                            minDamage, minFacePlaceHp, minFacePlaceDamage, enemyRangeCrystalSQ, maxSelfDamage, maxYTarget, minYTarget);
                }
                return;
        }
    }

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
        results.add(best);
        best = getResult(results);
    }

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


    List<List<PositionInfo>> getPossibleCrystals(PlayerInfo self, double maxSelfDamage, boolean raytrace) {
        // Get every possibilites
        List<BlockPos> possibilites = CrystalUtil.findCrystalBlocks(placeRange.getValue().floatValue(), newPlace.getValue());
        // Output with position and damage
        List<PositionInfo> damagePos = new ArrayList<>();
        for (BlockPos crystal : possibilites) {
            float damage = DamageUtil.calculateDamageThreaded(crystal.getX() + .5D, crystal.getY() + 1D, crystal.getZ() + .5D, self);
            RayTraceResult result;
            // Exclude useless crystals and non-visible in case of raytrace
            if (damage < maxSelfDamage
                    && (!raytrace || !((result = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
                    new Vec3d(crystal.getX() + .5d, crystal.getY() + 1D, crystal.getZ() + .5d))) != null && result.typeOfHit != RayTraceResult.Type.ENTITY))) {
                damagePos.add(new PositionInfo(crystal, damage));
            }
        }
        // Remove every crystals that deal more damage to us
        return splitList(damagePos, nThread.getValue());
    }


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

    Stream<EntityPlayer> getBasicPlayers(double rangeEnemySQ) {
        return mc.world.playerEntities.stream()
                .filter(entity -> entity.getDistanceSq(mc.player) <= rangeEnemySQ)
                .filter(entity -> !EntityUtil.basicChecksEntity(entity))
                .filter(entity -> entity.getHealth() > 0.0f);
    }

    static class Sortbyroll implements Comparator<EntityPlayer> {

        @Override
        public int compare(EntityPlayer o1, EntityPlayer o2) {
            return (int) (o1.getDistanceSq(mc.player) - o2.getDistanceSq(mc.player));
        }
    }


    public void onWorldRender(RenderEvent event) {
        if (best.crystal != null)
            RenderUtil.drawBox(best.crystal, 1, new GSColor(255, 255, 255, 50), 63);
    }


    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {

    });

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Receive> packetReceiveListener = new Listener<>(event -> {

    });

    public void onEnable() {

    }

    public void onDisable() {

    }

    public String getHudInfo() {
        String t = "";

        return t;
    }
}