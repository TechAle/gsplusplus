package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
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

import java.util.Arrays;

@Module.Declaration(name = "ElytraFlight", category = Category.Movement)
public class ElytraFlight extends Module {

    ModeSetting upMode = registerMode("UpMode", Arrays.asList("jump", "look", "none"), "jump");
    DoubleSetting speed = registerDouble("speed", 1, 0, 25);
    DoubleSetting ySpeed = registerDouble("ySpeed", 1,0,100);
    IntegerSetting upLook = registerInteger("upBoostDelay", 35, 0, 250);

    Timer upTimer = new Timer();

    boolean doFlight;

    boolean doY;

    @Override
    public void onUpdate() {

        //System.out.println(upTimer.getTimePassed());

        if (mc.player.cameraPitch > 0 /*|| 50 / upTimer.getTimePassed() >= upLook.getValue()*/) { // if looking down (+ive pitch) or upTimer has passed
            if (mc.player.isElytraFlying()) {
                doFlight = true; // do elytrafly
            }
        }

        if (doFlight) { // if should elytrafly



            if (mc.gameSettings.keyBindJump.isKeyDown() && upMode.getValue() == "jump") {

                mc.player.motionY = ySpeed.getValue();

                doY = false;

            } else if (mc.gameSettings.keyBindSneak.isKeyDown() && upMode.getValue() == "jump") {

                mc.player.motionY = -ySpeed.getValue();

                doY = false;

            } else if (upTimer.getTimePassed() >= (upLook.getValue() * 50) && mc.player.cameraPitch < 0 && upMode.getValue() == "look") {

                upTimer.reset();

                doY = false;

            } else if (upMode.getValue() == "none"){



            }

            else {

                mc.player.setVelocity(0,0,0);

                MotionUtil.setSpeed(mc.player, MotionUtil.getBaseMoveSpeed() * speed.getValue()); // fly

                PlayerPacket packet;
                packet = new PlayerPacket(this, new Vec2f(0, 0)); // to set pitch to 0 (yaw matters not at all and will make us bypass on servers)
                PlayerPacketManager.INSTANCE.addPacket(packet);

                doY = true;

            }
        }
    }


    public void onDisable() {

    }

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (doFlight && doY) { // if should be elytraflying

            Double velY = 0d;
            event.setY(velY); // dont go down

        }

    }

    );
}




