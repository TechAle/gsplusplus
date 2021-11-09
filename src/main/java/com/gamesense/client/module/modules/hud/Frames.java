package com.gamesense.client.module.modules.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
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

@Module.Declaration(name = "Frames", category = Category.HUD)
@HUDModule.Declaration(posX = 0, posZ = 0)
public class Frames extends HUDModule {

    BooleanSetting sortUp = registerBoolean("Sort Up", true);
    BooleanSetting sortRight = registerBoolean("Sort Right", false);

    private final FrameList list = new FrameList();

    @Override
    public void populate(ITheme theme) {
        component = new ListComponent(new Labeled(getName(),null,()->true), position, getName(), list, GameSenseGUI.FONT_HEIGHT, HUDModule.LIST_BORDER);
    }

    private class FrameList implements HUDList {

        public List<Module> activeModules = new ArrayList<Module>();

        @Override
        public int getSize() {
            return activeModules.size();
        }

        @Override
        public String getItem(int index) {
            try {
                return String.valueOf(mc.fpsCounter) + " FPS";
            } catch (Exception e) {
                return 0 + " FPS";
            }
        }

        @Override
        public Color getItemColor(int index) {
            return Color.WHITE;
        }

        @Override
        public boolean sortUp() {
            return sortUp.getValue();
        }

        @Override
        public boolean sortRight() {
            return sortRight.getValue();
        }
    }
}