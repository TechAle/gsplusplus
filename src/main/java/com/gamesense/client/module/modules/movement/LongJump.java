package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

import java.util.Arrays;

@Module.Declaration(name = "LongJump", category = Category.Movement)
public class LongJump extends Module {

    ModeSetting mode = registerMode("mode", Arrays.asList("BHop", "Peak", "Ground", "Velocity", "Continuous"), "Peak");

    // BHop
    DoubleSetting speed = registerDouble("BHop speed", 2.15, 0, 10, () -> mode.getValue().equalsIgnoreCase("BHop"));

    // Peak
    DoubleSetting farSpeed = registerDouble("Peak Speed", 1, 0, 10, () -> mode.getValue().equalsIgnoreCase("Peak"));
    IntegerSetting farAccel = registerInteger("Peak Acceleration", 0, 1, 5, () -> mode.getValue().equalsIgnoreCase("Peak"));
    DoubleSetting initialFar = registerDouble("Peak Hop Speed", 1, 0, 10, () -> mode.getValue().equalsIgnoreCase("Peak"));

    // Velocity
    BooleanSetting jump = registerBoolean("Jump", true, () -> mode.getValue().equalsIgnoreCase("Velocity"));
    DoubleSetting jumpHeightVelo = registerDouble("Jump Height Velocity", 1,0,10, () -> mode.getValue().equalsIgnoreCase("Velocity"));
    BooleanSetting allowY = registerBoolean("Velocity Multiply", true, () -> mode.getValue().equalsIgnoreCase("Velocity"));
    DoubleSetting xzvelocity = registerDouble("XZ Velocity Multiplier", 0.1,0,5, () -> mode.getValue().equalsIgnoreCase("Velocity"));
    DoubleSetting yvelocity = registerDouble("Y Velocity Multiplier", 0.1,0,2, () -> mode.getValue().equalsIgnoreCase("Velocity"));

    // Continuous
    DoubleSetting factorMax = registerDouble("Factor", 0,0,50, () -> mode.getValue().equalsIgnoreCase("Continuous"));

    // Ground
    DoubleSetting normalSpeed = registerDouble("Ground Speed", 3,0,10, () -> mode.getValue().equalsIgnoreCase("Ground"));

    // Other
    BooleanSetting lagback = registerBoolean("Disable On LagBack", false);
    DoubleSetting jumpHeight = registerDouble("jumpHeight", 0.41, 0, 1);

    Double playerSpeed;

    boolean slowDown;
    boolean hasaccel;

    public boolean velo;

    float mf;

    int i;

    private final Timer timer = new Timer();

    public void onEnable() {
        playerSpeed = MotionUtil.getBaseMoveSpeed();
        mf = mc.player.jumpMovementFactor;
    }

    @Override
    public void onDisable() {
        timer.reset();
        mc.player.jumpMovementFactor = mf;
    }

    @EventHandler
    private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (event.getPacket() instanceof SPacketPlayerPosLook && lagback.getValue())
            disable();

        if ((event.getPacket() instanceof SPacketExplosion || event.getPacket() instanceof SPacketEntityVelocity) && mc.player != null) {
            if (mode.getValue().equals("Velocity")) {

                double[] dir = MotionUtil.forward(1);

                if (jump.getValue() && mc.player.onGround) {

                    mc.player.jumpMovementFactor = jumpHeightVelo.getValue().floatValue();
                    mc.player.motionY = jumpHeight.getValue();

                }

                velo = false;

                if (event.getPacket() instanceof SPacketEntityVelocity){
                    if (!(!allowY.getValue() && ((SPacketEntityVelocity) event.getPacket()).motionY > 0)) {

                        ((SPacketEntityVelocity) event.getPacket()).motionY = ((int) (((SPacketEntityVelocity) event.getPacket()).motionY * yvelocity.getValue()));
                        ((SPacketEntityVelocity) event.getPacket()).motionX = ((int) (((SPacketEntityVelocity) event.getPacket()).motionX * xzvelocity.getValue() * dir[0]));
                        ((SPacketEntityVelocity) event.getPacket()).motionZ = ((int) (((SPacketEntityVelocity) event.getPacket()).motionZ * xzvelocity.getValue() * dir[1]));

                    }
                }
                if (event.getPacket() instanceof SPacketExplosion){
                    if (!(!allowY.getValue() && ((SPacketExplosion) event.getPacket()).motionY > 0)) {

                        ((SPacketExplosion) event.getPacket()).motionY = ((int) (((SPacketExplosion) event.getPacket()).motionY * yvelocity.getValue()));;
                        ((SPacketExplosion) event.getPacket()).motionX = ((int) (((SPacketExplosion) event.getPacket()).motionX * xzvelocity.getValue() * dir[0]));
                        ((SPacketExplosion) event.getPacket()).motionZ = ((int) (((SPacketExplosion) event.getPacket()).motionZ * xzvelocity.getValue() * dir[1]));

                    }
                }

            } else {

                velo = true;

            }
        }

    });

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {
        if (mode.getValue().equals("BHop")) {
            if (mc.player.isInLava() || mc.player.isInWater() || mc.player.isOnLadder() || mc.player.isInWeb || Anchor.active) {
                return;
            }
            double speedY = jumpHeight.getValue();

            if (mc.player.onGround && MotionUtil.isMoving(mc.player) && timer.hasReached(300)) {
                if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                    speedY += (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f;
                }

                event.setY(mc.player.motionY = speedY);
                playerSpeed = MotionUtil.getBaseMoveSpeed() * (EntityUtil.isColliding(0, -0.5, 0) instanceof BlockLiquid && !EntityUtil.isInLiquid() ? 0.9 : speed.getValue());
                slowDown = true;
                timer.reset();
            } else {
                if (slowDown || mc.player.collidedHorizontally) {
                    playerSpeed -= (EntityUtil.isColliding(0, -0.8, 0) instanceof BlockLiquid && !EntityUtil.isInLiquid()) ? 0.4 : 0.7 * (playerSpeed = MotionUtil.getBaseMoveSpeed());
                    slowDown = false;
                } else {
                    playerSpeed -= playerSpeed / 159.0;
                }
            }
            playerSpeed = Math.max(playerSpeed, MotionUtil.getBaseMoveSpeed());
            double[] dir = MotionUtil.forward(playerSpeed);
            event.setX(dir[0]);
            event.setZ(dir[1]);
        }
    });

    @Override
    public void onUpdate() {
        double[] dir = MotionUtil.forward(playerSpeed);
        if (mode.getValue().equalsIgnoreCase("Peak")) {

            if (mc.player.onGround)
                hasaccel=false;

            if (mc.player.onGround && mc.gameSettings.keyBindForward.isKeyDown()) {
                mc.player.motionX = dir[0] * initialFar.getValue().floatValue();
                mc.player.motionZ = dir[1] * initialFar.getValue().floatValue();
                mc.player.motionY = jumpHeight.getValue();
                i = 0;
            }
            if (mc.player.motionY <= 0 && !hasaccel) {
                hasaccel = !mc.player.onGround;
                if (farAccel.getValue().equals(0)) {
                    mc.player.jumpMovementFactor = farSpeed.getValue().floatValue();
                } else {
                    i++;
                    mc.player.jumpMovementFactor = i * (farSpeed.getValue().floatValue() / farAccel.getValue());
                }


            }
        } else if (mode.getValue().equalsIgnoreCase("Continuous")) {

            if (mc.player.onGround) {

                mc.player.jumpMovementFactor = mf;
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motionY = jumpHeight.getValue();
                    dir = MotionUtil.forward(MotionUtil.getBaseMoveSpeed());

                    mc.player.motionX = dir[0]; // instant accel
                    mc.player.motionZ = dir[1]; // instant accel
                }


            } else {

                mc.player.jumpMovementFactor = 0.02f * factorMax.getValue().floatValue();

            }

        } else if (mode.getValue().equalsIgnoreCase("Ground")) {

            if (mc.player.onGround && MotionUtil.isMoving(mc.player)) {

                mc.player.motionY = jumpHeight.getValue();

                mc.player.motionX = dir[0] * normalSpeed.getValue();
                mc.player.motionZ = dir[1] * normalSpeed.getValue();

            }

        }
    }

    public String getHudInfo() {
        return "[" + ChatFormatting.WHITE + mode.getValue() + ChatFormatting.GRAY + "]";
    }

}



