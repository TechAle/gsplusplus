package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.PlayerPacket;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.manager.managers.PlayerPacketManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.modules.misc.AutoTool;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

@Module.Declaration(name = "ElytraFlight", category = Category.Movement)
public class ElytraFlight extends Module {

    DoubleSetting speed = registerDouble("speed", 1, 0, 25);
    IntegerSetting upLook = registerInteger("upBoostDelay", 35, 0, 250);

    Timer upTimer = new Timer();

    int upLookMS;

    boolean doFlight;

    @Override
    public void onUpdate() {

        if (mc.player.cameraPitch > 0 || upTimer.getTimePassed() >= upLookMS) { // if looking down (+ive pitch) or upTimer has passed
            if (mc.player.isElytraFlying()) {
                doFlight = true; // do elytrafly
                upTimer.reset(); // reset timer (so the timer goes out in bursts)
            }
        }

        upLookMS = 50 * upLook.getValue();

        if (doFlight) { // if should elytrafly

            PlayerPacket packet;
            packet = new PlayerPacket(this, new Vec2f(0, 0)); // to set pitch to 0 (yaw matters not at all and will make us bypass on servers)
            PlayerPacketManager.INSTANCE.addPacket(packet);

            MotionUtil.setSpeed(mc.player, MotionUtil.getBaseMoveSpeed() * speed.getValue()); // fly
        }
    }


    public void onDisable() {

    }

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (doFlight) { // if should be elytraflying

            Double velY = 0d;
            event.setY(velY); // dont go down

        }

    }

    );
}




