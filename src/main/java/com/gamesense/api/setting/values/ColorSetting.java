package com.gamesense.api.setting.values;

import java.util.function.Supplier;

import com.gamesense.api.setting.Setting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.module.Module;

public class ColorSetting extends Setting<GSColor> {
    private boolean rainbow = false;
    private final boolean rainbowEnabled,alphaEnabled;

    public ColorSetting(String name, Module module, boolean rainbow, GSColor value) {
        super(value, name, module);
        this.rainbow = rainbow;
        this.rainbowEnabled = true;
        this.alphaEnabled = false;
    }

    public ColorSetting(String name, String configName, Module module, Supplier<Boolean> isVisible, boolean rainbow, boolean rainbowEnabled, boolean alphaEnabled, GSColor value) {
        super(value, name, configName, module, isVisible);
        this.rainbow = rainbow;
        this.rainbowEnabled = rainbowEnabled;
        this.alphaEnabled = alphaEnabled;
    }

    @Override
    public GSColor getValue() {
        if (rainbow) return this.getRainbowColor(0, 0, 0, false);
        else return super.getValue();
    }

    public static GSColor getRainbowColor(int incr, int multiply, int start, boolean stop) {
        return GSColor.fromHSB((((stop ? start : System.currentTimeMillis()) + incr * multiply) % (360 * 32)) / (360f * 32), 1, 1);
    }

    public static GSColor getRainbowColor(double incr) {
        return GSColor.fromHSB((float) (((incr) % (360 * 32)) / (360f * 32)), 1, 1);
    }

    public static GSColor getRainbowSin(int incr, int multiply, double height, int multiplyHeight, double millSin, int start, boolean stop) {
        return GSColor.fromHSB((float) ((height * multiplyHeight * Math.sin((((stop ? start : System.currentTimeMillis()) + (incr / millSin) * multiply) % (360 * 32)) / (360f * 32)))), 1, 1);
    }

    public static GSColor getRainbowTan(int incr, int multiply, double height, int multiplyHeight, double millSin, int start, boolean stop) {
        return GSColor.fromHSB((float) ((height * multiplyHeight * Math.tan((((stop ? start : System.currentTimeMillis()) + ((incr / millSin) * multiply) % (360 * 32)) / (360f * 32))))), 1, 1);
    }

    public static GSColor getRainbowSec(int incr, int multiply, double height, int multiplyHeight, double millSin, int start, boolean stop) {
        return GSColor.fromHSB((float) ((height * multiplyHeight * (1/Math.sin((((stop ? start : System.currentTimeMillis()) + ((float) incr / millSin) * multiply) % (360 * 32)) / (360f * 32))))), 1, 1);
    }

    public static GSColor getRainbowCosec(int incr, int multiply, double height, int multiplyHeight, double millSin, int start, boolean stop) {
        return GSColor.fromHSB((float) ((height * multiplyHeight * (1/Math.cos((((stop ? start : System.currentTimeMillis()) + (incr / millSin) * multiply) % (360 * 32)) / (360f * 32))))), 1, 1);
    }

    public static GSColor getRainbowCoTan(int incr, int multiply, double height, int multiplyHeight, double millSin, int start, boolean stop) {
        return GSColor.fromHSB((float) ((height * multiplyHeight * Math.tan((((stop ? start : System.currentTimeMillis()) + (incr / millSin) * multiply) % (360 * 32)) / (360f * 32)))), 1, 1);
    }

    public int toInteger() {
        return getValue().getRGB() & 0xFFFFFF + (this.rainbow ? 1 : 0) * 0x1000000;
    }

    public void fromInteger(int number) {
        this.rainbow = ((number & 0x1000000) != 0);

        super.setValue(this.rainbow ? GSColor.fromHSB((System.currentTimeMillis() % (360 * 32)) / (360f * 32), 1, 1) : new GSColor(number & 0xFFFFFF));
    }

    @Override
    public void setValue(Color value) {
        super.setValue(new GSColor(value));
    }

    @Override
    public Color getColor() {
        return super.getValue();
    }

    @Override
    public boolean getRainbow() {
    	return rainbow;
    }

    public void setRainbow (boolean rainbow) {
    	this.rainbow=rainbow;
    }

    public boolean rainbowEnabled() {
    	return rainbowEnabled;
    }

    public boolean alphaEnabled() {
    	return alphaEnabled;
    }
}