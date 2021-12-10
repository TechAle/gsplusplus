package com.gamesense.client.module.modules.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.GameSense;
import com.gamesense.client.clickgui.GameSenseGUI;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.HUDModule;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.lukflug.panelstudio.hud.HUDList;
import com.lukflug.panelstudio.hud.ListComponent;
import com.lukflug.panelstudio.setting.Labeled;
import com.lukflug.panelstudio.theme.ITheme;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;

@Module.Declaration(name = "Frames", category = Category.HUD)
@HUDModule.Declaration(posX = 0, posZ = 0)
public class Frames extends HUDModule {

    int frames;

    private final FrameList list = new FrameList();


    @Override
    public void populate(ITheme theme) {
        component = new ListComponent(new Labeled(getName(),null,()->true), position, getName(), list, GameSenseGUI.FONT_HEIGHT, HUDModule.LIST_BORDER);
    }

    private class FrameList implements HUDList {

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public String getItem(int index) {
            try {
                return "FPS " + Minecraft.getDebugFPS();
            } catch (Exception e) {
                return "FPS " + 0;
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
            return false;
        }
    }
}
