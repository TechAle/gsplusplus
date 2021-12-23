package com.gamesense.client.module.modules.misc;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.ChatType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

@Module.Declaration(name = "QueueNotifier", category = Category.Misc)
public class QueueNotifier extends Module {
    BooleanSetting techale = registerBoolean("Techale mode", false);

    @EventHandler
    Listener<ClientChatReceivedEvent> listener = new Listener<>(event -> {
        String message = event.getMessage().getUnformattedText();
        if (message.matches("Position in queue: ([1-5]\\b|[12]0)") && event.getType() == ChatType.SYSTEM) {
            if (techale.getValue()) {
                for (int i = 0; i < 29; i++) {
                    playSound();
                }
            }
            playSound();
        }
    });

    private void playSound() {
        mc.soundHandler.playSound(PositionedSoundRecord.getRecord(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f));
    }
}
