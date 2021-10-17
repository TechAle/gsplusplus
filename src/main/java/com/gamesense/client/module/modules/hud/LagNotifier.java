package com.gamesense.client.module.modules.hud;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.clickgui.GameSenseGUI;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.HUDModule;
import com.gamesense.client.module.Module;
import com.lukflug.panelstudio.hud.HUDList;
import com.lukflug.panelstudio.hud.ListComponent;
import com.lukflug.panelstudio.setting.Labeled;
import com.lukflug.panelstudio.theme.ITheme;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

import java.awt.*;

@Module.Declaration(name = "LagNotifier", category = Category.HUD)
@HUDModule.Declaration(posX = 50, posZ = 50)
public class LagNotifier extends HUDModule {

    public boolean lag;
    int tmr;
    boolean lagB;

    IntegerSetting delay = registerInteger("Hide Delay Ticks", 20, 0, 60);

    public void populate(ITheme theme) {

            component = new ListComponent(new Labeled(getName(), null, () -> true), position, getName(), new LagNotifierList(), GameSenseGUI.FONT_HEIGHT, HUDModule.LIST_BORDER);
    }



    @EventHandler
    private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (event.getPacket() instanceof SPacketPlayerPosLook){

            lagB = true;
            tmr = 0;
            
        }
    });

    @Override
    public void onUpdate() {

        tmr++;
        lag = tmr < delay.getValue();
    }

    private class LagNotifierList implements HUDList {

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public String getItem(int index) {
            if (lag) {
                return "Rubberband Detected [" + (delay.getValue() - tmr) + "]";
            } else {
                return "";
            }
        }

        @Override
        public Color getItemColor(int index) {
            return GSColor.red;
        }

        @Override
        public boolean sortUp() {
            return false;
        }

        @Override
        public boolean sortRight() {
            return false;
        }
    }
}
