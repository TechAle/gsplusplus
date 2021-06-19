package com.gamesense.client.module.modules.misc;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.Discord;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.GameSense;
import net.minecraft.init.Items;

import java.util.ArrayList;
import java.util.Arrays;

@Module.Declaration(name = "DiscordRPC", category = Category.Misc, drawn = false)
public class DiscordRPCModule extends Module {

    /*
    ArrayList<String> options = new ArrayList<String>() {
        {
            add("Server");
            add("Status");
            add("Name");
            add("Health");
        }
    };*/

    static final String discordID = "840996509880680479";
    static final DiscordRichPresence discordRichPresence = new DiscordRichPresence();
    static final DiscordRPC discordRPC = DiscordRPC.INSTANCE;

    int curImg = -1;
    ModeSetting imgType = registerMode("Image", Arrays.asList("gs++", "insigna", "luk"), "gs++");
    ModeSetting lowImg = registerMode("Low Img", Arrays.asList("none", "nocatsnolife", "sable__", "phantom826", "doogie13", "soulbond", "anonymousplayer", "hoosier"), "none");
    BooleanSetting animateGs = registerBoolean("Animated gs++", true);
    IntegerSetting msChange = registerInteger("Image Change", 2000, 250, 5000);


    void updateStatus() {

    }

    public void onEnable() {
        //Discord.startRPC();
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        eventHandlers.disconnected = ((var1, var2) -> System.out.println("Discord RPC disconnected, var1: " + var1 + ", var2: " + var2));
        discordRPC.Discord_Initialize(discordID, eventHandlers, true, null);
        discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
        prevTimeImg = System.currentTimeMillis();
        updateRpc();
    }

    void updateRpc() {

        updatePicture();

        discordRPC.Discord_UpdatePresence(discordRichPresence);
    }

    void updatePicture() {
        /// Large IMG
        // Output
        String imgNow, description;
        // If gs++
        if ("gs++".equals(imgType.getValue())) {
            // If animated
            if (animateGs.getValue()) {
                // If we have to change
                if (prevTimeImg + msChange.getValue() < System.currentTimeMillis()) {
                    // Get where we are
                    int maxImg = 4;
                    curImg = curImg >= maxImg ? 0 : curImg + 1;
                    // Update time
                    prevTimeImg = System.currentTimeMillis();
                }
            // Set 0 in case of normal gs++
            } else curImg = 0;
            // Set imgNow
            imgNow = "gs" + curImg;
            description = "gs++ engine";
        } else {
            // Default
            imgNow = imgType.getValue();
            description = imgNow + " powered by gs++";
        }

        // Picture + text
        discordRichPresence.largeImageKey = imgNow;
        discordRichPresence.largeImageText = description;

        /// Small IMG
        if (!lowImg.getValue().equals("none")) {
            // Get img
            discordRichPresence.smallImageKey = lowImg.getValue();
            // Get text
            discordRichPresence.smallImageText = lowImg.getValue().equalsIgnoreCase(mc.player.getName()) ? "Confirmed user" : "Not identified user";
        }
        else discordRichPresence.smallImageKey = null;

    }

    private long prevTimeImg;

    public void onUpdate() {

        updateRpc();

    }


    private void changeStatus(boolean called) {
        if (mc.player == null) {
            discordRichPresence.state = "On the main menu";
            return;
        }
        if (mc.player.inventory.armorItemInSlot(2).getItem().equals(Items.DIAMOND_CHESTPLATE))
            discordRichPresence.state = "Fighting " + (int) PlayerUtil.getHealth() + "hp";
        else discordRichPresence.state = "Chillin around";
        discordRichPresence.details = GameSense.MODVER + " | " + mc.player.getName();
        discordRichPresence.largeImageKey = "gs" + curImg;
        if (!called)
            discordRPC.Discord_UpdatePresence(discordRichPresence);
    }


    public void onDisable() {
        //Discord.stopRPC();
        discordRPC.Discord_Shutdown();
        discordRPC.Discord_ClearPresence();
    }
}