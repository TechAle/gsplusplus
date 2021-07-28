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
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Array;
import java.util.Arrays;

@Module.Declaration(name = "AntiVoid", category = Category.Movement)
public class AntiVoid extends Module {

    ModeSetting mode = registerMode("Mode", Arrays.asList("Freeze", "Catch"), "Freeze");
    DoubleSetting fheight = registerDouble("Freeze Height", 2,0,5);
    DoubleSetting height = registerDouble("Catch Height", 2,0,5);

    boolean doPlace;
    int targetBlockSlot;
    boolean cont;

    @Override
    public void onUpdate() {
        if (!mc.player.noClip /*to account for packetfly*/ && mc.player.posY < fheight.getValue() && mode.getValue().equalsIgnoreCase("Freeze") && mc.world.getBlockState(new BlockPos(mc.player.posX,1,mc.player.posZ)).getMaterial().isReplaceable()) {

            mc.player.setVelocity(0.0D, 0.0D, 0.0D);

            if (mc.player.getRidingEntity() != null)
                mc.player.getRidingEntity().setVelocity(0.0D, 0.0D, 0.0D);
        } else if (!mc.player.noClip /*to account for packetfly*/ && mc.player.posY < height.getValue() && mode.getValue().equalsIgnoreCase("Catch")) {

                placeBlockPacket(null,(new BlockPos(mc.player.posX,1, mc.player.posZ)));

        }
    }
    void placeBlockPacket(EnumFacing side, BlockPos pos) {

        cont = true;
        doPlace = true;

        targetBlockSlot = -1;

        for (int i = 1; i < 10; i++) {

            if (cont && mc.player.inventory.getStackInSlot(i) != ItemStack.EMPTY && mc.player.inventory.getStackInSlot(i).item instanceof ItemBlock) {

                targetBlockSlot = i;

                cont = false;

            }

        }


        if (!(targetBlockSlot == -1)) {

            if (mc.player.posY > 0){
                MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You are out of blocks... good luck!");
            }
            doPlace = false;

        } else {
            doPlace = true;
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

