package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;

@Module.Declaration(name = "TickShift", category = Category.Movement)
public class TickShift extends Module {

    IntegerSetting limit = registerInteger("Limit", 16,1,50);
    DoubleSetting timer = registerDouble("Timer", 2,1,5);

    int ticks;

    @Override
    protected void onEnable() {
        mc.timer.tickLength = 50;
        ticks = 0;
    }

    @Override
    protected void onDisable() {
        mc.timer.tickLength = 50;
    }

    @Override
    public void onUpdate() {

        if (isMoving()) { // garunteed movement packet

            if (ticks > 0) {
                mc.timer.tickLength = 50 / timer.getValue().floatValue();
                ticks--;
            }

        } else {

            mc.timer.tickLength = 50;
            if (ticks < limit.getValue())
                ticks++;
        }

    }

    @Override
    public String getHudInfo() {
        // we make it red when 0 and green when at limit, in between is grey

        if (ticks == 0)
            return ChatFormatting.WHITE + "["+ ChatFormatting.RED + ticks + ChatFormatting.WHITE + "]";

        if (ticks == limit.getValue())
            return ChatFormatting.WHITE + "["+ ChatFormatting.GREEN + ticks + ChatFormatting.WHITE + "]";


        return ChatFormatting.WHITE + "["+ ChatFormatting.GRAY + ticks + ChatFormatting.WHITE + "]";
    }

    boolean isMoving() {

        return MotionUtil.getMotion(mc.player) + Math.abs(mc.player.posY - mc.player.prevPosY) != 0;

    }

}
