/*
package com.gamesense.client.module.modules.hud;

import com.gamesense.client.clickgui.GameSenseGUI;
import com.gamesense.client.module.HUDModule;
import com.lukflug.panelstudio.hud.HUDList;
import com.lukflug.panelstudio.hud.ListComponent;
import com.lukflug.panelstudio.setting.Labeled;
import com.lukflug.panelstudio.theme.ITheme;

import java.awt.*;

@Module.Declaration(name = "TPS", category = Category.HUD)
@HUDModule.Declaration(posX = 0, posZ = 0)
public class TPS extends HUDModule {
    private final TPSList list = new TPSList();

    @Override
    public void populate(ITheme theme) {
        component = new ListComponent(new Labeled(getName(),null,()->true), position, getName(), list, GameSenseGUI.FONT_HEIGHT, HUDModule.LIST_BORDER);
    }

    private class TPSList implements HUDList {

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public String getItem(int index) {
            try {
                return "TPS: " + mc.serverName;
            } catch (Exception e) {
                return "TPS: null";
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
*/
