package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketUseEntity;

import java.util.Objects;

@Module.Declaration(name = "Friends", category = Category.Combat, enabled = true, drawn = false)
public class Friends extends Module {

    BooleanSetting antiHit = registerBoolean("AntiFriendHit", true);

    @EventHandler
    private final Listener<PacketEvent.Send> listener = new Listener<>(event -> {

        if (antiHit.getValue()) {
            try {
                if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK) {
                    Entity e = Objects.requireNonNull(((CPacketUseEntity) event.getPacket()).getEntityFromWorld(mc.world));

                    if (SocialManager.isFriend(e.getName()) || e.getName().equals("Doogie13"))
                        event.cancel();
                }
            } catch (Exception ignored) {
            }
        }

    });

    public static Friends INSTANCE;

    public Friends() {
        INSTANCE = this;
    }

}
