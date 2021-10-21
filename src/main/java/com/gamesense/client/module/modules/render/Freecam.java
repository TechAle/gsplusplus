package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import io.netty.util.internal.MathUtil;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;

import static java.lang.Math.PI;

@Module.Declaration(name = "Freecam", category = Category.Render)
public class Freecam extends Module {

    BooleanSetting source = registerBoolean("Source Engine", false);
    BooleanSetting noclip = registerBoolean("NoClip", true);
    BooleanSetting cancelPackets = registerBoolean("Cancel Packets", true);
    DoubleSetting speed = registerDouble("Speed", 10, 0, 20);

    private double posX, posY, posZ;
    private float pitch, yaw;

    private EntityOtherPlayerMP clonedPlayer;

    private boolean isRidingEntity;
    private Entity ridingEntity;

    public void onEnable() {
        if (mc.player != null) {
            isRidingEntity = mc.player.getRidingEntity() != null;

            if (mc.player.getRidingEntity() == null) {
                posX = mc.player.posX;
                posY = mc.player.posY;
                posZ = mc.player.posZ;
            } else {
                ridingEntity = mc.player.getRidingEntity();
                mc.player.dismountRidingEntity();
            }

            pitch = mc.player.rotationPitch;
            yaw = mc.player.rotationYaw;

            clonedPlayer = new EntityOtherPlayerMP(mc.world, mc.getSession().getProfile());
            clonedPlayer.copyLocationAndAnglesFrom(mc.player);
            clonedPlayer.rotationYawHead = mc.player.rotationYawHead;
            mc.world.addEntityToWorld(-100, clonedPlayer);
            mc.player.noClip = noclip.getValue();
        }
    }

    public void onDisable() {
        EntityPlayer localPlayer = mc.player;
        if (localPlayer != null) {
            mc.player.setPositionAndRotation(posX, posY, posZ, yaw, pitch);
            mc.world.removeEntityFromWorld(-100);
            clonedPlayer = null;
            posX = posY = posZ = 0.D;
            pitch = yaw = 0.f;
            mc.player.noClip = false;
            mc.player.motionX = mc.player.motionY = mc.player.motionZ = 0.f;

            if (isRidingEntity) {
                mc.player.startRiding(ridingEntity, true);
            }
        }
    }

    public void onUpdate() {

        if (!source.getValue()){
            if (mc.gameSettings.keyBindJump.isKeyDown()) {

                mc.player.motionY = (speed.getValue());

            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {

                mc.player.motionY = (-speed.getValue());

            } else

                mc.player.motionY = 0;

        } else {

            double pitchRad = mc.player.rotationPitch * PI / 180;
            if (MotionUtil.isMoving(mc.player)){
                mc.player.motionY = -Math.sin(pitchRad) * speed.getValue();
            } else
                mc.player.motionY = 0;


        }

        if (MotionUtil.isMoving(mc.player)) {
            MotionUtil.setSpeed(mc.player, speed.getValue());

        } else {

            mc.player.motionX = (0);
            mc.player.motionZ = (0);

        }
        mc.player.noClip = noclip.getValue();
        mc.player.onGround = false;
        mc.player.fallDistance = 0;

    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PlayerMoveEvent> moveListener = new Listener<>(event -> {
        mc.player.noClip = noclip.getValue();
    });

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PlayerSPPushOutOfBlocksEvent> pushListener = new Listener<>(event -> {
        event.setCanceled(true);
    });

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if ((event.getPacket() instanceof CPacketPlayer || event.getPacket() instanceof CPacketInput) && cancelPackets.getValue()) {
            event.cancel();
        }
    });

    public static double degToRad(double deg) {
        return deg * (float) (PI / 180.0f);
    }

}