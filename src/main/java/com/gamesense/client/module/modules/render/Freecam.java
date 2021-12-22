package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.Phase;
import com.gamesense.api.event.events.OnUpdateWalkingPlayerEvent;
import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.util.player.PlayerPacket;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.manager.managers.PlayerPacketManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;

import static java.lang.Math.PI;

@Module.Declaration(name = "Freecam", category = Category.Render)
public class Freecam extends Module {

    BooleanSetting source = registerBoolean("Source Engine", false);
    BooleanSetting noclip = registerBoolean("NoClip", true);
    BooleanSetting cancelPackets = registerBoolean("Cancel Packets", true);
    DoubleSetting speedXZ = registerDouble("Speed XZ", 10, 0, 20);
    DoubleSetting speedY = registerDouble("Speed Y", 10, 0, 20);
    BooleanSetting rotate = registerBoolean("Rotate", false);

    private double posX, posY, posZ;
    private float pitch, yaw;
    Vec3d lastHitVec;

    private EntityOtherPlayerMP clonedPlayer;

    private boolean isRidingEntity;
    private Entity ridingEntity;

    public void onEnable() {
        lastHitVec = null;
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

        mc.player.onGround = true;

        if (!source.getValue()){
            if (mc.gameSettings.keyBindJump.isKeyDown()) {

                mc.player.motionY = (speedY.getValue());

            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {

                mc.player.motionY = (-speedXZ.getValue());

            } else

                mc.player.motionY = 0;

        } else {

            double pitchRad = mc.player.rotationPitch * PI / 180;
            if (MotionUtil.isMoving(mc.player)){
                mc.player.motionY = -Math.sin(pitchRad) * speedXZ.getValue();
            } else
                mc.player.motionY = 0;


        }

        if (MotionUtil.isMoving(mc.player)) {
            MotionUtil.setSpeed(mc.player, speedXZ.getValue());

        } else {

            mc.player.motionX = (0);
            mc.player.motionZ = (0);

        }
        mc.player.noClip = noclip.getValue();
        mc.player.onGround = false;
        mc.player.fallDistance = 0;

        if (rotate.getValue()) {
            RayTraceResult ray = mc.player.rayTrace(12, mc.getRenderPartialTicks());
            if (ray != null && ray.typeOfHit != RayTraceResult.Type.ENTITY) {
                lastHitVec = ray.hitVec;
            }

        }

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
        if ((event.getPacket() instanceof CPacketPlayer.Position || event.getPacket() instanceof CPacketPlayer.PositionRotation || event.getPacket() instanceof CPacketInput) && cancelPackets.getValue()) {
            event.cancel();
        }
    });

    public static double degToRad(double deg) {
        return deg * (float) (PI / 180.0f);
    }


    @EventHandler
    private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {
        // If we dont have to rotate
        if (event.getPhase() != Phase.PRE || !rotate.getValue() || lastHitVec == null || mc.world == null || mc.player == null)
            return;
        EntityPlayer pl = (EntityPlayer) mc.world.getEntityByID(-100);
        Vec2f rotation = RotationUtil.getRotationTo(lastHitVec, pl);
        PlayerPacket packet = new PlayerPacket(this, rotation);
        PlayerPacketManager.INSTANCE.addPacket(packet);
    });

}