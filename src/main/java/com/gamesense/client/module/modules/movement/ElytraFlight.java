package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

@Module.Declaration(name = "ElytraFlight", category = Category.Movement)
public class ElytraFlight extends Module {

    ModeSetting mode = registerMode("mode", Arrays.asList("control", "creative", "boost"), "creative");
    BooleanSetting strict = registerBoolean("strict", false, () -> mode.getValue().equals("boost"));
    ModeSetting upMode = registerMode("UpMode", Arrays.asList("jump", "look", "none"), "jump");
    ModeSetting lookMode = registerMode("lookMode", Arrays.asList("client", "user"), "client", () -> upMode.getValue().equals("look"));
    DoubleSetting yawStep = registerDouble("yawStep", 1.5, 0, 10);
    DoubleSetting speed = registerDouble("speed", 1, 0, 25);
    DoubleSetting ySpeed = registerDouble("ySpeed", 1, 0, 5);
    DoubleSetting glideSpeed = registerDouble("glideSpeed", 0.0003, 0, 3);
    IntegerSetting upLook = registerInteger("upBoostDelay", 35, 0, 250);

    boolean doFlight;

    boolean returnViewAngle;

    boolean doStuck;

    boolean doGetPitch;

    float oldPitch;

    double pitchToSetTo = 0;

    boolean doPitch;

    boolean doY;

    @Override
    public void onUpdate() {

        if (mc.player.isElytraFlying()) {
            doFlight = true;
            mc.timer.tickLength = 50;
        } else {
            if(mc.player.onGround){mc.player.jump();}
            mc.timer.tickLength = 300f;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));

        }

        if (doFlight) { // if should elytrafly

            switch (mode.getValue()) {
                case "control":
                    mc.player.capabilities.isFlying = false;
                    if (mc.gameSettings.keyBindJump.isKeyDown() && upMode.getValue().equals("jump")) {

                        mc.player.motionY = ySpeed.getValue();

                        doY = false;

                        doControl();

                    } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {

                        mc.player.motionY = -ySpeed.getValue();

                        doY = false;

                        doControl();

                    } else if (upMode.getValue().equals("look")
                            &&
                            mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindForward.isKeyDown()) {

                        doLookBoost(lookMode.getValue());

                        if (doGetPitch) {

                            oldPitch = mc.player.rotationPitch;
                            doGetPitch = false;

                        }

                        returnViewAngle = true;

                    } else if (upMode.getValue().equals("none")) {

                        doControl();

                    } else {

                        pitchToSetTo = 0;

                        if (returnViewAngle) {

                            mc.player.rotationPitch = oldPitch;

                            returnViewAngle = false;

                            doGetPitch = true;

                            doY = true;

                        }

                        doControl();

                    }

                    break;
                case "creative":

                    if ((mc.player.ticksExisted % upLook.getValue() == 0 && upMode.getValue().equals("look") || upMode.getValue().equals("none") || upMode.getValue().equals("jump"))) {

                        doCreative();

                    }
                    break;
                case "boost":

                    doBoost();

                    break;
            }
        }
    }


    public void onDisable() {

        mc.player.capabilities.isFlying = false;
        mc.timer.tickLength = 50;
        doFlight = false;

    }

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (doFlight && doY && mode.getValue().equals("control")) { // if should be elytraflying

            Double velY = 0d;
            event.setY(velY); // don't go down

        }

        if (doStuck) { // dont float about at 14 km/h or so if idle

            Double velX = 0d;
            Double velZ = 0d;

            event.setX(velX);
            event.setZ(velZ);

        }

    });

    // Skiddable flight modes

    public void doControl() {
        // if no keys are down
        if (!MotionUtil.isMoving(mc.player)) {

            doStuck = true;

        } else {
            doStuck = false;
        }

        mc.player.setVelocity(0, 0, 0);



        mc.player.capabilities.isFlying = false;

        MotionUtil.setSpeed(mc.player, MotionUtil.getBaseMoveSpeed() * speed.getValue()); // fly

    }

    public void doCreative() {

        mc.player.capabilities.isFlying = true;
        mc.player.capabilities.setFlySpeed(speed.getValue().floatValue());

        mc.player.setVelocity(0, 0 - glideSpeed.getValue(), 0);

    }

    public void doBoost() {

        if (mc.player.rotationYaw > 0 || !strict.getValue()) {
            if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindForward.isKeyDown()) {
                float yaw = (float) Math.toRadians(mc.player.rotationYaw);
                mc.player.motionX -= MathHelper.sin(yaw) * 0.05f;
                mc.player.motionZ += MathHelper.cos(yaw) * 0.05f;
            }
        }
    }

    public void doLookBoost(String kind) {

        pitchToSetTo += -yawStep.getValue();

        doY = false;

        doPitch = true;

        if (kind == "client") {
            mc.player.rotationPitch = ((float) pitchToSetTo);
        } else if (kind == "user" && mc.player.rotationPitch > 0) {
            mc.player.rotationPitch = 0;
        }

        if (!(pitchToSetTo > -91 && pitchToSetTo < -1)) {

            pitchToSetTo = 0;
            doPitch = false;
            doControl();
            doY = true;
        }
    }
}