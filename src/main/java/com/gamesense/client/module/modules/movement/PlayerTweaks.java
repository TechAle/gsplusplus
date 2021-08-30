package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.EntityCollisionEvent;
import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.WaterPushEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.combat.AntiCrystal;
import com.gamesense.client.module.modules.combat.PistonCrystal;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.InputUpdateEvent;
import org.lwjgl.input.Keyboard;

@Module.Declaration(name = "PlayerTweaks", category = Category.Movement)
public class PlayerTweaks extends Module {

    public BooleanSetting guiMove = registerBoolean("Gui Move", false);
    BooleanSetting noPush = registerBoolean("No Push", false);
    BooleanSetting noPushWater = registerBoolean("No Push Liquid", false);
    BooleanSetting noFall = registerBoolean("No Fall", false);
    public BooleanSetting noSlow = registerBoolean("No Slow", false);
    BooleanSetting antiKnockBack = registerBoolean("Velocity", false);
    DoubleSetting veloXZ = registerDouble("XZ Multiplier", 0,-5,5, () -> antiKnockBack.getValue());
    DoubleSetting veloY = registerDouble("Y Multiplier", 0,-5,5, () -> antiKnockBack.getValue());
    public BooleanSetting noPushBlock = registerBoolean("No Push Block", false);
    BooleanSetting pistonPush = registerBoolean("Anti Piston Push", false);
    IntegerSetting postSecure = registerInteger("Post Secure", 15, 1, 40,
            () -> pistonPush.getValue());


    int ticksBef;

    @EventHandler
    private final Listener<PacketEvent.Send> packetReceiveListener = new Listener<>(event -> {

        if (event.getPacket() instanceof CPacketPlayer.Position || event.getPacket() instanceof CPacketPlayer.PositionRotation) {
            if ( HoleUtil.isHole(EntityUtil.getPosition(mc.player), true, true).getType() != HoleUtil.HoleType.NONE) {

                boolean found = isPushable(mc.player.posX, mc.player.posY, mc.player.posZ);


                if (found) {
                    event.cancel();
                    ticksBef = postSecure.getValue();
                } else if (--ticksBef > 0) {
                    event.cancel();
                }
            }
        }
    });

    boolean isPushable(double x, double y, double z) {
        Block temp;

        if ((temp = BlockUtil.getBlock(x, ++y, z)) == Blocks.PISTON_HEAD || temp == Blocks.PISTON_EXTENSION )
            return true;

        TileEntityShulkerBox tempShulker;
        AxisAlignedBB tempAxis;
        for(TileEntity entity : mc.world.loadedTileEntityList) {
            if (entity instanceof TileEntityShulkerBox) {
                if ((tempShulker = ((TileEntityShulkerBox) entity)).getProgress(mc.getRenderPartialTicks()) > 0)
                    if ((tempAxis = tempShulker.getRenderBoundingBox()).minY <= y && tempAxis.maxY >= y
                    &&  ((int) tempAxis.minX <= x && tempAxis.maxX >= x) || ((int) tempAxis.minZ <= z && tempAxis.maxZ >= z))
                        return true;
            } // (int) == x
        }

        return false;
    }

    public void onUpdate() {
        if (guiMove.getValue() && mc.currentScreen != null) {
            if (!(mc.currentScreen instanceof GuiChat)) {
                if (Keyboard.isKeyDown(200)) {
                    mc.player.rotationPitch -= 5;
                }
                if (Keyboard.isKeyDown(208)) {
                    mc.player.rotationPitch += 5;
                }
                if (Keyboard.isKeyDown(205)) {
                    mc.player.rotationYaw += 5;
                }
                if (Keyboard.isKeyDown(203)) {
                    mc.player.rotationYaw -= 5;
                }
                if (mc.player.rotationPitch > 90) {
                    mc.player.rotationPitch = 90;
                }
                if (mc.player.rotationPitch < -90) {
                    mc.player.rotationPitch = -90;
                }
            }
        }

    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<InputUpdateEvent> eventListener = new Listener<>(event -> {
        if (noSlow.getValue()) {
            if (mc.player.isHandActive() && !mc.player.isRiding()) {
                event.getMovementInput().moveStrafe *= 5;
                event.getMovementInput().moveForward *= 5;
            }
        }
    });

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<EntityCollisionEvent> entityCollisionEventListener = new Listener<>(event -> {
        if (noPush.getValue()) {
            event.cancel();
        }
    });

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (antiKnockBack.getValue()) {
            if (event.getPacket() instanceof SPacketEntityVelocity) {
                if (((SPacketEntityVelocity) event.getPacket()).getEntityID() == mc.player.getEntityId()) {
                    ((SPacketEntityVelocity) event.getPacket()).motionX *= veloXZ.getValue();
                    ((SPacketEntityVelocity) event.getPacket()).motionY *= veloY.getValue();
                    ((SPacketEntityVelocity) event.getPacket()).motionZ *= veloXZ.getValue();
                }

            }
            if (event.getPacket() instanceof SPacketExplosion) {
                ((SPacketExplosion) event.getPacket()).motionX *= veloXZ.getValue();
                ((SPacketExplosion) event.getPacket()).motionY *= veloY.getValue();
                ((SPacketExplosion) event.getPacket()).motionZ *= veloXZ.getValue();
            }
        }
    });

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (noFall.getValue() && event.getPacket() instanceof CPacketPlayer && mc.player.fallDistance >= 3.0 && !(mc.player.isElytraFlying() && ModuleManager.isModuleEnabled("ElytraFlight"))) {
            CPacketPlayer packet = (CPacketPlayer) event.getPacket();
            packet.onGround = true;
        }
    });

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<WaterPushEvent> waterPushEventListener = new Listener<>(event -> {
        if (noPushWater.getValue()) {
            event.cancel();
        }
    });
}