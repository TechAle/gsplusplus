package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.BlockChangeEvent;
import com.gamesense.api.event.events.TotemPopEvent;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.mojang.authlib.GameProfile;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.GameType;

import java.util.*;


@Module.Declaration(name = "PopChams", category = Category.Render)
public class PopChams extends Module {

    IntegerSetting range = registerInteger("Range", 10,0, 100);
    IntegerSetting width = registerInteger("Line Width", 2, 1, 5);
    IntegerSetting life = registerInteger("Time", 100, 10, 300);
    ColorSetting color = registerColor("Color");


    private int fpNum = 0;
    List<Integer> fakePlayers = new ArrayList<>();

    private int testInt;

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<TotemPopEvent> totemPopEventListener = new Listener<>(event -> {



        Entity e = event.getEntity();


        if(isWithinRange(e)) {
            MessageBus.sendClientPrefixMessage("player popped named: "+e.getName());
            spawnFakePlayer(e);
        }


    });
    @Override
    public void onUpdate() {

        for(int i = 0; i < fakePlayers.size(); i++) {

            if (mc.world.getEntityByID(fakePlayers.get(i)).ticksExisted > life.getValue()) {
                mc.world.removeEntityFromWorld(fakePlayers.get(i));
                fakePlayers.remove(fakePlayers.get(i));
                i--;
            }
        }


    }

    private boolean isWithinRange(Entity entity){
        return entity.getDistance(mc.player) <= range.getValue();
    }

    private void spawnFakePlayer(Entity entity){
        EntityOtherPlayerMP clonedPlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("fdee323e-7f0c-4c15-8d1c-0f277442342a"), ""));
        clonedPlayer.copyLocationAndAnglesFrom(entity);
        clonedPlayer.rotationYawHead = entity.getRotationYawHead();
        clonedPlayer.rotationYaw = entity.rotationYaw;
        clonedPlayer.rotationPitch = entity.rotationPitch;
        clonedPlayer.setGameType(GameType.CREATIVE);
        mc.world.addEntityToWorld((-12356 + fpNum), clonedPlayer);
        fakePlayers.add((-12356 + fpNum));
        fpNum++;

    }


    }
