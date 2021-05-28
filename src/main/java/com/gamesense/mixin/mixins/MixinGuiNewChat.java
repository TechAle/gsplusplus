package com.gamesense.mixin.mixins;

import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.misc.ChatModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat {
    // Get te module
    ChatModifier chatModifier = ModuleManager.getModule(ChatModifier.class);

    // Draw the background of the chat
    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V"))
    private void drawRectBackgroundClean(int left, int top, int right, int bottom, int color) {
        if (chatModifier.isEnabled()) {
            // This fix a bug with some random colors appears
            if (left != 0 && top != 0)
                Gui.drawRect(left, top, right, bottom, new GSColor(chatModifier.backColor.getValue(), chatModifier.alphaColor.getValue()).getRGB());
        } else Gui.drawRect(left, top, right, bottom, color);
    }

    // Custom Size
    @Redirect(method = {"drawChat"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V"))
    private void customSize(float x, float y, float z) {
        // If enable
        if (chatModifier.isEnabled()) {
            // Custom Scale
            GlStateManager.scale(chatModifier.xScale.getValue(), chatModifier.yScale.getValue(), 1f);
            // Translate it
            GlStateManager.translate(chatModifier.leftPosition.getValue() != -1 ? chatModifier.leftPosition.getValue() : x,
                    chatModifier.upPosition.getValue() != -100 ? -chatModifier.upPosition.getValue() : y,
                    z);
        } else {
            GlStateManager.translate(x, y, z);
        }

    }

    // Color
    @Redirect(method={"drawChat"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int drawStringWithShadow(FontRenderer fontRenderer, String text, float x, float y, int color) {
        // If enable
        if (chatModifier.isEnabled()) {
            // Display text
            displayText(text, x, y);
        } else Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color);
        return 0;
    }

    String[] specialWords = null;

    private void displayText(String word, float x, float y) {
        // Lets get the actual colored output
        StringBuilder outputstring = new StringBuilder();

        // The list that is going to tell us how we are going to write everything
        ArrayList<ArrayList<String>> toWrite = new ArrayList<>();

        // The color
        String nowColor = "";

        // If we found before a $ => Color
        boolean before$ = false;

        // Get the list
        if (specialWords == null)
            specialWords = SocialManager.getSpecialNamesString().toArray(new String[0]);


        // Transform it into a char array
        for(char c : word.toCharArray()) {
            // If it's colored
            if (c == '§') {
                // Lets check the color after
                before$ = true;
                // Check if we have to append a new text
                if (outputstring.length() != 0) {
                    // Check if we have a special word
                    String lowercase = outputstring.toString();
                    boolean found = false;
                    // Check for every words
                    for(int i = 0; i < specialWords.length; i++) {
                        if (lowercase.toLowerCase().contains(specialWords[i])) {
                            // If we have found that this could have special words
                            found = true;
                            StringBuilder newOutput = new StringBuilder();
                            // Iterate for every words
                            boolean foundSpecial;
                            for(String part : lowercase.split(" ")) {
                                foundSpecial = false;
                                // Check if there is a special
                                for(int j = i; j < specialWords.length; j++) {
                                    if (part.toLowerCase().contains(specialWords[j])) {
                                        // There is
                                        foundSpecial = true;
                                        break;
                                    }
                                }
                                // If there was
                                if (foundSpecial) {
                                    // Add the before text
                                    if (newOutput.length() != 0)
                                        toWrite.add(new ArrayList<>(Arrays.asList(
                                                nowColor, newOutput.toString())));
                                    // Add the special one
                                    toWrite.add(new ArrayList<>(Arrays.asList(
                                            "\u200Especial", part + " ")));
                                    newOutput.setLength(0);
                                // If normal
                                } else newOutput.append(part).append(" ");
                            }
                            // If there was something left, add
                            if (newOutput.length() != 0)
                                toWrite.add(new ArrayList<>(Arrays.asList(
                                        nowColor, newOutput.toString())));
                            break;
                        }
                    }

                    if (!found) {
                        // Append it
                        toWrite.add(new ArrayList<>(Arrays.asList(
                                nowColor, outputstring.toString())));
                    }
                    // Clear
                    outputstring.setLength(0);
                    nowColor = "";
                }
            }
            else {
                // If friend
                if (c == '\u2064') {
                    nowColor = Integer.toString(-new GSColor(chatModifier.friendColor.getValue(), 255).getRGB());
                // If enemy
                } else if (c == '\u2065') {
                    nowColor = Integer.toString(-new GSColor(chatModifier.enemyColor.getValue(), 255).getRGB());
                // If normal
                } else if (c == '\u2066') {
                    nowColor = Integer.toString(-new GSColor(chatModifier.playerColor.getValue(), 255).getRGB());
                } else if (c == '\u2067') {
                    nowColor = Integer.toString(-new GSColor(chatModifier.timeColor.getValue(), 255).getRGB());
                }
                // If we had a color
                if (before$) {
                    // Ignore special characters
                    if (c == 'k' || c == 'l' || c == 'm' || c == 'n' || c == 'o')
                        outputstring.append("§").append(c);
                    else
                        // reset color
                        if (c == 'r')
                            nowColor = "";
                        else
                            // Add the special color
                            nowColor = Integer.toString(getIntFromat(c));
                    before$ = false;
                }
                else outputstring.append(c);


            }
        }
        int width = 0;

        // This remember where we are with the rainbow color
        int rainbowColor = 0;
        // This is a temp variable for the 2 variables we are having as return in the function writeDesync
        int[] temp;
        // Some values for the sin thing
        int rainbowDesyncSmooth = chatModifier.rainbowDesyncSmooth.getValue();
        double heightSin = chatModifier.heightSin.getValue();
        int multiplyHeight = chatModifier.multiplyHeight.getValue();
        double millSin = chatModifier.millSin.getValue();
        // Iterate for every string
        for (ArrayList<String> strings : toWrite) {
            // Check the type of the text
            switch (strings.get(0)) {
                // Normal text
                case "":
                    // If rainbow desync
                    if (chatModifier.desyncRainbowNormal.getValue()) {
                        // Write
                        temp = writeDesync(strings.get(1), width, x, y, rainbowColor, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin);
                        width = temp[0];
                        rainbowColor = temp[1];
                    } else
                        // Else, customColor
                        width += writeCustom(strings.get(1), width, x, y, -new GSColor(chatModifier.normalColor.getValue(), 255).getRGB());
                    break;
                // If special
                case "\u200Especial":
                    // If rainbow desync
                    if (chatModifier.desyncRainbowSpecial.getValue()) {
                        temp = writeDesync(strings.get(1).replace("\u2063", ""), width, x, y, rainbowColor, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin);
                        width = temp[0];
                        rainbowColor = temp[1];
                    } else
                        // Else, custom
                        width += writeCustom(strings.get(1), width, x, y, -new GSColor(chatModifier.normalColor.getValue(), 255).getRGB());
                    break;
                default:
                    // Write custom with the color in input
                    width += writeCustom(strings.get(1), width, x, y, Integer.parseInt(strings.get(0)));
                    break;
            }
        }

    }

    private int getIntFromat(char value) {
        switch (value) {
            // aqua
            case 'b':
                return -new GSColor(chatModifier.aqua.getValue(), 255).getRGB();
            // purple
            case 'd':
                return -new GSColor(chatModifier.purple.getValue(), 255).getRGB();
            // Dark purple
            case '5':
                return -new GSColor(chatModifier.dark_purple.getValue(), 255).getRGB();
            // Blue
            case '9':
                return -new GSColor(chatModifier.blue.getValue(), 255).getRGB();
            // Gray
            case '7':
                return -new GSColor(chatModifier.gray.getValue(), 255).getRGB();
            // Dark Aqua
            case '3':
                return -new GSColor(chatModifier.dark_aqua.getValue(), 255).getRGB();
            // Dark Blue
            case '1':
                return -new GSColor(chatModifier.dark_blue.getValue(), 255).getRGB();
            // Yellow
            case 'e':
                return -new GSColor(chatModifier.yellow.getValue(), 255).getRGB();
            // Red
            case 'c':
                return-new GSColor(chatModifier.red.getValue(), 255).getRGB();
            // Green
            case 'a':
                return -new GSColor(chatModifier.green.getValue(), 255).getRGB();
            // Dark Gray
            case '8':
                return -new GSColor(chatModifier.dark_gray.getValue(), 255).getRGB();
            // Gold
            case '6':
                return -new GSColor(chatModifier.gold.getValue(), 255).getRGB();
            // Dark Red
            case '4':
                return -new GSColor(chatModifier.dark_red.getValue(), 255).getRGB();
            // Dark Green
            case '2':
                return -new GSColor(chatModifier.dark_green.getValue(), 255).getRGB();
            // Black
            case '0':
                return -new GSColor(chatModifier.black.getValue(), 255).getRGB();
            default:
                return -new GSColor(chatModifier.white.getValue(), 255).getRGB();
        }

    }

    private int writeCustom(String text, int width, float x, float y, int color) {
        // Write it and return the new width
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x + width, y, -color);
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }

    private int[] writeDesync(String text, int width, float x, float y, int rainbowColor, int rainbowDesyncSmooth, double heightSin, int multiplyHeight, double millSin) {
        boolean skip = false;
        // Iterate for every characters
        for(String character : text.split("")) {
            // If we have to skip because it's a cholor
            if (skip) {
                skip = false;
                continue;
            }
            // If it's a color that we have to read
            if (character.equals("§")) {
                skip = true;
                continue;
            }
            GSColor colorOut = null;
            switch (chatModifier.rainbowType.getValue().toLowerCase()) {
                case "sin":
                    colorOut = getSinRainbow(rainbowColor, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin);
                    break;
                case "tan":
                    colorOut = getTanRainbow(rainbowColor, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin);
                    break;
                case "secant":
                    colorOut = getSecantRainbow(rainbowColor, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin);
                    break;
                case "cosecant":
                    colorOut = getCosecRainbow(rainbowColor, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin);
                    break;
                case "cotangent":
                    colorOut = getCoTanRainbow(rainbowColor, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin);
                    break;
                default:
                    colorOut = getRainbow(rainbowColor);
                    break;
            }
            // Color 1 character
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(character, x + width, y, new GSColor(colorOut.getRGB()).getRGB());
            // Add width
            width += Minecraft.getMinecraft().fontRenderer.getStringWidth(character);
            // Add rainbow
            rainbowColor += 1;
        }
        return new int[] {width, rainbowColor};
    }

    // Get rainbow color
    private GSColor getRainbow(int incr) {
        GSColor color = ColorSetting.getRainbowColor(incr, chatModifier.rainbowDesyncSmooth.getValue());
        return new GSColor(color.getRed(), color.getBlue(), color.getGreen(), 255);
    }

    // Get rainbow color
    private GSColor getSinRainbow(int incr, int rainbowDesyncSmooth, double heightSin, int multiplyHeight, double millSin) {
        GSColor color = ColorSetting.getRainbowSin(incr, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin);
        return new GSColor(color.getRed(), color.getBlue(), color.getGreen(), 255);
    }

    // Get rainbow color
    private GSColor getTanRainbow(int incr, int rainbowDesyncSmooth, double heightSin, int multiplyHeight, double millSin) {
        GSColor color = ColorSetting.getRainbowTan(incr, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin);
        return new GSColor(color.getRed(), color.getBlue(), color.getGreen(), 255);
    }

    // Get rainbow color
    private GSColor getCosecRainbow(int incr, int rainbowDesyncSmooth, double heightSin, int multiplyHeight, double millSin) {
        GSColor color = ColorSetting.getRainbowCosec(incr, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin);
        return new GSColor(color.getRed(), color.getBlue(), color.getGreen(), 255);
    }

    // Get rainbow color
    private GSColor getSecantRainbow(int incr, int rainbowDesyncSmooth, double heightSin, int multiplyHeight, double millSin) {
        GSColor color = ColorSetting.getRainbowSec(incr, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin);
        return new GSColor(color.getRed(), color.getBlue(), color.getGreen(), 255);
    }

    // Get rainbow color
    private GSColor getCoTanRainbow(int incr, int rainbowDesyncSmooth, double heightSin, int multiplyHeight, double millSin) {
        GSColor color = ColorSetting.getRainbowCoTan(incr, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin);
        return new GSColor(color.getRed(), color.getBlue(), color.getGreen(), 255);
    }


    // This is for custom height
    @Inject(method = {"getChatHeight"}, at = @At("HEAD"), cancellable = true)
    public void getChatHeight(CallbackInfoReturnable<Integer> cir) {
        if (chatModifier.isEnabled()) {
            if (chatModifier.maxH.getValue() != -1)
                cir.setReturnValue(chatModifier.maxH.getValue());
        }
    }

    // This is for custom width
    @Inject(method = {"getChatWidth"}, at = @At("HEAD"), cancellable = true)
    public void getChatWidth(CallbackInfoReturnable<Integer> cir) {
        if (chatModifier.isEnabled()) {
            if (chatModifier.maxW.getValue() != -1)
                cir.setReturnValue(chatModifier.maxW.getValue());
        }
    }

}