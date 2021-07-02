package com.gamesense.api.util.world.combatRewrite.ac.entityData;

import com.gamesense.api.util.world.combatRewrite.ac.entityData.mobs.*;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.objects.*;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.network.play.server.SPacketSpawnObject;

public class InfoCreator {
    public static EntityInfo createObject(SPacketSpawnObject object) {
        switch (object.getType()) {
            case 1:
                return new BoatInfo(object);
            case 3:
                return new AreaEffectCloudInfo(object);
            case 10:
                return new MinecartInfo(object);
            // Falling Block has different start
            case 70:
                EntityInfo entity = new EntityInfo(object);
                entity.width = 0.98D;
                entity.height = 0.98D;
                entity.updatePosition(object.getX(), object.getY() + ((1.0D - entity.height) / 2.0D), object.getZ());
                entity.updateSize();
                return entity;
            case 71:
                return new ItemFrameInfo(object);
            case 77:
                return new LeashKnotInfo(object);
            case 78:
                return new ArmorStandInfo(object);
            default:
                return new EntityInfo(object);
        }
    }

    public static EntityLivingInfo createMob(SPacketSpawnMob mob) {
        switch (mob.getEntityType()) {
            case 5:
            case 6:
            case 51:
                return new AbstractSkeletonInfo(mob);
            case 23:
            case 27:
            case 54:
            case 57:
                return new ZombieInfo(mob);
            case 28:
            case 29:
                return new UndeadHorseInfo(mob);
            case 31:
            case 32:
                return new AbstractChestHorseInfo(mob);
            case 52:
                return new AbstractSpiderInfo(mob);
            case 55:
            case 62:
                return new SlimeInfo(mob);
            case 60:
            case 67:
                return new SmallThingInfo(mob);
            case 69:
                return new ShulkerInfo(mob);
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 98:
            case 100:
            case 101:
            case 102:
            case 105:
            case 120:
                return new EntityAnimalInfo(mob);
            case 103:
                return new LlamaInfo(mob);
            default:
                return new EntityLivingInfo(mob);
        }
    }
}
