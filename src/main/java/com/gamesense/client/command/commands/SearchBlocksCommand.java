package com.gamesense.client.command.commands;

import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.command.Command;
import com.gamesense.client.module.modules.render.Search;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;

@Command.Declaration(name = "SearchBlocksCommand", syntax = "Search [block/collection] [add/del/list] [block/collection]]", alias = {"Search","blockesp", "BlockFinder"})
public class SearchBlocksCommand extends Command {

    @Override
    public void onCommand(String command, String[] message) {

        /*
        * [0] = type
        * [1] = method
        * [2] = result
        */

        if (message[0].equalsIgnoreCase("block"))
            switch (message[1].toLowerCase()) {

                case "add" :
                    if (Search.addBlock(Block.getBlockFromName(message[1])))
                        MessageBus.sendClientPrefixMessage("Added " + message[1] + " to your search list");
                    else
                        MessageBus.sendClientPrefixMessage("Search [block/collection] [add/del/list] [block/collection]]");

                case "del" :
                    if (Search.removeBlock(Block.getBlockFromName(message[1])))
                        MessageBus.sendClientPrefixMessage("Removed " + message[1] + " from your search list");
                    else
                        MessageBus.sendClientPrefixMessage("Search [block/collection] [add/del/list] [block/collection]]");

                case "list" :
                    MessageBus.sendClientPrefixMessage(ChatFormatting.BOLD + "Blocks: " + ChatFormatting.RESET + Search.getList());

                default:
                    MessageBus.sendClientPrefixMessage("\"Search [block/collection] [add/del/list] [block/collection]]\"");

            }
        else if (message[0].equalsIgnoreCase("collection")) {

            switch (message[1].toLowerCase()) {

                case "add": {

                    if (Search.addCollection(Search.getCollection(message[2])))
                        MessageBus.sendClientPrefixMessage("Added " + message[1] + "s to your search list");
                    else
                        MessageBus.sendClientPrefixMessage("Search [block/collection] [add/del/list] [block/collection]]");

                }
                case "del": {

                    if (Search.removeCollection(Search.getCollection(message[2])))
                        MessageBus.sendClientPrefixMessage("Added " + message[1] + "s to your search list");
                    else
                        MessageBus.sendClientPrefixMessage("Search [block/collection] [add/del/list] [block/collection]]");

                }
                case "list": {

                    MessageBus.sendClientPrefixMessage(ChatFormatting.BOLD + "Collections: " + ChatFormatting.RESET + Search.colList());

                }
            }

        }
    }
}
