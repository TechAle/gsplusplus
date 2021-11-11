package com.gamesense.api.util.misc;

import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.hud.Notifications;
import com.gamesense.client.module.modules.misc.ChatModifier;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.concurrent.ConcurrentException;

import java.util.ConcurrentModificationException;

/**
 * @author Hoosiers
 * @since 11/04/2020
 */

public class MessageBus {

    public static String watermark = ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + "g" + ChatFormatting.GREEN + "s++" + ChatFormatting.GRAY + "] " + ChatFormatting.RESET;
    public static ChatFormatting messageFormatting = ChatFormatting.GRAY;

    protected static final Minecraft mc = Minecraft.getMinecraft();

    /**
     * Sends a client-sided message WITH the client prefix
     **/
    public static void sendClientPrefixMessage(String message) {
        sendClientPrefixMessageWithID(message, 0);
    }

    public static void sendClientPrefixMessageWithID(String message, boolean generateID) {
        if (!generateID){
            sendClientPrefixMessageWithID(message, 0);
        } else {
            sendClientPrefixMessageWithID(message, Module.getIdFromString(message));
        }
    }

    public static void sendClientPrefixMessageWithID(String message, int id) {
        ChatModifier chat = ModuleManager.getModule(ChatModifier.class);
        TextComponentString string1 = new TextComponentString(
                (   chat.isEnabled() &&
                        chat.watermarkSpecial.getValue() ? "\u2063[gs++]" : watermark)
                        + messageFormatting + message);
        TextComponentString string2 = new TextComponentString(messageFormatting + message);

        Notifications notifications = ModuleManager.getModule(Notifications.class);
        notifications.addMessage(string2);
        if (notifications.isEnabled() && notifications.disableChat.getValue()) {
            return;
        }
        try {
            if (mc.player != null && mc.world != null) {
                mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(string1, id);
            }
        }catch (ConcurrentModificationException ignored) {

        }
    }

    /**
     * Command-oriented message, with the nature of commands we don't want them being a notification
     **/
    public static void sendCommandMessage(String message, boolean prefix) {
        String watermark1 = prefix ? watermark : "";
        TextComponentString string = new TextComponentString(watermark1 + messageFormatting + message);

        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(string, Module.getIdFromString(message));
    }

    /**
     * @Unused Sends a client-sided message WITHOUT the client prefix
     **/
    public static void sendClientRawMessage(String message) {
        TextComponentString string = new TextComponentString(messageFormatting + message);

        Notifications notifications = ModuleManager.getModule(Notifications.class);
        notifications.addMessage(string);
        if (ModuleManager.isModuleEnabled(Notifications.class) && notifications.disableChat.getValue()) {
            return;
        }
        mc.player.sendMessage(string);
    }

    /**
     * Sends a server-sided message
     **/
    public static void sendServerMessage(String message) {
        mc.player.connection.sendPacket(new CPacketChatMessage(message));
    }
}