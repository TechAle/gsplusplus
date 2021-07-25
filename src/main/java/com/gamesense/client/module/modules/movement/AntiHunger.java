package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer;

//Pasted from Kami Blue, which pasted from Seppuku

@Module.Declaration(name = "AntiHunger", category = Category.Movement)
public class AntiHunger extends Module {

    BooleanSetting spoofMovement = registerBoolean("Spoof Movement", true);

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Send> packetSendListener = new Listener<>(event -> {
        Packet<?> packet = event.getPacket();
        EntityPlayerSP player = mc.player;

        if (packet instanceof CPacketPlayer) {
            ((CPacketPlayer) packet).onGround = (player.fallDistance <= 0 || mc.playerController.isHittingBlock) && player.isElytraFlying();
        }

        if (packet instanceof CPacketEntityAction
                && spoofMovement.getValue()
                && (((CPacketEntityAction) packet).getAction() == Action.START_SPRINTING 
                    || ((CPacketEntityAction) packet).getAction() == Action.STOP_SPRINTING)) {
            event.cancel();
        }
    });
}
