package com.gamesense.client.command.commands;

import com.gamesense.client.command.Command;
import net.minecraft.network.play.client.CPacketPlayer;

@Command.Declaration(name = "Teleport", syntax = "tp [x] [y] [z]", alias = {"tp", "teleport", "clipto"})
public class TeleportCommand extends Command {
    @Override
    public void onCommand(String command, String[] message) {
        try {
            String x = message[0];
            String y = message[1];
            String z = message[2];

            int xp = Integer.parseInt(x);
            int yp = Integer.parseInt(y);
            int zp = Integer.parseInt(z);

            if (mc.player.ridingEntity == null)
                mc.player.setPositionAndUpdate(xp,yp,zp);
            else
                mc.player.ridingEntity.setPosition(xp,yp,zp);

            mc.player.connection.sendPacket(new CPacketPlayer.Position(xp,yp,zp,false));

        } catch (Exception ignored) {}
    }
}
