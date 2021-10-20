package com.gamesense.client.command.commands;

import com.gamesense.api.config.ProfileManager;
import com.gamesense.api.config.SaveConfig;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.command.Command;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @Author Hoosiers on 11/05/2020
 */

@Command.Declaration(name = "OpenFolder", syntax = "openfolder", alias = {"openfolder", "open", "folder"})
public class OpenFolderCommand extends Command {

    public void onCommand(String command, String[] message) {
        try {
            String path = ProfileManager.getCurrentProfile().equals("") ? ProfileManager.fileName.replace("/", "") :  ProfileManager.fileName + "profiles/" + ProfileManager.getCurrentProfile();
            Desktop.getDesktop().open(new File(path));
            MessageBus.sendCommandMessage("Opened config folder!", true);
        } catch (IOException e) {
            MessageBus.sendCommandMessage("Could not open config folder!", true);
            e.printStackTrace();
        }
    }
}