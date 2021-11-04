/*
package com.gamesense.client.module.modules.hud;

import com.gamesense.client.clickgui.GameSenseGUI;
import com.gamesense.client.module.HUDModule;
import com.lukflug.panelstudio.hud.HUDList;
import com.lukflug.panelstudio.hud.ListComponent;
import com.lukflug.panelstudio.setting.Labeled;
import com.lukflug.panelstudio.theme.ITheme;
import net.minecraft.client.Minecraft;

import java.awt.*;

@HUDModule.Declaration(posX = 0, posZ = 0)
public class Frames extends HUDModule {
    @Override
    public void populate(ITheme theme) {

        component = new ListComponent(new Labeled(getName(), null, () -> true), position, getName(), new FramesList(), GameSenseGUI.FONT_HEIGHT, HUDModule.LIST_BORDER);

    }

    private static class FramesList implements HUDList {

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public String getItem(int i) {
            return String.valueOf(Minecraft.getDebugFPS());
        }

        @Override
        public Color getItemColor(int i) {
            return Color.WHITE;
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
}*/
