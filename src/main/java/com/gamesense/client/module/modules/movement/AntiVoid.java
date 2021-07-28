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
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
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

    ModeSetting mode = registerMode("Mode", Arrays.asList("Freeze"), "Freeze");
    DoubleSetting fheight = registerDouble("Freeze Height", 2, 0, 5);
    /*DoubleSetting height = registerDouble("Catch Height", 2, 0, 5);*/

    @Override
    public void onUpdate() {
        if (!mc.player.noClip /*to account for packetfly*/ && mc.player.posY < fheight.getValue() && mode.getValue().equalsIgnoreCase("Freeze") && mc.world.getBlockState(new BlockPos(mc.player.posX, 1, mc.player.posZ)).getMaterial().isReplaceable()) {

            mc.player.setVelocity(0.0D, 0.0D, 0.0D);

            if (mc.player.getRidingEntity() != null)
                mc.player.getRidingEntity().setVelocity(0.0D, 0.0D, 0.0D);
        }
    }
}

