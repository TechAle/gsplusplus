package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.BoatMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

@Module.Declaration(name = "BoatFly", category = Category.Movement)
public class BoatFly extends Module {

    DoubleSetting speed = registerDouble("Speed", 2,0,5);
    DoubleSetting ySpeed = registerDouble("Y Speed", 1,0,5);
    DoubleSetting glide = registerDouble("Glide Speed", 0.1,0,1);
    BooleanSetting hover = registerBoolean("Hover", false, ()-> glide.getValue() <= 0);
    BooleanSetting bypass = registerBoolean("NCP", true);
    IntegerSetting strength = registerInteger("Strength", 3,1,20);

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<BoatMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (mc.gameSettings.keyBindJump.isKeyDown()) {

            event.setY(ySpeed.getValue());

        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {

            event.setY(-ySpeed.getValue());

        } else {

            if (!hover.getValue() || mc.player.ticksExisted % 2 == 0) {
                event.setY(-glide.getValue());
            }
            if (hover.getValue() && mc.player.ticksExisted % 2 == 1)
                event.setY(glide.getValue());

        }

        if (MotionUtil.isMoving(mc.player)) {
            MotionUtil.setSpeed(mc.player, speed.getValue());
        } else {
            event.setX(0);
            event.setZ(0);
        }

        if (bypass.getValue() && mc.player.ticksExisted % strength.getValue() == 0)
            doNCPExploit();

    });

    void doNCPExploit() {

        Entity boat = mc.player.ridingEntity;

        if (boat == null)
            return;

        mc.player.connection.sendPacket(new CPacketUseEntity(boat, EnumHand.MAIN_HAND));

    }

}
