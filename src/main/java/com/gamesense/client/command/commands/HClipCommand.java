package com.gamesense.client.command.commands;

import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.command.Command;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import com.gamesense.api.util.world.MotionUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;

@Command.Declaration(name = "HorizontalClip", syntax = "HClip [Distance]", alias = {"hclip", "hc", "forward", "fwd", "chineseComunistParty"})
public class HClipCommand extends Command {

    double amount;

    @Override
    public void onCommand(String command, String[] message) {

        if (mc.player != null) {

            String main = message[0];

            try {
                amount = Double.parseDouble(main);
                if (amount >= 0){
                    MessageBus.sendCommandMessage(ModuleManager.getModule(ColorMain.class).getEnabledColor() + "Clipped the player " + amount + " blocks forward.", true);
                } else
                    MessageBus.sendCommandMessage(ModuleManager.getModule(ColorMain.class).getEnabledColor() + "Clipped the player " + -amount + " blocks backward.", true);

                final Vec3d dir = new Vec3d(Math.cos((mc.player.rotationYaw + 90f) * Math.PI / 180.0f), 0, Math.sin((mc.player.rotationYaw + 90f) * Math.PI / 180.0f));

                mc.player.setPosition(mc.player.posX + dir.x * amount, mc.player.posY, mc.player.posZ + dir.z * amount);

            } catch (NumberFormatException e) {
                MessageBus.sendCommandMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You moron, you absolute buffoon, how do you mess up entering a number into a command, you philistine!", true);
            }
        }
    }
}
