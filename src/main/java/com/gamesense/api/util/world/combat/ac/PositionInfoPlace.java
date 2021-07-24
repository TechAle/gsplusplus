package com.gamesense.api.util.world.combat.ac;

import net.minecraft.util.math.BlockPos;

public class PositionInfoPlace {

    public final BlockPos pos;
    private final double selfDamage;
    public double rapp;
    public double damage;
    public double distance;
    public double distancePlayer;

    public PositionInfoPlace(BlockPos pos, double selfDamage) {
        this.pos = pos;
        this.selfDamage = selfDamage;
    }


    public PositionInfoPlace() {
        this.pos = null;
        this.selfDamage = 100;
        this.damage = 0;
        this.rapp = 100;
        this.distancePlayer = 100;
    }

    public void setEnemyDamage(double damage) {
        this.rapp = ((this.damage = damage) / this.selfDamage);
    }



}
