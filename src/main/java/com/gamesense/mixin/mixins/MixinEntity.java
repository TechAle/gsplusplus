package com.gamesense.mixin.mixins;

import com.gamesense.api.event.events.EntityCollisionEvent;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.movement.SafeWalk;
import com.gamesense.client.module.modules.movement.Scaffold;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Inject(method = "applyEntityCollision", at = @At("HEAD"), cancellable = true)
    public void velocity(Entity entityIn, CallbackInfo ci) {
        EntityCollisionEvent event = new EntityCollisionEvent();
        GameSense.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    public boolean isSneaking(Entity entity) {
        return (ModuleManager.isModuleEnabled(Scaffold.class)) && !Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown() || ModuleManager.isModuleEnabled(SafeWalk.class) || entity.isSneaking();
    }

    // I am truly sorry Nep for making you read obfuscated minecraft code :pensive:
    @Inject(method = "move",at = @At(value = "HEAD"))
    public void move(MoverType type, double tx, double ty, double tz, CallbackInfo ci) {


        try {
            Minecraft mc = Minecraft.getMinecraft();

            if (mc.world == null || mc.player == null)
                return;

            if (PlayerUtil.nullCheck())
                return;

            double x = tx;
            double y = ty;
            double z = tz;

            if (ci.isCancelled())
                return;

            EntityPlayer player = new EntityPlayer(mc.player.world, mc.player.gameProfile) {
                @Override
                public boolean isSpectator() {
                    return false;
                }

                @Override
                public boolean isCreative() {
                    return false;
                }
            };

            player.noClip = mc.player.noClip;
            player.world = mc.world;
            player.setPositionAndUpdate(mc.player.posX, mc.player.posY, mc.player.posZ);
            player.setEntityBoundingBox(mc.player.getEntityBoundingBox());
            player.onGround = mc.player.onGround;
            player.stepHeight = mc.player.stepHeight;

            if (!player.noClip) {
                if (type.equals(MoverType.PISTON))
                    return;

                player.world.profiler.startSection("move");

                if (player.isInWeb) {
                    return;
                }

                double d2 = x;
                double d3 = y;
                double d4 = z;

                if ((type == MoverType.SELF || type == MoverType.PLAYER) && player.onGround && player.isSneaking()) {
                    for (double d5 = 0.05D; x != 0.0D && player.world.getCollisionBoxes(player, player.getEntityBoundingBox().offset(x, (double) (-player.stepHeight), 0.0D)).isEmpty(); d2 = x) {
                        if (x < 0.05D && x >= -0.05D) {
                            x = 0.0D;
                        } else if (x > 0.0D) {
                            x -= 0.05D;
                        } else {
                            x += 0.05D;
                        }
                    }

                    for (; z != 0.0D && player.world.getCollisionBoxes(player, player.getEntityBoundingBox().offset(0.0D, (double) (-player.stepHeight), z)).isEmpty(); d4 = z) {
                        if (z < 0.05D && z >= -0.05D) {
                            z = 0.0D;
                        } else if (z > 0.0D) {
                            z -= 0.05D;
                        } else {
                            z += 0.05D;
                        }
                    }

                    for (; x != 0.0D && z != 0.0D && player.world.getCollisionBoxes(player, player.getEntityBoundingBox().offset(x, (double) (-player.stepHeight), z)).isEmpty(); d4 = z) {
                        if (x < 0.05D && x >= -0.05D) {
                            x = 0.0D;
                        } else if (x > 0.0D) {
                            x -= 0.05D;
                        } else {
                            x += 0.05D;
                        }

                        d2 = x;

                        if (z < 0.05D && z >= -0.05D) {
                            z = 0.0D;
                        } else if (z > 0.0D) {
                            z -= 0.05D;
                        } else {
                            z += 0.05D;
                        }
                    }
                }

                List<AxisAlignedBB> list1 = player.world.getCollisionBoxes(player, player.getEntityBoundingBox().expand(x, y, z));
                AxisAlignedBB axisalignedbb = player.getEntityBoundingBox();

                if (y != 0.0D) {
                    int k = 0;

                    for (int l = list1.size(); k < l; ++k) {
                        y = ((AxisAlignedBB) list1.get(k)).calculateYOffset(player.getEntityBoundingBox(), y);
                    }

                    player.setEntityBoundingBox(player.getEntityBoundingBox().offset(0.0D, y, 0.0D));
                }

                if (x != 0.0D) {
                    int j5 = 0;

                    for (int l5 = list1.size(); j5 < l5; ++j5) {
                        x = ((AxisAlignedBB) list1.get(j5)).calculateXOffset(player.getEntityBoundingBox(), x);
                    }

                    if (x != 0.0D) {
                        player.setEntityBoundingBox(player.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
                    }
                }

                if (z != 0.0D) {
                    int k5 = 0;

                    for (int i6 = list1.size(); k5 < i6; ++k5) {
                        z = ((AxisAlignedBB) list1.get(k5)).calculateZOffset(player.getEntityBoundingBox(), z);
                    }

                    if (z != 0.0D) {
                        player.setEntityBoundingBox(player.getEntityBoundingBox().offset(0.0D, 0.0D, z));
                    }
                }

                boolean flag = player.onGround || d3 != y && d3 < 0.0D;

                if (player.stepHeight > 0.0F && flag && (d2 != x || d4 != z)) {
                    double d14 = x;
                    double d6 = y;
                    double d7 = z;
                    AxisAlignedBB axisalignedbb1 = player.getEntityBoundingBox();
                    player.setEntityBoundingBox(axisalignedbb);
                    y = (double) player.stepHeight;
                    List<AxisAlignedBB> list = player.world.getCollisionBoxes(player, player.getEntityBoundingBox().expand(d2, y, d4));
                    AxisAlignedBB axisalignedbb2 = player.getEntityBoundingBox();
                    AxisAlignedBB axisalignedbb3 = axisalignedbb2.expand(d2, 0.0D, d4);
                    double d8 = y;
                    int j1 = 0;

                    for (int k1 = list.size(); j1 < k1; ++j1) {
                        d8 = ((AxisAlignedBB) list.get(j1)).calculateYOffset(axisalignedbb3, d8);
                    }

                    axisalignedbb2 = axisalignedbb2.offset(0.0D, d8, 0.0D);
                    double d18 = d2;
                    int l1 = 0;

                    for (int i2 = list.size(); l1 < i2; ++l1) {
                        d18 = ((AxisAlignedBB) list.get(l1)).calculateXOffset(axisalignedbb2, d18);
                    }

                    axisalignedbb2 = axisalignedbb2.offset(d18, 0.0D, 0.0D);
                    double d19 = d4;
                    int j2 = 0;

                    for (int k2 = list.size(); j2 < k2; ++j2) {
                        d19 = ((AxisAlignedBB) list.get(j2)).calculateZOffset(axisalignedbb2, d19);
                    }

                    axisalignedbb2 = axisalignedbb2.offset(0.0D, 0.0D, d19);
                    AxisAlignedBB axisalignedbb4 = player.getEntityBoundingBox();
                    double d20 = y;
                    int l2 = 0;

                    for (int i3 = list.size(); l2 < i3; ++l2) {
                        d20 = ((AxisAlignedBB) list.get(l2)).calculateYOffset(axisalignedbb4, d20);
                    }

                    axisalignedbb4 = axisalignedbb4.offset(0.0D, d20, 0.0D);
                    double d21 = d2;
                    int j3 = 0;

                    for (int k3 = list.size(); j3 < k3; ++j3) {
                        d21 = ((AxisAlignedBB) list.get(j3)).calculateXOffset(axisalignedbb4, d21);
                    }

                    axisalignedbb4 = axisalignedbb4.offset(d21, 0.0D, 0.0D);
                    double d22 = d4;
                    int l3 = 0;

                    for (int i4 = list.size(); l3 < i4; ++l3) {
                        d22 = ((AxisAlignedBB) list.get(l3)).calculateZOffset(axisalignedbb4, d22);
                    }

                    axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d22);
                    double d23 = d18 * d18 + d19 * d19;
                    double d9 = d21 * d21 + d22 * d22;

                    if (d23 > d9) {
                        x = d18;
                        z = d19;
                        y = -d8;
                        player.setEntityBoundingBox(axisalignedbb2);
                    } else {
                        x = d21;
                        z = d22;
                        y = -d20;
                        player.setEntityBoundingBox(axisalignedbb4);
                    }

                    int j4 = 0;

                    for (int k4 = list.size(); j4 < k4; ++j4) {
                        y = ((AxisAlignedBB) list.get(j4)).calculateYOffset(player.getEntityBoundingBox(), y);
                    }

                    player.setEntityBoundingBox(player.getEntityBoundingBox().offset(0.0D, y, 0.0D));

                    if (d14 * d14 + d7 * d7 >= x * x + z * z) {
                        x = d14;
                        y = d6;
                        z = d7;
                        player.setEntityBoundingBox(axisalignedbb1);
                    }
                }

                player.world.profiler.endSection();
                player.world.profiler.startSection("rest");
                player.resetPositionToBB();
                player.collidedHorizontally = d2 != x || d4 != z;
                player.collidedVertically = d3 != y;
                player.onGround = player.collidedVertically && d3 < 0.0D;
                player.collided = player.collidedHorizontally || player.collidedVertically;
                int j6 = MathHelper.floor(player.posX);
                int i1 = MathHelper.floor(player.posY - 0.20000000298023224D);
                int k6 = MathHelper.floor(player.posZ);
                BlockPos blockpos = new BlockPos(j6, i1, k6);
                IBlockState iblockstate = player.world.getBlockState(blockpos);

                if (iblockstate.getMaterial() == Material.AIR) {
                    BlockPos blockpos1 = blockpos.down();
                    IBlockState iblockstate1 = player.world.getBlockState(blockpos1);
                    Block block1 = iblockstate1.getBlock();

                    if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate) {
                        iblockstate = iblockstate1;
                        blockpos = blockpos1;
                    }
                }
            }
        } catch (Exception ignored) {}
    }

}