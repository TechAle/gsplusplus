package com.gamesense.client.command.commands;


import com.gamesense.api.config.ProfileManager;
import com.gamesense.api.config.SaveConfig;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.GameSense;
import com.gamesense.client.command.Command;
import com.gamesense.client.command.CommandManager;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.io.IOException;

@Command.Declaration(name = "Config", syntax = "config add/list/load/remove configName", alias = {"config", "profile"})
public class ConfigCommand extends Command {

    @Override
    public void onCommand(String command, String[] message) {
        switch(message[0]){

            case "list":
                TextComponentString msg = new TextComponentString("\2477Profiles: " + "\247f ");


                int size = ProfileManager.getProfiles().size();
                int index = 0;
                for (String profile : ProfileManager.getProfiles()){
                    if(profile.equals("")) profile = "default";
                    msg.appendSibling(new TextComponentString((ProfileManager.getCurrentProfile().equals(profile) ? ChatFormatting.GREEN : ChatFormatting.RED) + profile + "\2477" + ((index == size - 1) ? "" : ", ")));
                            //.setStyle(new Style()
                            //        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Load "+profile)))
                            //        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, CommandManager.getCommandPrefix() + "config load" + " " + profile)));

                    index++;
                }
                msg.appendSibling(new TextComponentString(ChatFormatting.GRAY + "!"));
                mc.ingameGUI.getChatGUI().printChatMessage(msg);



                break;

           case "create":
           case "new":
           case "add":
                GameSense.LOGGER.info("attempting to add config " + message[1]);

                if (ProfileManager.getProfiles().contains(message[1])) {
                    MessageBus.sendCommandMessage("Profile "+message[1]+" already exists, please choose a different profile name", true);
                    break;
                }

                ProfileManager.createConfig(message[1]);
                MessageBus.sendCommandMessage("Created profile "+message[1], true);

               break;

           case "load":
                GameSense.LOGGER.info("attempting to load config " + message[1]);

               if (!ProfileManager.getProfiles().contains(message[1])) MessageBus.sendCommandMessage("Profile not found. (dont use spaces btw)", true);
               else{
                   ProfileManager.setCurrentProfile(message[1]);
                   MessageBus.sendCommandMessage("Loaded profile "+message[1], true);
               }

                break;

           case "save":
               SaveConfig.init();
               MessageBus.sendCommandMessage("Saved profile "+message[1], true);
               break;

           case "remove":
           case "delete":
            case "del":
                if(message[1].equals("default")){
                    MessageBus.sendClientPrefixMessage("You cannot delete the default profile! dumbass");
                }
                else {
                    if(ProfileManager.getProfiles().contains(message[1])) {
                        ProfileManager.removeProfile(message[1]);
                        MessageBus.sendClientPrefixMessage("Removed profile " + message[1]);
                    }
                }
               break;
        }

    }

}
