package com.gamesense.api.config;

import com.gamesense.api.setting.Setting;
import com.gamesense.api.setting.SettingsManager;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.client.GameSense;
import com.gamesense.client.clickgui.GameSenseGUI;
import com.gamesense.client.clickgui.GuiConfig;
import com.gamesense.client.command.CommandManager;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.misc.AutoGG;
import com.gamesense.client.module.modules.misc.AutoReply;
import com.gamesense.client.module.modules.misc.AutoRespawn;
import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Hoosiers
 * @since 10/15/2020
 */

public class ProfileManager {

    public static String fileName = "gs++/";
    private static final String moduleName = "Modules/";
    private static final String mainName = "Main/";
    private static final String miscName = "Misc/";
    private static final String profilesPath = "profiles/";

    private static String currentProfile = "";

    public static void init() {
        try {
            currentProfile = loadCurrentProfile();
            LoadConfig.setProfile(currentProfile);
            SaveConfig.setProfile(currentProfile);
            LoadConfig.init();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void createConfig(String configName) {
        currentProfile = configName;
        try {
            saveCurrentProfile();
            saveConfig();
            SaveConfig.setProfile(currentProfile);
            SaveConfig.init();

        } catch (IOException e) {
            e.printStackTrace();
        }
        GameSense.LOGGER.info("Created new config "+currentProfile);
    }

    public static Collection<String> getProfiles() throws IOException{

        ArrayList<String> profiles = new ArrayList<>();


        Path dir = Paths.get(fileName+profilesPath);
        Files.walk(dir).forEach(path -> {
            if(path.toFile().isDirectory())  profiles.add(path.toFile().getName());
        });

        return profiles;
    }

    private static void saveConfig() throws IOException {

        if(currentProfile.equals("")) return;

        if (!Files.exists(Paths.get(fileName + profilesPath))) {
            Files.createDirectories(Paths.get(fileName + profilesPath));
        }
        if (!Files.exists(Paths.get(fileName + profilesPath + currentProfile))) {
            Files.createDirectories(Paths.get(fileName + profilesPath + currentProfile));
        }
        if (!Files.exists(Paths.get(fileName + profilesPath + currentProfile + moduleName ))) {
            Files.createDirectories(Paths.get(fileName + profilesPath + currentProfile + moduleName));
        }
        if (!Files.exists(Paths.get(fileName + profilesPath + mainName))) {
            Files.createDirectories(Paths.get(fileName + profilesPath + currentProfile + mainName));
        }
        if (!Files.exists(Paths.get(fileName + profilesPath + miscName))) {
            Files.createDirectories(Paths.get(fileName + profilesPath + currentProfile + miscName));
        }
        GameSense.LOGGER.info("created new config directory");
    }

    private static void registerFiles(String location, String name) throws IOException {
        if (Files.exists(Paths.get(fileName + location + name + ".json"))) {
            File file = new File(fileName + location + name + ".json");

            file.delete();

        }
        Files.createFile(Paths.get(fileName + location + name + ".json"));
    }


    private static void saveCurrentProfile() throws IOException {

        registerFiles(miscName, "CurrentProfile");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        OutputStreamWriter fileOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(fileName + miscName + "CurrentProfile" + ".json"), StandardCharsets.UTF_8);
        JsonObject mainObject = new JsonObject();

        mainObject.add("Profile", new JsonPrimitive(currentProfile));
        String jsonString = gson.toJson(new JsonParser().parse(mainObject.toString()));
        fileOutputStreamWriter.write(jsonString);
        fileOutputStreamWriter.close();
    }


    private static String loadCurrentProfile() throws IOException {
        String fileLocation = fileName + miscName;

        if (!Files.exists(Paths.get(fileLocation + "CurrentProfile" + ".json"))) {
            saveCurrentProfile();
            return "";
        }

        InputStream inputStream = Files.newInputStream(Paths.get(fileLocation + "CurrentProfile" + ".json"));
        JsonObject mainObject = new JsonParser().parse(new InputStreamReader(inputStream)).getAsJsonObject();

        if (mainObject.get("Profile") == null) {
            return "";
        }

        JsonElement dataObject = mainObject.get("Profile");
        if (dataObject != null && dataObject.isJsonPrimitive()) {
            inputStream.close();
            return dataObject.getAsString();
        }

        inputStream.close();
        return "";
    }

    public static void setCurrentProfile(String newProfile) {
        currentProfile = newProfile;
        LoadConfig.setProfile(currentProfile);
        SaveConfig.setProfile(currentProfile);
        LoadConfig.init();
    }

    public static String getCurrentProfile() {
        return currentProfile;
    }
}