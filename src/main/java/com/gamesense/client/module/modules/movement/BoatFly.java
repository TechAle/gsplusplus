package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.BoatMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.EnumHand;

@Module.Declaration(name = "BoatFly", category = Category.Movement)
public class BoatFly extends Module {

    DoubleSetting speed = registerDouble("Speed", 2, 0, 10);
    DoubleSetting ySpeed = registerDouble("Y Speed", 1, 0, 10);
    DoubleSetting glideSpeed = registerDouble("Glide Speed", 0, -10, 10);
    BooleanSetting hover = registerBoolean("Hover", false);
    BooleanSetting bypass = registerBoolean("Bypass", false);

    private final Listener<BoatMoveEvent> boatMoveEventListener = new Listener<>(event -> {

        if (mc.gameSettings.keyBindJump.isKeyDown())
            event.setY(ySpeed.getValue());

         else if (mc.gameSettings.keyBindSneak.isKeyDown())
            event.setY(-ySpeed.getValue());

        else
            event.setY(hover.getValue() && mc.player.ticksExisted % 2 == 0 ? glideSpeed.getValue() : -glideSpeed.getValue());


        if (MotionUtil.isMoving(mc.player)) {

            double[] dir = MotionUtil.forward(speed.getValue());

            event.setX(dir[0]);
            event.setZ(dir[1]);

        } else {
            event.setX(0);
            event.setZ(0);
        }

    });

    @Override
    public void onUpdate() {
        if (bypass.getValue() && mc.player.ticksExisted % 4 == 0)
            if (mc.player.ridingEntity instanceof EntityBoat)
                mc.playerController.interactWithEntity(mc.player,mc.player.ridingEntity, EnumHand.MAIN_HAND);
    }
}
