package com.gamesense.client.command.commands;

import com.gamesense.api.config.ProfileManager;
import com.gamesense.api.config.SaveConfig;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.command.Command;

/**
 * @author Hoosiers
 * @since 1/1/2020
 */

@Command.Declaration(name = "SaveConfig", syntax = "saveconfig", alias = {"saveconfig", "reloadconfig", "saveconfiguration"})
public class SaveConfigCommand extends Command {

    public void onCommand(String command, String[] message) {
        SaveConfig.setProfile(ProfileManager.getCurrentProfile());
        SaveConfig.init();
        MessageBus.sendCommandMessage("Config saved!", true);
    }
}