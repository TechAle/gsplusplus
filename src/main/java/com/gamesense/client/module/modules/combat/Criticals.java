package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;

import java.util.Arrays;
import java.util.Objects;

@Module.Declaration(name = "Criticals", category = Category.Combat)
public class Criticals extends Module {

    ModeSetting mode =  registerMode("Mode", Arrays.asList("Packet","Hop"), "Packet");
    BooleanSetting allowWater = registerBoolean("In Water", false);

    @EventHandler
    private final Listener<PacketEvent.Send> listener = new Listener<>(event -> {

        if (PlayerUtil.nullCheck())
            if (event.getPacket() instanceof CPacketUseEntity && (!mc.player.isInWater() || !allowWater.getValue())) {
                if (((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world) != null)
                    if (((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK && mc.player.onGround && !(mc.world.getEntityByID(Objects.requireNonNull(((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world)).getEntityId()) instanceof EntityEnderCrystal)) {
                        switch (mode.getValue().toLowerCase()) {
                            case "packet" :{
                                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.11, mc.player.posZ, false));
                                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1100013579, mc.player.posZ, false));
                                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.3579E-6, mc.player.posZ, false));
                                mc.player.connection.sendPacket(new CPacketPlayer());
                                break;
                            }

                            case "hop" : {

                                mc.player.motionY = 0.1f;
                                mc.player.fallDistance = 0.1f;
                                mc.player.onGround = false;
                                break;
                            }
                        }
                    }
            }
    });
}
