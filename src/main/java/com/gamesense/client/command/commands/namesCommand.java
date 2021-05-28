package com.gamesense.client.command.commands;

import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.client.command.Command;

/**
 * @Author Hoosiers on 11/05/2020
 */

@Command.Declaration(name = "Names", syntax = "names list/add/del [player]", alias = {"names", "name", "specialname"})
public class namesCommand extends Command {

    public void onCommand(String command, String[] message) {
        String main = message[0];

        if (main.equalsIgnoreCase("list")) {
            MessageBus.sendClientPrefixMessage("Names: " + SocialManager.getSpecialNamesString() + "!");
            return;
        }

        String value = message[1];

        if (main.equalsIgnoreCase("add") && !SocialManager.isSpecial(value)) {
            SocialManager.addSpecialName(value);
            MessageBus.sendCommandMessage("Added name: " + value.toUpperCase() + "!", true);
        } else if (main.equalsIgnoreCase("del") && SocialManager.isSpecial(value)) {
            SocialManager.delSpecial(value);
            MessageBus.sendCommandMessage("Deleted name: " + value.toUpperCase() + "!", true);
        }
    }
}