package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.modules.combat.PistonCrystal;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

@Module.Declaration(name = "Blink", category = Category.Movement)
public class Blink extends Module {

    BooleanSetting ghostPlayer = registerBoolean("Ghost Player", true);
    BooleanSetting keepRotations = registerBoolean("Keep Rotations", false);
    ModeSetting scatterTiming = registerMode("Scatter", Arrays.asList("Numbers", "Timer"), "Milliseconds");
    IntegerSetting millisecondPackets = registerInteger("Milliseconds", 5000, 1000, 10000, () -> scatterTiming.getValue().equals("Timer"));
    IntegerSetting nPacketsLimit = registerInteger("Number Packets", 150, 0, 2000, () -> scatterTiming.getValue().equals("Numbers"));
    IntegerSetting outputScatter = registerInteger("Output Scatter", 10, 0, 50);
    BooleanSetting debug = registerBoolean("Debug", false);
    BooleanSetting shiftScatter = registerBoolean("Shift Scatter", false);

    private EntityOtherPlayerMP entity;
    public final ArrayList<Packet<?>> packets = new ArrayList<>();
    private boolean startScatter;
    private long startScatterTimer;
    private int nPackets;

    public void onEnable() {
        EntityPlayerSP player = mc.player;
        WorldClient world = mc.world;
        startScatter = isRemoving = false;
        nPackets = 0;
        startScatterTimer = System.currentTimeMillis();

        if (player == null || world == null) {
            disable();
        } else if (ghostPlayer.getValue()) {
            entity = new EntityOtherPlayerMP(world, mc.getSession().getProfile());
            entity.copyLocationAndAnglesFrom(player);
            entity.inventory.copyInventory(player.inventory);
            entity.rotationYaw = player.rotationYaw;
            entity.rotationYawHead = player.rotationYawHead;
            world.addEntityToWorld(667, entity);
        }
        packets.clear();
    }

    boolean isRemoving = false;

    public void onUpdate() {
        if (mc.player == null || mc.world == null)
            disable();
        Entity entity = this.entity;
        WorldClient world = mc.world;

        if (!ghostPlayer.getValue() && entity != null && world != null) {
            world.removeEntity(entity);
        }

        if (shiftScatter.getValue() && mc.gameSettings.keyBindSneak.isPressed())
            startScatter = true;

        if (!startScatter)
            switch (scatterTiming.getValue()) {
                case "Timer":
                    if (System.currentTimeMillis() - startScatterTimer >= millisecondPackets.getValue()) {
                        startScatter = true;

                        if (debug.getValue())
                            PistonCrystal.printDebug("N^Packets: " + nPackets, false);
                    }
                    break;
                case "Numbers":
                    if (nPackets >= nPacketsLimit.getValue()) {
                        startScatter = true;
                        if (debug.getValue())
                            PistonCrystal.printDebug("N^Packets: " + nPackets, false);
                    }
                    break;
            }

        if (startScatter) {
            isRemoving = true;
            for (int i = 0; i < outputScatter.getValue(); i++) {
                if (packets.size() == 0) {
                    disable();
                    return;
                }
                if (ghostPlayer.getValue()) {
                    CPacketPlayer packet = (CPacketPlayer) packets.get(0);
                    Objects.requireNonNull(entity).setPosition(packet.x, packet.y, packet.z);
                }
                mc.player.connection.sendPacket(packets.get(0));
                packets.remove(0);
                nPackets--;

            }
            isRemoving = false;
        }
    }

    public void onDisable() {


        Entity entity = this.entity;
        WorldClient world = mc.world;

        if (entity != null && world != null) {
            world.removeEntity(entity);
        }

        EntityPlayerSP player = mc.player;

        if (packets.size() > 0 && player != null) {
            for (Packet<?> packet : packets) {
                player.connection.sendPacket(packet);
            }
            packets.clear();
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Send> packetSendListener = new Listener<>(event -> {
        if (isRemoving)
            return;
        if (mc.player == null || mc.world == null)
            disable();
        if (!keepRotations.getValue()){
            Packet<?> packet = event.getPacket();

            if (mc.player != null && mc.player.isEntityAlive() && packet instanceof CPacketPlayer) {
                packets.add(packet);
                nPackets++;
                event.cancel();
            }
        } else {
            Packet<?> packet = event.getPacket();

            if (mc.player != null && mc.player.isEntityAlive()) {
                if (packet instanceof CPacketPlayer.Position || packet instanceof CPacketPlayer.PositionRotation) {
                    packets.add(packet);
                    nPackets++;
                    event.cancel();
                }
            }




        }
    });

    public String getHudInfo() {
        return "[" + ChatFormatting.WHITE + nPackets + ChatFormatting.GRAY + "]";
    }
}