package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.SwingEvent;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;

import java.util.Arrays;

@Module.Declaration(name = "Swing", category = Category.Render)
public class Swing extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("Swap", "Silent"), "Swap");

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<SwingEvent> swingEventListener = new Listener<>(event -> {

        switch (mode.getValue()) {

            case "Swap": {
                // swing without triggering event lol
                EnumHand hand = event.getHand().equals(EnumHand.OFF_HAND) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;

                ItemStack stack = mc.player.getHeldItem(hand);
                if (!stack.isEmpty()) {
                    if (stack.getItem().onEntitySwing(mc.player, stack)) return;
                }
                if (!mc.player.isSwingInProgress || mc.player.swingProgressInt >= getArmSwingAnimationEnd() / 2 || mc.player.swingProgressInt < 0) {
                    mc.player.swingProgressInt = -1;
                    mc.player.isSwingInProgress = true;
                    mc.player.swingingHand = hand;

                    if (mc.player.world instanceof WorldServer) {
                        ((WorldServer) mc.player.world).getEntityTracker().sendToTracking(mc.player, new SPacketAnimation(mc.player, hand == EnumHand.MAIN_HAND ? 0 : 3));
                    }
                }

                mc.player.connection.sendPacket(new CPacketAnimation(hand));

                event.cancel();

                break;
            }
            case "Silent": {
                event.cancel();
                mc.player.connection.sendPacket(new CPacketAnimation(event.getHand()));
                break;
            }
        }

    });

    private int getArmSwingAnimationEnd() {
        if (mc.player.isPotionActive(MobEffects.HASTE)) {
            return 6 - (1 + mc.player.getActivePotionEffect(MobEffects.HASTE).getAmplifier());
        } else {
            return mc.player.isPotionActive(MobEffects.MINING_FATIGUE) ? 6 + (1 + mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) * 2 : 6;
        }
    }

}
