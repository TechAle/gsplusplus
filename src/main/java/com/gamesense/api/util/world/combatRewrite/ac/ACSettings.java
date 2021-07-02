package com.gamesense.api.util.world.combatRewrite.ac;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.PlayerInfo;

public class ACSettings {
    public final String breakMode;
    public final String handBreak;
    public final String breakType;
    public final String crystalPriority;
    public final boolean breakCrystal;
    public final boolean placeCrystal;
    public final int breakSpeed;
    public final int placeSpeed;
    public final double breakRange;
    public final double placeRange;
    public final double wallsRange;
    public final double enemyRange;
    public final boolean antiWeakness;
    public final boolean antiSuicide;
    public final int antiSuicideValue;
    public final boolean autoSwitch;
    public final boolean noGapSwitch;
    public final boolean endCrystalMode;
    public final double minDmg;
    public final double minBreakDmg;
    public final double maxSelfDmg;
    public final int facePlaceValue;
    public final int armourFacePlace;
    public final double minFacePlaceDmg;
    public final boolean rotate;
    public final boolean raytrace;

    public final PlayerInfo player;

    public ACSettings(String breakMode, String handBreak, String breakType, String crystalPriority, boolean breakCrystal, boolean placeCrystal, int breakSpeed, int placeSpeed, double breakRange, double placeRange, double wallsRange, double enemyRange, boolean antiWeakness, boolean antiSuicide, int antiSuicideValue, boolean autoSwitch, boolean noGapSwitch, boolean endCrystalMode, double minDmg, double minBreakDmg, double maxSelfDmg, int facePlaceValue, int armourFacePlace, double minFacePlaceDmg, boolean rotate, boolean raytrace, PlayerInfo player) {
        this.breakMode = breakMode;
        this.handBreak = handBreak;
        this.breakType = breakType;
        this.crystalPriority = crystalPriority;
        this.breakCrystal = breakCrystal;
        this.placeCrystal = placeCrystal;
        this.breakSpeed = breakSpeed;
        this.placeSpeed = placeSpeed;
        this.breakRange = breakRange;
        this.placeRange = placeRange;
        this.wallsRange = wallsRange;
        this.enemyRange = enemyRange;
        this.antiWeakness = antiWeakness;
        this.antiSuicide = antiSuicide;
        this.antiSuicideValue = antiSuicideValue;
        this.autoSwitch = autoSwitch;
        this.noGapSwitch = noGapSwitch;
        this.endCrystalMode = endCrystalMode;
        this.minDmg = minDmg;
        this.minBreakDmg = minBreakDmg;
        this.maxSelfDmg = maxSelfDmg;
        this.facePlaceValue = facePlaceValue;
        this.armourFacePlace = armourFacePlace;
        this.minFacePlaceDmg = minFacePlaceDmg;
        this.rotate = rotate;
        this.raytrace = raytrace;

        this.player = player;
    }
}
