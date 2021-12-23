package com.gamesense.client.module.modules.misc;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.gamesense.api.event.events.SendMessageEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.StringSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.misc.WebsocketClientEndpoint;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Module.Declaration(name = "IRC", category = Category.Misc)
public class IRC extends Module {

    ColorSetting sideColor = registerColor("SideColor", new GSColor(255, 0, 0));
    BooleanSetting alwaysIRC = registerBoolean("Always IRC", false);
    StringSetting bindIRC = registerString("Bind IRC", "#");
    BooleanSetting addgs = registerBoolean("Add gs", true);

    static boolean finish;
    static final Object syn = new Object();

    WebsocketClientEndpoint clientEndPoint;


    @Override
    public void onUpdate() {
        if (clientEndPoint == null) {
            finish = true;

            try {
                // open websocket
                clientEndPoint = new WebsocketClientEndpoint(new URI("wss://hack.chat/chat-ws"));

                // send message to websocket
                clientEndPoint.sendMessage(getCreateChatroomRequest("MC_" + mc.player.getName()));


                // add listener
                clientEndPoint.addMessageHandler(message -> {
                    JsonObject convertedObject = new Gson().fromJson(message, JsonObject.class);

                    String textMSG;
                    String name;
                    switch (convertedObject.get("cmd").getAsString()) {
                        case "onlineSet":
                            JsonArray online = convertedObject.get("nicks").getAsJsonArray();
                            StringBuilder on = new StringBuilder();
                            for(int i = 0; i < online.size(); i++) {
                                if (!online.get(i).equals("server"))
                                    on.append(online.get(i)).append(" ");
                            }
                            textMSG = ChatFormatting.BOLD + "" + ChatFormatting.AQUA + "IRC "+ ChatFormatting.RESET + " players online: " + on.toString();
                            if (addgs.getValue())
                                MessageBus.sendClientPrefixMessage(textMSG);
                            else MessageBus.sendClientRawMessage(textMSG);

                            break;

                        case "onlineRemove":
                            name = convertedObject.get("nick").toString();
                            if (name.contains("server"))
                                return;
                            textMSG = ChatFormatting.BOLD + "" + ChatFormatting.AQUA + "IRC "+ ChatFormatting.RESET + name + " left the irc";
                            if (addgs.getValue())
                                MessageBus.sendClientPrefixMessage(textMSG);
                            else MessageBus.sendClientRawMessage(textMSG);
                            break;

                        case "onlineAdd":
                            name = convertedObject.get("nick").toString();
                            if (name.equals("server"))
                                return;
                            textMSG = ChatFormatting.BOLD + "" + ChatFormatting.AQUA + "IRC "+ ChatFormatting.RESET + name + " joined the irc";
                            if (addgs.getValue())
                                MessageBus.sendClientPrefixMessage(textMSG);
                            else MessageBus.sendClientRawMessage(textMSG);
                            break;

                        case "chat":
                            String text = convertedObject.get("text").getAsString();
                            String[] values = text.split(":");
                            String realAuthor;
                            String realMSG;
                            if (values.length == 1) {
                                realAuthor = convertedObject.get("nick").getAsString();
                                realMSG = text;
                            } else {
                                realAuthor = values[0];
                                realMSG = text.substring(text.indexOf(':'));
                            }

                            if (realAuthor.equals("server"))
                                return;

                            textMSG = ChatFormatting.BOLD + "" + ChatFormatting.AQUA + "IRC " + ChatFormatting.RESET + realAuthor + realMSG;

                            if (addgs.getValue())
                                MessageBus.sendClientPrefixMessage(textMSG);
                            else MessageBus.sendClientRawMessage(textMSG);
                            break;
                        case "warn":
                            if (convertedObject.get("text").getAsString().equals("Nickname taken")) {
                                tries++;
                                clientEndPoint.sendMessage(getCreateChatroomRequest("MC_" + mc.player.getName()));
                            }
                            break;
                    }

                    /*
                    JsonElement a = convertedObject.get("data");
                    if (!a.isJsonArray()) {
                        String realMSG = convertedObject.get("data").getAsJsonObject().get("message").getAsString();
                        String realName = convertedObject.get("data").getAsJsonObject().get("user").getAsJsonObject().get("name").getAsString();

                        if (!realMSG.equals("update")) {

                            //System.out.println(realName + " : " + realMSG);
                            String text = ChatFormatting.BOLD + "" + ChatFormatting.AQUA + "IRC " + ChatFormatting.RESET + realName + ": " + realMSG;

                            if (addgs.getValue())
                                MessageBus.sendClientPrefixMessage(text);
                            else MessageBus.sendClientRawMessage(text);


                        }
                    }*/
                });

            }catch (URISyntaxException ignored) {

            }
        } else if (clientEndPoint.getUserSession() == 0)
            clientEndPoint = null;
    }

    @Override
    protected void onDisable() {
        finish = false;
        clientEndPoint.close();
        clientEndPoint = null;
    }

    int tries = 0;

    String getCreateChatroomRequest(String name) {
        return "{\"cmd\":\"join\",\"channel\":\"gs\",\"nick\":\""+name+ (tries == 0 ? "" : String.valueOf(tries)) + "\"}";
    }
    String getSendMessageRequest(String name, String message) {
        return "{\"cmd\":\"chat\",\"text\":\""+name + ":" + message+"\"}";
    }

    String getUUID(String name) {
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    result.append(line);
                }
            }
            String str = result.toString();
            if (str.equals(""))
                return "";
            else {
                JsonObject convertedObject = new Gson().fromJson(str, JsonObject.class);
                JsonElement value = convertedObject.get("id");
                if (value.isJsonNull())
                    return "";
                else return value.getAsString();

            }
        } catch (IOException ignored) {
            return "error";
        }
    }

    String getUUID() {
        // 	https://heroku-temp-chat-server.herokuapp.com/uuid
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL("https://heroku-temp-chat-server.herokuapp.com/uuid");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    result.append(line);
                }
            }
            String str = result.toString();
            if (str.equals(""))
                return "";
            else {
                JsonObject convertedObject = new Gson().fromJson(str, JsonObject.class);
                JsonElement value = convertedObject.get("uuid");
                if (value.isJsonNull())
                    return "";
                else return value.getAsString();

            }
        } catch (IOException e){
            return "";
        }

    }

    String realNameMsg;

    void sendPicture(String realName) {
        realNameMsg = realName;
        Thread t = new Thread(() -> {
            String imageURL = "https://crafatar.com/avatars/" + getUUID(realNameMsg).replaceAll("-", "") + "?size=64&default=MHF_Steve&overlay";

            String url = "https://discord.com/api/webhooks/906976095901986846/6mtRpFDzCEWuwTuuvWPfRywdcPPWtBWTjOlWxtusaC2gABELZ7N4Zr3_nQX8XwuFPYVz";
            WebhookClient client = WebhookClient.withUrl(url);

            if (Files.exists(Paths.get("screenshots"))) {

                File uploadFile1 = getLastModified(Paths.get("screenshots").toAbsolutePath().toString());
                WebhookMessage msg = new WebhookMessageBuilder()
                        .addFile(uploadFile1)
                        .setAvatarUrl(imageURL)
                        .setUsername(realNameMsg)
                        .build();

                client.send(msg);
            }
        });
        t.start();

    }

    public static File getLastModified(String directoryFilePath)
    {
        File directory = new File(directoryFilePath);
        File[] files = directory.listFiles(File::isFile);
        long lastModifiedTime = Long.MIN_VALUE;
        File chosenFile = null;

        if (files != null)
        {
            for (File file : files)
            {
                if (file.lastModified() > lastModifiedTime)
                {
                    chosenFile = file;
                    lastModifiedTime = file.lastModified();
                }
            }
        }

        return chosenFile;
    }


    Map<String, String> map = Stream.of(new String[][] {
            { "@doogie", "<@467346196873347082>" },
            { "@techale", "<@185754779258060802>" },
            { "@aven", "<@584363189890711562>"},
            { "@mwa", "<@679115407751381014>"},
            { "@phantom", "<@345816471949017089>"},
            { "@soggy", "<@254741747832324096>"},
            { "@sable", "<@901783136462065665>"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    String msgSend;

    // When recived a message
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<SendMessageEvent> chatReceivedEventListener = new Listener<>(event -> {
        String msg = event.getMessage();

        if (clientEndPoint == null)
            return;

        if (alwaysIRC.getValue() || msg.startsWith(bindIRC.getText().length() > 0 ? String.valueOf(bindIRC.getText().charAt(0)) : "")) {
            if (!alwaysIRC.getValue())
                msg = msg.substring(1);

            for(String key : map.keySet()) {
                msg = msg.replace(key, map.get(key));
            }

            if (msg.equals("img")) {
                sendPicture(mc.player.getName());
            } else
                synchronized (syn) {
                    msgSend = msg;
                    Thread t = new Thread() {
                        public void run() {
                            clientEndPoint.sendMessage(getSendMessageRequest(mc.player.getName(), msgSend));
                            String imageURL = "https://crafatar.com/avatars/" + getUUID(mc.player.getName()).replaceAll("-", "") + "?size=64&default=MHF_Steve&overlay";

                            String url = "https://discord.com/api/webhooks/906976095901986846/6mtRpFDzCEWuwTuuvWPfRywdcPPWtBWTjOlWxtusaC2gABELZ7N4Zr3_nQX8XwuFPYVz";
                            WebhookClient client = WebhookClient.withUrl(url);

                            WebhookMessage realMSG = new WebhookMessageBuilder()
                                    .setAvatarUrl(imageURL)
                                    .setUsername(mc.player.getName())
                                    .addEmbeds(new WebhookEmbed(null, sideColor.getColor().getRGB(), msgSend, null, null, null, null, null,
                                            new ArrayList<>()))
                                    .build();

                            client.send(realMSG);
                        }
                    };
                    t.start();

                }
            event.cancel();
        }
    });



}