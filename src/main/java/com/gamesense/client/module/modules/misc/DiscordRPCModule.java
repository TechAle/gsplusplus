package com.gamesense.client.module.modules.misc;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.misc.Discord;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.GameSense;
import net.minecraft.init.Items;

@Module.Declaration(name = "DiscordRPC", category = Category.Misc, drawn = false)
public class DiscordRPCModule extends Module {

    private static final String discordID = "840996509880680479";
    private static final DiscordRichPresence discordRichPresence = new DiscordRichPresence();
    private static final DiscordRPC discordRPC = DiscordRPC.INSTANCE;
    private int curImg = -1;
    private final int maxImg = 4;
    IntegerSetting msChange = registerInteger("Image Change", 2000, 250, 5000);
    IntegerSetting hpChange = registerInteger("Hp Change", 100, 50, 2000);

    public void onEnable() {
        //Discord.startRPC();
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers();
        eventHandlers.disconnected = ((var1, var2) -> System.out.println("Discord RPC disconnected, var1: " + var1 + ", var2: " + var2));
        discordRPC.Discord_Initialize(discordID, eventHandlers, true, null);
        discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
        discordRichPresence.largeImageText = "gs++";
        prevTimeImg = prevTimeHp = System.currentTimeMillis();
        changeImage();
    }

    private long prevTimeImg;
    private long prevTimeHp;

    public void onUpdate() {
        if (prevTimeImg + msChange.getValue() < System.currentTimeMillis()) {
            changeImage();
            prevTimeImg = System.currentTimeMillis();
        }
        if (prevTimeHp + hpChange.getValue() < System.currentTimeMillis()) {
            changeStatus(false);
            prevTimeImg = System.currentTimeMillis();
        }
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

    private void changeImage() {
        changeStatus(true);
        curImg = curImg >= maxImg ? 0 : curImg + 1;
        discordRichPresence.largeImageKey = "gs" + curImg;
        discordRPC.Discord_UpdatePresence(discordRichPresence);
    }

    public void onDisable() {
        //Discord.stopRPC();
        discordRPC.Discord_Shutdown();
        discordRPC.Discord_ClearPresence();
    }
}