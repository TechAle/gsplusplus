package com.gamesense.client.module.modules.misc;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.SoundEvents;

@Module.Declaration(name = "KillEffect", category = Category.Misc, enabled = true)
public class KillEffect extends Module {

    BooleanSetting thunder = registerBoolean("Thunder", true);
    BooleanSetting sound = registerBoolean("Sound", true);

    @Override
    public void onUpdate() {
        mc.world.playerEntities.stream().filter(entity -> entity.getHealth() <= 0.0f).forEach(entity -> {
            if (thunder.getValue())
                mc.world.spawnEntity(new EntityLightningBolt(mc.world, entity.posX, entity.posY, entity.posZ, true));
            if (sound.getValue()) mc.player.playSound(SoundEvents.ENTITY_LIGHTNING_THUNDER, 0.5f, 1.f);
        });
    }
}
