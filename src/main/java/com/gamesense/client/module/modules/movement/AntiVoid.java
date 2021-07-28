package com.gamesense.client.module.modules.movement;

import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Array;
import java.util.Arrays;

@Module.Declaration(name = "AntiVoid", category = Category.Movement)
public class AntiVoid extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("Freeze", "Catch"), "Freeze");
    DoubleSetting height = registerDouble("Catch Height", 2,0,5);

    boolean doPlace;

    @Override
    public void onUpdate() {
        if (!mc.player.noClip /*to account for packetfly*/ && mc.player.posY < 0 && mode.getValue().equalsIgnoreCase("Freeze")) {

            mc.player.setVelocity(0.0D, 0.0D, 0.0D);

            if (mc.player.getRidingEntity() != null)
                mc.player.getRidingEntity().setVelocity(0.0D, 0.0D, 0.0D);
        } else if (!mc.player.noClip /*to account for packetfly*/ && mc.player.posY < height.getValue() && mode.getValue().equalsIgnoreCase("Catch") && (mc.world.getBlockState(new BlockPos(mc.player.posX,0, mc.player.posZ)).getMaterial().isReplaceable())) {

                placeBlockPacket(null,(new BlockPos(mc.player.posX,0, mc.player.posZ)));

        }
    }
    void placeBlockPacket(EnumFacing side, BlockPos pos) {

        int targetBlockSlot = InventoryUtil.findObsidianSlot(false, true);

        if (!(targetBlockSlot > 0 && targetBlockSlot < 9)) {

            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You are fucked...");
            doPlace = false;

        }

        if (doPlace) {
            mc.player.inventory.currentItem = targetBlockSlot;

            if (side == null) {
                side = BlockUtil.getPlaceableSide(pos);
            }
            if (side == null) {
                return;
            }
            BlockPos neighbour = pos.offset(side);
            EnumFacing opposite = side.getOpposite();
            Vec3d vec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));

            // idk why these but PlayerControllerMP use them
            float f = (float) (vec.x - (double) pos.getX());
            float f1 = (float) (vec.y - (double) pos.getY());
            float f2 = (float) (vec.z - (double) pos.getZ());

            // Place
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                    neighbour
                    , opposite, EnumHand.MAIN_HAND, f, f1, f2));

            // Swing
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
    }
}

