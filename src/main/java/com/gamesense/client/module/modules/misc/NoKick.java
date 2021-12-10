package com.gamesense.client.module.modules.misc;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.network.play.server.SPacketSoundEffect;
import org.lwjgl.input.Keyboard;

/**
 * @see com.gamesense.mixin.mixins.MixinNetworkManager
 */

@Module.Declaration(name = "NoKick", category = Category.Misc)
public class NoKick extends Module {

    public BooleanSetting noPacketKick = registerBoolean("Packet", true);
    BooleanSetting noSlimeCrash = registerBoolean("Slime", false);
    BooleanSetting noOffhandCrash = registerBoolean("Offhand", false);
    BooleanSetting noSignCrash = registerBoolean("Cancel Sign Edit", false); // shift to actually write sign lol

    public void onUpdate() {
        if (mc.world != null && noSlimeCrash.getValue()) {
            mc.world.loadedEntityList.forEach(entity -> {
                if (entity instanceof EntitySlime) {
                    EntitySlime slime = (EntitySlime) entity;
                    if (slime.getSlimeSize() > 4) {
                        mc.world.removeEntity(entity);
                    }
                }
            });
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (noOffhandCrash.getValue() && !Keyboard.isKeyDown(42)) {
            if (event.getPacket() instanceof SPacketSoundEffect) {
                if (((SPacketSoundEffect) event.getPacket()).getSound() == SoundEvents.ITEM_ARMOR_EQUIP_GENERIC) {
                    event.cancel();
                }
            }
        }
    });

    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {

        if (event.getPacket() instanceof CPacketUpdateSign && noSignCrash.getValue() && !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {

            event.cancel();

        }

    });
}