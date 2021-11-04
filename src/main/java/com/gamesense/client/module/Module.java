package com.gamesense.client.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import com.gamesense.api.setting.values.*;
import io.netty.util.AsciiString;
import me.zero.alpine.listener.Listenable;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.SettingsManager;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.modules.gui.ColorMain;

import net.minecraft.client.Minecraft;
import scala.Int;

import javax.annotation.RegEx;

public abstract class Module implements Listenable {

    protected static final Minecraft mc = Minecraft.getMinecraft();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Declaration {
        String name();

        Category category();

        int priority() default 0;

        int bind() default Keyboard.KEY_NONE;

        boolean enabled() default false;

        boolean drawn() default true;

        boolean toggleMsg() default false;
    }

    private final String name = getDeclaration().name();
    private final Category category = getDeclaration().category();
    private final int priority = getDeclaration().priority();
    private int bind = getDeclaration().bind();
    private boolean enabled = getDeclaration().enabled();
    private boolean drawn = getDeclaration().drawn();
    private boolean toggleMsg = getDeclaration().toggleMsg();

    private Declaration getDeclaration() {
        return getClass().getAnnotation(Declaration.class);
    }

    protected void onEnable() {

    }

    public void onDisabledUpdate() {

    }

    protected void onDisable() {

    }

    public void onUpdate() {

    }

    public void onRender() {

    }

    public void onWorldRender(RenderEvent event) {

    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private String disabledMessage = name + " turned OFF!";

    public void setDisabledMessage(String message) {
        this.disabledMessage = message;
    }

    public void enable() {
        setEnabled(true);
        GameSense.EVENT_BUS.subscribe(this);
        onEnable();
        if (toggleMsg && mc.player != null)
            MessageBus.sendClientPrefixMessageWithID(ModuleManager.getModule(ColorMain.class).getEnabledColor() + name + " turned ON!", getIdFromString(name));
    }

    public void disable() {
        setEnabled(false);
        GameSense.EVENT_BUS.unsubscribe(this);
        onDisable();
        if (toggleMsg && mc.player != null)
            MessageBus.sendClientPrefixMessageWithID(ModuleManager.getModule(ColorMain.class).getDisabledColor() + disabledMessage, getIdFromString(name));
        setDisabledMessage(name + " turned OFF!");
    }

    public static int getIdFromString(String name) {

        StringBuilder s = new StringBuilder();

        name = name.replace("§", "e");

        String blacklist = "[^a-z]";

        for (int i = 0; i < name.length(); i++)
            s.append(Integer.parseInt(String.valueOf(name.charAt(i)).replaceAll(blacklist,"e"), 36));

        try {
            s = new StringBuilder(s.substring(0, 8));
        } catch (StringIndexOutOfBoundsException ignored) {
            s = new StringBuilder(Integer.MAX_VALUE);
        }

        return Integer.MAX_VALUE - Integer.parseInt(s.toString().toLowerCase());

    }

    public void toggle() {
        if (isEnabled()) {
            disable();
        } else if (!isEnabled()) {
            enable();
        }
    }

    public String getName() {
        return this.name;
    }

    public Category getCategory() {
        return this.category;
    }

    public int getPriority() {
        return priority;
    }

    public int getBind() {
        return this.bind;
    }

    public void setBind(int bind) {
        if (bind >= 0 && bind <= 255) {
            this.bind = bind;
        }
    }

    public String getHudInfo() {
        return "";
    }

    public boolean isDrawn() {
        return this.drawn;
    }

    public void setDrawn(boolean drawn) {
        this.drawn = drawn;
    }

    public boolean isToggleMsg() {
        return this.toggleMsg;
    }

    public void setToggleMsg(boolean toggleMsg) {
        this.toggleMsg = toggleMsg;
    }

    protected IntegerSetting registerInteger(String name, int value, int min, int max) {
        IntegerSetting integerSetting = new IntegerSetting(name, this, value, min, max);
        SettingsManager.addSetting(integerSetting);
        return integerSetting;
    }

    protected IntegerSetting registerInteger(String name, int value, int min, int max, Supplier<Boolean> dipendent) {
        IntegerSetting integerSetting = new IntegerSetting(name, this, value, min, max);
        integerSetting.setVisible(dipendent);
        SettingsManager.addSetting(integerSetting);
        return integerSetting;
    }

    protected StringSetting registerString(String name, String value) {
        StringSetting stringSetting = new StringSetting(name, this, value);
        SettingsManager.addSetting(stringSetting);
        return stringSetting;
    }

    protected StringSetting registerString(String name, String value, Supplier<Boolean> dipendent) {
        StringSetting stringSetting = new StringSetting(name, this, value);
        stringSetting.setVisible(dipendent);
        SettingsManager.addSetting(stringSetting);
        return stringSetting;
    }

    protected DoubleSetting registerDouble(String name, double value, double min, double max) {
        DoubleSetting doubleSetting = new DoubleSetting(name, this, value, min, max);
        SettingsManager.addSetting(doubleSetting);
        return doubleSetting;
    }

    protected DoubleSetting registerDouble(String name, double value, double min, double max, Supplier<Boolean> dipendent) {
        DoubleSetting doubleSetting = new DoubleSetting(name, this, value, min, max);
        doubleSetting.setVisible(dipendent);
        SettingsManager.addSetting(doubleSetting);
        return doubleSetting;
    }

    protected BooleanSetting registerBoolean(String name, boolean value) {
        BooleanSetting booleanSetting = new BooleanSetting(name, this,value);
        SettingsManager.addSetting(booleanSetting);
        return booleanSetting;
    }

    protected BooleanSetting registerBoolean(String name, boolean value, Supplier<Boolean> dipendent) {
        BooleanSetting booleanSetting = new BooleanSetting(name, this,value);
        booleanSetting.setVisible(dipendent);
        SettingsManager.addSetting(booleanSetting);
        return booleanSetting;
    }

    protected ModeSetting registerMode(String name, List<String> modes, String value) {
        ModeSetting modeSetting = new ModeSetting(name, this, value, modes);
        SettingsManager.addSetting(modeSetting);
        return modeSetting;
    }

    protected ModeSetting registerMode(String name, List<String> modes, String value, Supplier<Boolean> dipendent) {
        ModeSetting modeSetting = new ModeSetting(name, this, value, modes);
        modeSetting.setVisible(dipendent);
        SettingsManager.addSetting(modeSetting);
        return modeSetting;
    }

    protected ColorSetting registerColor(String name, GSColor color) {
        ColorSetting colorSetting = new ColorSetting(name, this, false, color);
        SettingsManager.addSetting(colorSetting);
        return colorSetting;
    }

    protected ColorSetting registerColor(String name, GSColor color, Supplier<Boolean> dipendent) {
        ColorSetting colorSetting = new ColorSetting(name, this, false, color);
        colorSetting.setVisible(dipendent);
        colorSetting.alphaEnabled();
        SettingsManager.addSetting(colorSetting);
        return colorSetting;
    }

    protected ColorSetting registerColor(String name, GSColor color, Supplier<Boolean> dipendent, Boolean alphaEnabled) {
        ColorSetting colorSetting = new ColorSetting(name, this, false, color, alphaEnabled);
        colorSetting.setVisible(dipendent);
        colorSetting.alphaEnabled();
        SettingsManager.addSetting(colorSetting);
        return colorSetting;
    }

    protected ColorSetting registerColor(String name) {
        return registerColor(name, new GSColor(90, 145, 240));
    }

    protected ColorSetting registerColor(String name, Supplier<Boolean> dipendent) {
        ColorSetting color = registerColor(name, new GSColor(90, 145, 240));
        color.setVisible(dipendent);
        return color;
    }
}