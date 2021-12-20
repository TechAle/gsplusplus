package com.gamesense.client.module.modules.misc;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;

import java.util.ArrayList;

@Module.Declaration(name = "KillEffect", category = Category.Misc, enabled = true)
public class KillEffect extends Module {

    BooleanSetting thunder = registerBoolean("Thunder", true);
    IntegerSetting numbersThunder = registerInteger("Number Thunder", 1, 1, 10);
    BooleanSetting sound = registerBoolean("Sound", true);
    IntegerSetting numberSound = registerInteger("Number Sound", 1, 1, 10);
    BooleanSetting circle = registerBoolean("Circle", false);


    ArrayList<EntityPlayer> playersDead = new ArrayList<>();


    @Override
    protected void onEnable() {
        playersDead.clear();
    }

    @Override
    public void onUpdate() {

        if (mc.world == null || mc.player == null) {
            playersDead.clear();
            return;
        }

        mc.world.playerEntities.forEach(entity -> {
            if (playersDead.contains(entity)) {
                if (entity.getHealth() > 0)
                    playersDead.remove(entity);
            } else {
                if (entity.getHealth() == 0) {
                    if (thunder.getValue())
                        for(int i = 0; i < numbersThunder.getValue(); i++)
                            mc.world.spawnEntity(new EntityLightningBolt(mc.world, entity.posX, entity.posY, entity.posZ, true));
                    if (sound.getValue())
                        for(int i = 0; i < numberSound.getValue(); i++)
                            mc.player.playSound(SoundEvents.ENTITY_LIGHTNING_THUNDER, 0.5f, 1.f);
                    playersDead.add(entity);
                }
            }
        });
    }
}
