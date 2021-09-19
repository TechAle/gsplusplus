package com.gamesense.client.command.commands;

import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.command.Command;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import net.minecraft.network.play.client.CPacketPlayer;

@Command.Declaration(name = "VerticalClip", syntax = "VClip [Distance]", alias = {"vclip", "vc", "yclip", "yc"})
public class VClipCommand extends Command {

    double amount;

    @Override
    public void onCommand(String command, String[] message) {
        if (mc.player != null){
            String main = message[0];

            try {
                amount = Double.parseDouble(main);
                MessageBus.sendCommandMessage(ModuleManager.getModule(ColorMain.class).getEnabledColor() + "Clipped the player " + main + " blocks up", true);

            } catch (NumberFormatException e) {
                MessageBus.sendCommandMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You moron, you absolute buffoon, how do you mess up entering a number into a command, you philistine!", true);
                return;
            }
            mc.player.setPositionAndUpdate(mc.player.posX, mc.player.posY + amount, mc.player.posZ);
        }
    }
}
