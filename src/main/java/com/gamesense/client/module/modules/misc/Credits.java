package com.gamesense.client.module.modules.misc;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.client.GameSense;
import com.gamesense.client.command.CommandManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.modules.combat.PistonCrystal;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketChatMessage;

import java.util.Arrays;

@Module.Declaration(name = "Credits", category = Category.Misc, enabled = true)
public class Credits extends Module {


    @Override
    public void onUpdate() {
        if (mc.world == null && mc.player == null)
            return;

        PistonCrystal.printDebug(" think it's the duty of a developer to say if he took inspiration / took some piece of code from another client.\n" +
                "\n" +
                "    How customchat is implemented for overriding minecraft's chat (https://www.curseforge.com/minecraft/mc-mods/better-chat)\n" +
                "    BowExploit (https://github.com/PotatOoOoOo0/BowMcBomb)\n" +
                "    Shaders (Momentum)\n" +
                "    FootWalker (packet logged future and konas)\n" +
                "    ChorusPost (k5)\n" +
                "    Aspect (quantum)\n" +
                "    fix log exploit (https://github.com/ChloePrime/fix4log4j)\n" +
                "    AntiPing (Phobos)\n" +
                "    NewChunks (Seppuku)\n" +
                "    Trajectories (Phobos)\n" +
                "    Chams (k5)\n" +
                "    PacketLogger (w+3)\n", false);


        disable();
    }
}