package com.gamesense.api.util.world.combat.ac;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;

public class PositionInfo {

    public final BlockPos pos;
    private final double selfDamage;
    public double rapp;
    public double damage;
    public double distance;
    public double distancePlayer;

    public PositionInfo(BlockPos pos, double selfDamage) {
        this.pos = pos;
        this.selfDamage = selfDamage;
    }


    public PositionInfo() {
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
