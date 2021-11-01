package com.gamesense.client.command.commands;

import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.social.Friend;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.client.command.Command;

/**
 * @Author Hoosiers on 11/05/2020
 */

@Command.Declaration(name = "Friend", syntax = "friend list/add/del [player]", alias = {"friend", "friends", "f"})
public class FriendCommand extends Command {

    public void onCommand(String command, String[] message) {
        String main = message[0];

        if (main.equalsIgnoreCase("list")) {
            MessageBus.sendClientPrefixMessage("Friends: " + SocialManager.getFriendsByName() + "!");
            return;
        }

        String value = message[1];

        if (main.equalsIgnoreCase("add") && !isFriend(value)) {
            SocialManager.addFriend(value);
            MessageBus.sendCommandMessage("Added friend: " + value.toUpperCase() + "!", true);
        } else if (main.equalsIgnoreCase("del") && SocialManager.isFriend(value)) {
            SocialManager.delFriend(value);
            MessageBus.sendCommandMessage("Deleted friend: " + value.toUpperCase() + "!", true);
        }
    }

    boolean isFriend(String name) {

        for (Friend friend : SocialManager.getFriends()) {
            if (friend.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

}