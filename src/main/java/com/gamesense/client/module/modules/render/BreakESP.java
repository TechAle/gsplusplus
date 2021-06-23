package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.DrawBlockDamageEvent;
import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.GeometryMasks;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.modules.combat.PistonCrystal;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Hoosiers
 * @since 12/13/2020
 */

@Module.Declaration(name = "BreakESP", category = Category.Render)
public class BreakESP extends Module {

    ModeSetting renderType = registerMode("Render", Arrays.asList("Outline", "Fill", "Both"), "Both");
    IntegerSetting lineWidth = registerInteger("Width", 1, 0, 5);
    IntegerSetting range = registerInteger("Range", 100, 1, 200);
    IntegerSetting tickPacket = registerInteger("Tick Packet", 50, 0, 200);
    IntegerSetting stillRender = registerInteger("Still Render", 20, 0, 500);
    BooleanSetting cancelAnimation = registerBoolean("No Animation", true);
    BooleanSetting showPercentage = registerBoolean("Show Percentage", false);
    BooleanSetting showPacket = registerBoolean("Show possible packet mine", false);
    ColorSetting colorNotReady = registerColor("Color Not Ready", new GSColor(255, 0, 0, 255));
    ColorSetting colorReady = registerColor("Color Ready", new GSColor(0, 255, 0, 255));
    ColorSetting textColor  = registerColor("Text Color", new GSColor(255, 255, 255));
    ArrayList<ArrayList<Object>> possiblePacket = new ArrayList<>();

    // Fast Reset, this is on by default since well, it has no cons
    @EventHandler
    private final Listener<PacketEvent.Receive> packetReceiveListener = new Listener<>(event -> {
        if (!showPacket.getValue())
            return;
        // If packet digging
        if(event.getPacket() instanceof SPacketBlockBreakAnim) {
            // Get it
            SPacketBlockBreakAnim pack = (SPacketBlockBreakAnim) event.getPacket();
            // If we dont have it
            if (!havePos(pack.getPosition()))
                possiblePacket.add(new ArrayList<Object>() {{
                    add(pack.getPosition());
                    add(0);
                }});


        }
    });

    boolean havePos(BlockPos pos) {
        for(ArrayList<Object> part : possiblePacket) {
            // If we already have it
            BlockPos temp = (BlockPos) part.get(0);
            if (temp.getX() == pos.getX() && temp.getY() == pos.getY() && temp.getZ() == pos.getZ()) {
                // Remove
                return true;
            }
        }
        return false;
    }

    public void onWorldRender(RenderEvent event) {
        ArrayList<BlockPos> displayed = new ArrayList<>();
        mc.renderGlobal.damagedBlocks.forEach((integer, destroyBlockProgress) -> {
            if (destroyBlockProgress != null) {

                BlockPos blockPos = destroyBlockProgress.getPosition();

                if (mc.world.getBlockState(blockPos).getBlock() == Blocks.AIR) return;

                if (blockPos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) <= range.getValue()) {

                    displayed.add(blockPos);

                    int progress = destroyBlockProgress.getPartialBlockDamage();
                    AxisAlignedBB axisAlignedBB = mc.world.getBlockState(blockPos).getSelectedBoundingBox(mc.world, blockPos);
                    renderESP(axisAlignedBB, progress, progress >= 8 ? colorReady.getColor() : colorNotReady.getValue(), 8);
                    float temp;
                    if (showPercentage.getValue())
                        showPercentage(blockPos, new String[]{String.format("%.02f%%", (temp = (float) progress / 2 * 25) >= 100 ? 100 : temp)});
                }
            }
        });

        if (showPacket.getValue()) {

           for(int i = 0; i < possiblePacket.size(); i++) {

               BlockPos temp = (BlockPos) possiblePacket.get(i).get(0);
               int tick = (int) possiblePacket.get(i).get(1);

               if (BlockUtil.getBlock(temp) instanceof BlockAir) {
                   possiblePacket.remove(i);
                   i--;
                   continue;
               }

               if (!displayed.contains(temp)) {
                   if (temp.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) <= range.getValue()) {
                       AxisAlignedBB axisAlignedBB = mc.world.getBlockState(temp).getSelectedBoundingBox(mc.world, temp);
                       renderESP(axisAlignedBB, tick >= tickPacket.getValue() ? tickPacket.getValue() : tick,
                               tick > tickPacket.getValue() ? colorReady.getColor() : colorNotReady.getValue(), tickPacket.getValue());
                       if (showPercentage.getValue())
                           showPercentage(temp, new String[]{String.format("%.02f%%", (float) (tick >= tickPacket.getValue() ? tickPacket.getValue() : tick) / tickPacket.getValue() * 100)});
                   }
               } else possiblePacket.get(i).set(1, ++tick);
               if (++tick > tickPacket.getValue() + stillRender.getValue() ) {
                   possiblePacket.remove(i);
                   i--;
               } else possiblePacket.get(i).set(1, tick);
           }
        }


    }

    void showPercentage(BlockPos pos, String[] perc) {
        RenderUtil.drawNametag((double) pos.getX() + 0.5d, (double) pos.getY() + 0.5d,
                (double) pos.getZ() + 0.5d, perc, textColor.getColor(), 1);
    }

    private void renderESP(AxisAlignedBB axisAlignedBB, int progress, GSColor color, int max) {
        GSColor fillColor = new GSColor(color, 50);
        GSColor outlineColor = new GSColor(color, 255);

        double centerX = axisAlignedBB.minX + ((axisAlignedBB.maxX - axisAlignedBB.minX) / 2);
        double centerY = axisAlignedBB.minY + ((axisAlignedBB.maxY - axisAlignedBB.minY) / 2);
        double centerZ = axisAlignedBB.minZ + ((axisAlignedBB.maxZ - axisAlignedBB.minZ) / 2);
        double progressValX = progress * ((axisAlignedBB.maxX - centerX) / max);
        double progressValY = progress * ((axisAlignedBB.maxY - centerY) / max);
        double progressValZ = progress * ((axisAlignedBB.maxZ - centerZ) / max);

        AxisAlignedBB axisAlignedBB1 = new AxisAlignedBB(centerX - progressValX, centerY - progressValY, centerZ - progressValZ, centerX + progressValX, centerY + progressValY, centerZ + progressValZ);

        switch (renderType.getValue()) {
            case "Fill": {
                RenderUtil.drawBox(axisAlignedBB1, true, 0, fillColor, GeometryMasks.Quad.ALL);
                break;
            }
            case "Outline": {
                RenderUtil.drawBoundingBox(axisAlignedBB1, lineWidth.getValue(), outlineColor);
                break;
            }
            case "None": {

            }
            default: {
                RenderUtil.drawBox(axisAlignedBB1, true, 0, fillColor, GeometryMasks.Quad.ALL);
                RenderUtil.drawBoundingBox(axisAlignedBB1, lineWidth.getValue(), outlineColor);
                break;
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<DrawBlockDamageEvent> drawBlockDamageEventListener = new Listener<>(event -> {
        if (cancelAnimation.getValue()) {
            event.cancel();
        }
    });
}