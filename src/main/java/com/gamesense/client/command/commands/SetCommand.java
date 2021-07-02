package com.gamesense.client.command.commands;

import com.gamesense.api.setting.SettingsManager;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.command.Command;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;

/**
 * @Author Hoosiers on 8/4/2020
 * @Ported and modified on 11/05/2020
 */

@Command.Declaration(name = "Set", syntax = "set [module] [setting] value (no color support) module space = _ + remove setting space", alias = {"set", "setmodule", "changesetting", "setting"})
public class SetCommand extends Command {

    public void onCommand(String command, String[] message) {
        String main = message[0];

        Module module = ModuleManager.getModule(main.replace("_", " "));

        if (module == null) {
            MessageBus.sendCommandMessage(this.getSyntax(), true);
            return;
        }

        SettingsManager.getSettingsForModule(module).stream().filter(setting -> setting.getConfigName().equalsIgnoreCase(message[1].replace("_", " "))).forEach(setting -> {
            if (setting instanceof BooleanSetting) {
                if (message[2].equalsIgnoreCase("true") || message[2].equalsIgnoreCase("false")) {
                    setting.setValue(Boolean.parseBoolean(message[2]));
                    MessageBus.sendCommandMessage(module.getName() + " " + setting.getConfigName() + " set to: " + setting.getValue() + "!", true);
                } else {
                    MessageBus.sendCommandMessage(this.getSyntax(), true);
                }
            } else if (setting instanceof IntegerSetting) {
                setting.setValue(Integer.parseInt(message[2]));
                MessageBus.sendCommandMessage(module.getName() + " " + setting.getConfigName() + " set to: " + setting.getValue() + "!", true);
            } else if (setting instanceof DoubleSetting) {
                setting.setValue(Double.parseDouble(message[2]));
                MessageBus.sendCommandMessage(module.getName() + " " + setting.getConfigName() + " set to: " + setting.getValue() + "!", true);
            } else if (setting instanceof ModeSetting) {
                if (!((ModeSetting) setting).getModes().contains(message[2])) {
                    MessageBus.sendCommandMessage(this.getSyntax(), true);
                } else {
                    setting.setValue(message[2]);
                    MessageBus.sendCommandMessage(module.getName() + " " + setting.getConfigName() + " set to: " + setting.getValue() + "!", true);
                }
            } else {
                MessageBus.sendCommandMessage(this.getSyntax(), true);
            }
        });
    }
}