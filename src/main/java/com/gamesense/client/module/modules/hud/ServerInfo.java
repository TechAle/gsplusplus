package com.gamesense.client.module.modules.hud;

import com.gamesense.client.clickgui.GameSenseGUI;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.HUDModule;
import com.gamesense.client.module.Module;
import com.lukflug.panelstudio.hud.HUDList;
import com.lukflug.panelstudio.hud.ListComponent;
import com.lukflug.panelstudio.setting.Labeled;
import com.lukflug.panelstudio.theme.ITheme;

import java.awt.*;

@Module.Declaration(name = "ServerInfo", category = Category.HUD)
@HUDModule.Declaration(posX = 0, posZ = 0)
public class ServerInfo extends HUDModule {
    private final IPList list = new IPList();

    @Override
    public void populate(ITheme theme) {
        component = new ListComponent(new Labeled(getName(),null,()->true), position, getName(), list, GameSenseGUI.FONT_HEIGHT, HUDModule.LIST_BORDER);
    }

    private static class IPList implements HUDList {

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public String getItem(int index) {
            try {
                return "IP: " + mc.serverName;
            } catch (Exception e) {
                return "IP: null";
            }
        }

        @Override
        public Color getItemColor(int index) {
            return Color.WHITE;
        }

        @Override
        public boolean sortUp() {
            return true;
        }

        @Override
        public boolean sortRight() {
            return true;
        }
    }
}
