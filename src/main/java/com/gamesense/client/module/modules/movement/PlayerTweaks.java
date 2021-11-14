package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.EntityCollisionEvent;
import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.event.events.WaterPushEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.HoleUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.InputUpdateEvent;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

@Module.Declaration(name = "PlayerTweaks", category = Category.Movement)
public class PlayerTweaks extends Module {

    boolean snk;
    boolean lastTickOG;

    public BooleanSetting guiMove = registerBoolean("Gui Move", false);
    public BooleanSetting noSlow = registerBoolean("No Slow", false);
    BooleanSetting strict = registerBoolean("No Slow Strict", false, () -> noSlow.getValue());
    DoubleSetting speed = registerDouble("No Slow Strict Ground Speed", 0.2,0,1);
    public BooleanSetting webT = registerBoolean("No Slow Web", false);
    public BooleanSetting noPushBlock = registerBoolean("No Push Block", false);
    public BooleanSetting portalChat = registerBoolean("Portal Chat", false);
    BooleanSetting noPushWater = registerBoolean("No Push Liquid", false);
    BooleanSetting noFall = registerBoolean("No Fall", false);
    ModeSetting noFallMode = registerMode("No Fall Mode", Arrays.asList("Packet", "OldFag", "Catch"), "Packet", () -> noFall.getValue());
    ModeSetting catchM = registerMode("Catch Material", Arrays.asList("Web", "Water"), "Water", () -> noFallMode.getValue().equalsIgnoreCase("Catch"));
    BooleanSetting noFallDC = registerBoolean("Disconnect", false, () -> noFall.getValue());
    BooleanSetting antiKnockBack = registerBoolean("Velocity", false);
    BooleanSetting akbM = registerBoolean("Non 0 value", false);
    DoubleSetting veloXZ = registerDouble("XZ Multiplier", 0, -5, 5, () -> antiKnockBack.getValue() && akbM.getValue());
    DoubleSetting veloY = registerDouble("Y Multiplier", 0, -5, 5, () -> antiKnockBack.getValue() && akbM.getValue());
    BooleanSetting pistonPush = registerBoolean("Anti Piston Push", false);
    IntegerSetting postSecure = registerInteger("Post Secure", 15, 1, 40, () -> pistonPush.getValue());

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<InputUpdateEvent> eventListener = new Listener<>(event -> {

        if(mc.player.isHandActive() && !mc.player.isRiding()) {

            if (strict.getValue() && !snk) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                snk = true;
            }
            if (strict.getValue() && mc.player.onGround && lastTickOG) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                snk = false;
                mc.player.movementInput.moveForward /= speed.getValue();
                mc.player.movementInput.moveStrafe /= speed.getValue();
            }


            if (noSlow.getValue() || !mc.player.onGround && noSlow.getValue() && strict.getValue()) {
                mc.player.movementInput.moveForward /= 0.2f;
                mc.player.movementInput.moveStrafe /= 0.2f;
            }
        }

        lastTickOG = mc.player.onGround;
    });

    BooleanSetting noPush = registerBoolean("No Push", false);
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<EntityCollisionEvent> entityCollisionEventListener = new Listener<>(event -> {
        if (noPush.getValue()) {
            event.cancel();
        }
    });
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<WaterPushEvent> waterPushEventListener = new Listener<>(event -> {
        if (noPushWater.getValue()) {
            event.cancel();
        }
    });
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (ModuleManager.getModule(LongJump.class).velo || !ModuleManager.getModule(LongJump.class).isEnabled()) {
            if (antiKnockBack.getValue() && akbM.getValue()) {
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
            } else if (antiKnockBack.getValue() && !akbM.getValue()) {

                if (event.getPacket() instanceof SPacketEntityVelocity) {
                    if (((SPacketEntityVelocity) event.getPacket()).getEntityID() == mc.player.getEntityId()) {
                        event.cancel();
                    }

                }
                if (event.getPacket() instanceof SPacketExplosion) {
                    event.cancel();
                }

            }
        }
    });
    int ticksBef;
    @EventHandler
    private final Listener<PacketEvent.Send> packetReceiveListener = new Listener<>(event -> {

        if (event.getPacket() instanceof CPacketPlayer.Position || event.getPacket() instanceof CPacketPlayer.PositionRotation) {
            if (HoleUtil.isHole(EntityUtil.getPosition(mc.player), true, true).getType() != HoleUtil.HoleType.NONE) {

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
    BlockPos n1;
    Vec2f rot;
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (PlayerUtil.nullCheck()) {

            if (noFall.getValue() && event.getPacket() instanceof CPacketPlayer && mc.player.fallDistance >= 3.0 && !(mc.player.isElytraFlying())) {

                mc.player.connection.getNetworkManager().handleDisconnection();

                CPacketPlayer packet = (CPacketPlayer) event.getPacket();
                if (noFallMode.getValue().equalsIgnoreCase("Packet")) {
                    packet.onGround = true;
                    mc.player.fallDistance = 0;

                } else if (noFallMode.getValue().equalsIgnoreCase("OldFag") && predict(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ))) {

                    mc.player.motionY = 0.0;
                    packet.y = n1.getY();
                    mc.player.fallDistance = 0.0f;

                } else if (noFallMode.getValue().equalsIgnoreCase("Catch")) {

                    rot = RotationUtil.getRotationTo(mc.player.getPositionVector().add(0, -3, 0));

                    int oldSlot = mc.player.inventory.currentItem;
                    int slot = catchM.getValue().equalsIgnoreCase("Web") ? getSlot(Blocks.WEB) : getSlot(Items.WATER_BUCKET);

                    if (slot != -1) {
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));

                        if (catchM.getValue().equalsIgnoreCase("Web")) {
                            try {
                                PlacementUtil.place(getDownPos(), EnumHand.MAIN_HAND, false);
                            } catch (NullPointerException ignored) {
                            }
                        } else {
                            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(getDownPos(), EnumFacing.UP, EnumHand.MAIN_HAND, 0, 0, 0));
                        }

                        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
                    }
                }
            }
            if (noFallDC.getValue() && (mc.player.fallDistance - 2.1 >= mc.player.getHealth())) {

                mc.player.connection.getNetworkManager().closeChannel(new TextComponentString(ChatFormatting.GOLD + "Player would have taken fall damage"));

            }

        }
    });

    public static int getSlot(Block blockToFind) {

        int slot = -1;
        for (int i = 0; i < 9; i++) {

            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }

            Block block = ((ItemBlock) stack.getItem()).getBlock();
            if (block.equals(blockToFind)) {
                slot = i;
                break;
            }

        }

        return slot;

    }

    public static int getSlot(Item blockToFind) {

        int slot = -1;
        for (int i = 0; i < 9; i++) {

            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) {
                continue;
            }

            if (stack.getItem() == blockToFind) {
                slot = i;
                break;
            }

        }

        return slot;

    }

    boolean isPushable(double x, double y, double z) {
        Block temp;

        if ((temp = BlockUtil.getBlock(x, ++y, z)) == Blocks.PISTON_HEAD || temp == Blocks.PISTON_EXTENSION)
            return true;

        TileEntityShulkerBox tempShulker;
        AxisAlignedBB tempAxis;
        for (TileEntity entity : mc.world.loadedTileEntityList) {
            if (entity instanceof TileEntityShulkerBox) {
                if ((tempShulker = ((TileEntityShulkerBox) entity)).getProgress(mc.getRenderPartialTicks()) > 0)
                    if ((tempAxis = tempShulker.getRenderBoundingBox()).minY <= y && tempAxis.maxY >= y
                            && ((int) tempAxis.minX <= x && tempAxis.maxX >= x) || ((int) tempAxis.minZ <= z && tempAxis.maxZ >= z))
                        return true;
            } // (int) == x
        }

        return false;
    }

    public void onUpdate() {

        if (!ModuleManager.isModuleEnabled(Timer.class)){
            if (mc.player.isInWeb && !mc.player.onGround && webT.getValue()) {
                mc.timer.tickLength = 1;
                mc.player.moveForward = 0f;
                mc.player.moveStrafing = 0f;
            }
            else
                mc.timer.tickLength = 50;
        }

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

    private boolean predict(BlockPos blockPos) {
        n1 = blockPos.add(0, -3, 0);
        return mc.world.getBlockState(n1).getBlock() != Blocks.AIR;
    }

    BlockPos getDownPos() {

        BlockPos e = null;

        for (int i = 0; i < 5; i++) {
            // get down block and add 1
            if (!mc.world.isAirBlock(new BlockPos(mc.player.getPositionVector()).add(0, -i, 0))) {
                e = new BlockPos(mc.player.getPositionVector()).add(0, -i + 1, 0);
                break;
            }
        }

        return e;

    }

}