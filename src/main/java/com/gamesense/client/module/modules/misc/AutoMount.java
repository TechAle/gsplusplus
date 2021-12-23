package com.gamesense.client.module.modules.misc;

import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.*;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec2f;

@Module.Declaration(name = "AutoMount", category = Category.Misc)
public class AutoMount extends Module {

    @Override
    public void onUpdate() {
        if (mc.player.ridingEntity != null)
            return;

        for (Entity e : mc.world.loadedEntityList) {

            if (valid(e)) {

                Vec2f rot = RotationUtil.getRotationTo(e.getPositionVector());
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rot.x,rot.y,mc.player.onGround));
                mc.playerController.interactWithEntity(mc.player,e, EnumHand.MAIN_HAND);

            }

        }

    }

    boolean valid(Entity entity) {

        return (entity instanceof EntityBoat
                || (entity instanceof EntityAnimal && ((EntityAnimal) entity).getGrowingAge() == 1
                && (entity instanceof EntityHorse
                || entity instanceof EntitySkeletonHorse
                || entity instanceof EntityDonkey
                || entity instanceof EntityMule
                || entity instanceof EntityPig && ((EntityPig) entity).getSaddled()
                || entity instanceof EntityLlama)));

    }

}
