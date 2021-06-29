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
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module.Declaration(name = "AutoCrystalRewrite", category = Category.Combat, priority = 100)
public class AutoCrystalRewrite extends Module {

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
    IntegerSetting maxTarget = registerInteger("Max Target", 5, 1, 30);
    DoubleSetting minFacePlaceDmg = registerDouble("FacePlace Dmg", 2, 0, 10);
    BooleanSetting antiSuicide = registerBoolean("AntiSuicide", true);
    DoubleSetting rangeEnemy = registerDouble("RangeEnemy", 7, 0, 12);
    IntegerSetting nThread = registerInteger("N Thread", 4, 1, 8);

    public static boolean stopAC = false;

    Stack<CrystalInfo.PlaceInfo> result = new Stack<>();
    CrystalInfo.PlaceInfo best = new CrystalInfo.PlaceInfo(-100, null, null);

    EntityPlayer target = null;
    EntityEnderCrystal crystCalc;

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

    void placeCrystals() {
        getTarget(targetPlacing.getValue(), true);
    }

    void breakCrystals() {

    }

    ArrayList<Object> getTarget(String mode, boolean placing) {
        int nThread = this.nThread.getValue();
        float armourPercent = armourFacePlace.getValue() / 100.0f;
        double minDamage = this.minDamage.getValue();
        double minFacePlaceHp = this.facePlaceValue.getValue();
        double minFacePlaceDamage = this.minFacePlaceDmg.getValue();
        double enemyRangeCrystalSQ = crystalRangeEnemy.getValue() * 2;
        switch (mode) {
            case "Lowest":
                return new ArrayList<Object>() {{
                    add(getBasicPlayers().min((x, y) -> (int) x.getHealth()).orElse(null));
                    add(null);
                }};
            case "Nearest":
                return new ArrayList<Object>() {{
                    add(getBasicPlayers().min(Comparator.comparingDouble(x -> x.getDistanceSq(mc.player))).orElse(null));
                    add(null);
                }};
            case "Damage":
                // If we are placing
                if (placing) {

                    PlayerInfo player = new PlayerInfo(mc.player, false);

                    List<List<BlockPos>> possibleCrystals = getPossibleCrystals(player, maxSelfDamage.getValue(), antiSuicide.getValue());

                    // If nothing is possible
                    if (possibleCrystals == null)
                        return null;
                    List<EntityPlayer> players = getBasicPlayers().collect(Collectors.toList());
                    Collections.sort(players, new Sortbyroll());
                    // For every players
                    int count = 0;
                    best = new CrystalInfo.PlaceInfo(-100, null, null);
                    for (EntityPlayer playerTemp : players) {
                        if (count++ >= maxTarget.getValue())
                            break;
                        ExecutorService executor = Executors.newCachedThreadPool();
                        long inizio = System.currentTimeMillis();
                        PlayerInfo target = new PlayerInfo(playerTemp, armourPercent);
                        result.clear();

                        Thread[] th = new Thread[nThread];

                        for(int i = 0; i < nThread; i++) {
                            int finalI = i;
                            th[i] = new CaratteriThread(possibleCrystals.get(finalI), mc.player.posX, mc.player.posY, mc.player.posZ,
                                    target, minDamage, minFacePlaceHp, minFacePlaceDamage, enemyRangeCrystalSQ);
                            th[i].start();
                        }

                        try {
                            for(int i = 0; i < nThread; i++)
                                th[i].join();
                            long fine = System.currentTimeMillis();
                            long durata = fine - inizio;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        while (!result.isEmpty()) {
                            CrystalInfo.PlaceInfo now = result.pop();
                            if (now.damage > best.damage)
                                best = now;
                        }
                    }
                    // If we are breaking
                } else {

                }
                break;
        }
        return null;
    }


    List<List<BlockPos>> getPossibleCrystals(PlayerInfo self, double maxSelfDamage, boolean antiSuicide) {
        // Get every possibilites
        List<BlockPos> possibilites = CrystalUtil.findCrystalBlocks(placeRange.getValue().floatValue(), newPlace.getValue());
        // Remove every crystals that deal more damage to us
        possibilites.removeIf(crystal -> {
            float damage = DamageUtil.calculateDamageThreaded(crystal.getX() + .5D, crystal.getY() + 1D, crystal.getZ() + .5D, self);
            if (damage > maxSelfDamage) {
                return true;
            } else {
                return antiSuicide && damage > self.health;
            }
        });
        // Return splitted
        return splitList(possibilites, nThread.getValue());
    }


    List<List<BlockPos>> splitList(List<BlockPos> start, int nThreads) {
        // If we have only1  thread, return only 1 thing
        if (nThreads == 1)
            return new ArrayList<List<BlockPos>>() {
                {
                    get(0).addAll(start);
                }
            };
        // Get n^Possibilites
        int count;
        if ((count = start.size()) == 0)
            return null;
        // Output
        List<List<BlockPos>> output = new ArrayList<>(nThreads);
        for (int i = 0; i < nThreads; i++) output.add(new ArrayList<BlockPos>());

        // Add everything
        for (int i = 0; i < count; i++) {
            output.get(i % nThreads).add(start.get(i));
        }

        // Return
        return output;
    }


    class CaratteriThread extends Thread {
        List<BlockPos> possibleLocations;
        double x, y, z, minDamage, minFacePlaceHealth, minFacePlaceDamage, enemyRangeSq;
        PlayerInfo target;
        public CaratteriThread(List<BlockPos> possibleLocations, double x, double y, double z, PlayerInfo target,
                               double minDamage, double minFacePlaceHealth, double miNFacePlaceDamage, double enemyRangeSq) {
            this.possibleLocations = possibleLocations;
            this.x = x;
            this.y = y;
            this.z = z;
            this.minDamage = minDamage;
            this.minFacePlaceHealth = minFacePlaceHealth;
            this.minFacePlaceDamage = miNFacePlaceDamage;
            this.target = target;
            this.enemyRangeSq = enemyRangeSq;
        }


        @Override
        public void run() {

            // Start calculating damage
            BlockPos best = null;
            float bestDamage = 0f;
            for (BlockPos crystal : possibleLocations) {
                // if player is out of range of this crystal, do nothing
                if (target.entity.getDistanceSq((double) crystal.getX() + 0.5d, (double) crystal.getY() + 1.0d, (double) crystal.getZ() + 0.5d) <= enemyRangeSq) {
                    float currentDamage = DamageUtil.calculateDamageThreaded((double) crystal.getX() + 0.5d, (double) crystal.getY() + 1.0d, (double) crystal.getZ() + 0.5d, target);
                    if (currentDamage == bestDamage) {
                        // this new crystal is closer
                        // higher chance of being able to break it
                        if (best == null || crystal.distanceSq(x, y, z) < best.distanceSq(x, y, z)) {
                            bestDamage = currentDamage;
                            best = crystal;
                        }
                    } else if (currentDamage > bestDamage) {
                        bestDamage = currentDamage;
                        best = crystal;
                    }
                }
            }

            if (best != null) {
                if (bestDamage >= minDamage || ((target.health <= minFacePlaceHealth || target.lowArmour) && bestDamage >= minFacePlaceDamage)) {
                    result.add(new CrystalInfo.PlaceInfo(bestDamage, target, best));
                }
            }
        }
    }

    Stream<EntityPlayer> getBasicPlayers() {
        return mc.world.playerEntities.stream()
                .filter(entity -> entity.getDistanceSq(mc.player) <= rangeEnemy.getValue())
                .filter(entity -> !EntityUtil.basicChecksEntity(entity))
                .filter(entity -> entity.getHealth() > 0.0f);
    }

    class Sortbyroll implements Comparator<EntityPlayer>
    {

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