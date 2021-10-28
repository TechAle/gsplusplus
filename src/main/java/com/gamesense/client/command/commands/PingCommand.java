package com.gamesense.client.command.commands;

import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.command.Command;

import java.util.Objects;

@Command.Declaration(name = "Ping", syntax = "Ping [player]", alias = {"ping", "ms", "latency", "lag"})
public class PingCommand extends Command {
    @Override
    public void onCommand(String command, String[] message) {

        String pl = message[0];

        if (!pl.equals(mc.player.getName())) {
            try {
                MessageBus.sendClientPrefixMessage(Objects.requireNonNull(mc.world.getPlayerEntityByName(pl)).getName() + " Has " + mc.getConnection().getPlayerInfo(pl).getResponseTime() + "ms");
            } catch (NullPointerException ignored) {
                MessageBus.sendClientPrefixMessage("Invalid Player");
            }
        } else {

            try {
                MessageBus.sendClientPrefixMessage("You have no idea what your ms is trol");
            } catch (NullPointerException ignored) {
                MessageBus.sendClientPrefixMessage("Invalid Player");
            }

        }
    }
}
