package com.gamesense.api.util.misc;


import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import com.gamesense.client.module.modules.misc.ChatModifier;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


@SideOnly(Side.CLIENT)
public class NewChat extends GuiNewChat {

    final ChatModifier chatModifier = ModuleManager.getModule(ChatModifier.class);

    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft mc;
    /**
     * A list of messages previously sent through the chat GUI
     */
    private final List<String> sentMessages = Lists.newArrayList();
    /**
     * Chat lines to be displayed in the chat box
     */
    private final List<ChatLine> chatLines = Lists.newArrayList();
    /**
     * List of the ChatLines currently drawn
     */
    private final List<ChatLine> drawnChatLines = Lists.newArrayList();
    private int scrollPos;
    //private boolean isScrolled;
    public static float percentComplete = 0.0F;
    public static int newLines;
    public static long prevMillis = -1;
    public boolean configuring;
    double count = 0;

    public NewChat(Minecraft mcIn) {
        super(mcIn);
        this.mc = mcIn;
    }

    private void updatePercentage(long diff) {
        if (percentComplete < 1) percentComplete += 0.004f * diff;
        percentComplete = chatModifier.clamp(percentComplete, 0, 1);
    }

    public void drawChat(int updateCounter) {
        // type custom use a different count, normally dey use the date of the machine, custom use a double
        if (!chatModifier.stopDesyncSpecial.getValue())
            count += chatModifier.customAdd.getValue() * chatModifier.customMultiply.getValue();
        boolean customText = ModuleManager.getModule(ColorMain.class).textFont.getValue();
        if (configuring) return;
        // Y position for down animation
        if (prevMillis == -1) {
            prevMillis = System.currentTimeMillis();
            return;
        }
        long current = System.currentTimeMillis();
        long diff = current - prevMillis;
        prevMillis = current;
        updatePercentage(diff);
        float t = percentComplete;
        float percent = 1 - (--t) * t * t * t;
        percent = chatModifier.clamp(percent, 0, 1);
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int i = this.getLineCount();
            int j = this.drawnChatLines.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (j > 0) {
                boolean flag = false;

                if (this.getChatOpen()) {
                    flag = true;
                }

                float f1 = this.getChatScale();
                int k = MathHelper.ceil((float) this.getChatWidth() / f1);
                GlStateManager.pushMatrix();
                float x, y;
                x = 2.0F + chatModifier.leftPosition.getValue();
                y = 8.0F - chatModifier.upPosition.getValue();
                // Custom Scale
                GlStateManager.scale(chatModifier.xScale.getValue(), chatModifier.yScale.getValue(), 1f);
                // Translate it
                GlStateManager.translate(chatModifier.leftPosition.getValue() != -1 ? chatModifier.leftPosition.getValue() : x,
                        chatModifier.upPosition.getValue() != -100 ? -chatModifier.upPosition.getValue() : y,
                        0F);
                int l = 0;

                for (int i1 = 0; i1 + this.scrollPos < this.drawnChatLines.size() && i1 < i; ++i1) {
                    ChatLine chatline = this.drawnChatLines.get(i1 + this.scrollPos);

                    if (chatline != null) {
                        int j1 = updateCounter - chatline.getUpdatedCounter();

                        if (j1 < 200 || flag) {
                            double d0 = (double) j1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 = d0 * 10.0D;
                            d0 = MathHelper.clamp(d0, 0.0D, 1.0D);
                            d0 = d0 * d0;
                            int l1 = (int) (255.0D * d0);

                            if (flag) {
                                l1 = 255;
                            }

                            l1 = (int) ((float) l1 * f);
                            ++l;

                            if (l1 > 3) {
                                int i2 = 0;
                                int j2 = -i1 * 9;
                                // Draw background color
                                drawRect(-2, j2 - 9, i2 + k + 4, j2,
                                        getColorAlpha(chatModifier.backColor, chatModifier.alphaColor));
                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();

                                // If we have to add a new animation
                                switch (chatModifier.animationtext.getValue()) {
                                    case "Down Up":
                                        // Down up
                                        x = 0F;
                                        y = (float) (j2 - 8) + (18 - 18*percent)*f1;
                                        break;
                                    case "Left Right":
                                        // Get X Percent
                                        float xPercent = (updateCounter) - chatline.getUpdatedCounter();
                                        // If 8 ticks are passed
                                        if (xPercent > 8)
                                            x = 0;
                                        else
                                            // If no, calculate the percent
                                            x = 8F - (9 - 9*((xPercent / 8 * 100) - 100)/40)*f1;
                                        // Set the y
                                        y = (float) (j2 - 8);
                                        break;
                                    default:
                                        x = 0F;
                                        y = (float) (j2 - 8);
                                        break;
                                }
                                try {
                                    // Gs custom text
                                    displayText(s, x, y, customText);
                                }catch (IndexOutOfBoundsException ignored) {

                                }
                                //this.mc.fontRenderer.drawStringWithShadow(s, x, y, 16777215 + (l1 << 24));

                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();
                            }
                        }
                    }
                }

                if (flag) {
                    int k2 = this.mc.fontRenderer.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int l2 = j * k2 + j;
                    int i3 = l * k2 + l;
                    int j3 = this.scrollPos * i3 / j;
                    int k1 = i3 * i3 / l2;

                    if (l2 != i3) {
                        /*
                        int k3 = j3 > 0 ? 170 : 96;
                        int l3 = this.isScrolled ? 13382451 : 3355562;*/
                        // Gs custom slider
                        if (!chatModifier.hideSlider.getValue()) {
                            drawRect(-chatModifier.sliderSpace.getValue() + chatModifier.sliderWidth.getValue(), -j3,
                                    -chatModifier.sliderSpace.getValue(), -j3 - k1,
                                    getColorAlpha(chatModifier.firstColor, chatModifier.firstAlpha));
                            drawRect(-chatModifier.sliderSpace.getValue(), -j3,
                                    -chatModifier.sliderSpace.getValue() + -chatModifier.sliderWidth.getValue(),
                                    -j3 - k1,  getColorAlpha(chatModifier.secondColor, chatModifier.secondAlpha));
                            drawRect(-chatModifier.sliderSpace.getValue() + -chatModifier.sliderWidth.getValue(), -j3,
                                    -chatModifier.sliderSpace.getValue() + (-chatModifier.sliderWidth.getValue()*2),
                                    -j3 - k1,  getColorAlpha(chatModifier.thirdColor, chatModifier.thirdAlpha));
                        }
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }

    /**
     * Clears the chat.
     */
    public void clearChatMessages(boolean p_146231_1_) {
        this.drawnChatLines.clear();
        this.chatLines.clear();

        if (p_146231_1_) {
            this.sentMessages.clear();
        }
    }

    public void printChatMessage(ITextComponent chatComponent) {
        this.printChatMessageWithOptionalDeletion(chatComponent, 0);
    }

    /**
     * prints the ChatComponent to Chat. If the ID is not 0, deletes an existing Chat Line of that ID from the GUI
     */
    public void printChatMessageWithOptionalDeletion(ITextComponent chatComponent, int chatLineId) {
        percentComplete = 0.0F;
        this.setChatLine(chatComponent, chatLineId, this.mc.ingameGUI.getUpdateCounter(), false);
        LOGGER.info("[CHAT] {}", (Object) chatComponent.getUnformattedText().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
    }

    private void setChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly) {
        if (chatLineId != 0) {
            this.deleteChatLine(chatLineId);
        }

        int i = MathHelper.floor((float) this.getChatWidth() / this.getChatScale());
        List<ITextComponent> list = GuiUtilRenderComponents.splitText(chatComponent, i, this.mc.fontRenderer, false, false);
        boolean flag = this.getChatOpen();
        newLines = list.size() - 1;

        for (ITextComponent itextcomponent : list) {
            if (flag && this.scrollPos > 0) {
                //this.isScrolled = true;
                this.scroll(1);
            }

            this.drawnChatLines.add(0, new ChatLine(updateCounter, itextcomponent, chatLineId));
        }

        while (this.drawnChatLines.size() > 100) {
            this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
        }

        if (!displayOnly) {
            this.chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));

            while (this.chatLines.size() > 100) {
                this.chatLines.remove(this.chatLines.size() - 1);
            }
        }
    }

    public void refreshChat() {
        this.drawnChatLines.clear();
        this.resetScroll();

        for (int i = this.chatLines.size() - 1; i >= 0; --i) {
            ChatLine chatline = this.chatLines.get(i);
            this.setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
        }
    }

    /**
     * Gets the list of messages previously sent through the chat GUI
     */
    public List<String> getSentMessages() {
        return this.sentMessages;
    }

    /**
     * Adds this string to the list of sent messages, for recall using the up/down arrow keys
     */
    public void addToSentMessages(String message) {
        if (this.sentMessages.isEmpty() || !this.sentMessages.get(this.sentMessages.size() - 1).equals(message)) {
            this.sentMessages.add(message);
        }
    }

    /**
     * Resets the chat scroll (executed when the GUI is closed, among others)
     */
    public void resetScroll() {
        this.scrollPos = 0;
        //this.isScrolled = false;
    }

    /**
     * Scrolls the chat by the given number of lines.
     */
    public void scroll(int amount) {
        this.scrollPos += amount;
        int i = this.drawnChatLines.size();

        if (this.scrollPos > i - this.getLineCount()) {
            this.scrollPos = i - this.getLineCount();
        }

        if (this.scrollPos <= 0) {
            this.scrollPos = 0;
            //this.isScrolled = false;
        }
    }

    /**
     * Gets the chat component under the mouse
     */
    @Nullable
    public ITextComponent getChatComponent(int mouseX, int mouseY) {
        if (!this.getChatOpen()) {
            return null;
        } else {
            ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            int i = scaledresolution.getScaleFactor();
            float f = this.getChatScale();
            int j = mouseX / i - 2 - chatModifier.leftPosition.getValue();
            int k = mouseY / i - 40 - chatModifier.upPosition.getValue();
            j = MathHelper.floor((float) j / f);
            k = MathHelper.floor((float) k / f);

            if (j >= 0 && k >= 0) {
                int l = Math.min(this.getLineCount(), this.drawnChatLines.size());

                if (j <= MathHelper.floor((float) this.getChatWidth() / this.getChatScale()) && k < this.mc.fontRenderer.FONT_HEIGHT * l + l) {
                    int i1 = k / this.mc.fontRenderer.FONT_HEIGHT + this.scrollPos;

                    if (i1 >= 0 && i1 < this.drawnChatLines.size()) {
                        ChatLine chatline = this.drawnChatLines.get(i1);
                        int j1 = 0;

                        for (ITextComponent itextcomponent : chatline.getChatComponent()) {
                            if (itextcomponent instanceof TextComponentString) {
                                j1 += this.mc.fontRenderer.getStringWidth(GuiUtilRenderComponents.removeTextColorsIfConfigured(((TextComponentString) itextcomponent).getText(), false));

                                if (j1 > j) {
                                    return itextcomponent;
                                }
                            }
                        }
                    }

                    return null;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Returns true if the chat GUI is open
     */
    public boolean getChatOpen() {
        return this.mc.currentScreen instanceof GuiChat;
    }

    /**
     * finds and deletes a Chat line by ID
     */
    public void deleteChatLine(int id) {
        Iterator<ChatLine> iterator = this.drawnChatLines.iterator();

        while (iterator.hasNext()) {
            ChatLine chatline = iterator.next();

            if (chatline.getChatLineID() == id) {
                iterator.remove();
            }
        }

        iterator = this.chatLines.iterator();

        while (iterator.hasNext()) {
            ChatLine chatline1 = iterator.next();

            if (chatline1.getChatLineID() == id) {
                iterator.remove();
                break;
            }
        }
    }

    public int getChatWidth() {
        return chatModifier.maxW.getValue() != -1 ? chatModifier.maxW.getValue() : calculateChatboxWidth(this.mc.gameSettings.chatWidth);
    }

    public int getChatHeight() {
        return chatModifier.maxH.getValue() != -1 ? chatModifier.maxH.getValue() : calculateChatboxHeight(this.getChatOpen() ? this.mc.gameSettings.chatHeightFocused : this.mc.gameSettings.chatHeightUnfocused);
    }

    String[] specialWords = null;

    private void displayText(String word, float x, float y, boolean isCustom) {
        // Lets get the actual colored output
        StringBuilder outputstring = new StringBuilder();

        // The list that is going to tell us how we are going to write everything
        List<List<String>> toWrite = new ArrayList<>();

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
            if (c == 167) {
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
                    nowColor = Integer.toString(getColorAlpha(chatModifier.friendColor));
                    // If enemy
                } else if (c == '\u2065') {
                    nowColor = Integer.toString(getColorAlpha(chatModifier.enemyColor));
                    // If normal
                } else if (c == '\u2066') {
                    nowColor = Integer.toString(getColorAlpha(chatModifier.playerColor));
                } else if (c == '\u2067') {
                    nowColor = Integer.toString(getColorAlpha(chatModifier.timeColor));
                } else
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
        for (List<String> strings : toWrite) {
            // Check the type of the text
            switch (strings.get(0)) {
                // Normal text
                case "":
                    // If rainbow desync
                    if (chatModifier.desyncRainbowNormal.getValue()) {
                        // Write
                        temp = writeDesync(strings.get(1), width, x, y, rainbowColor, rainbowDesyncSmooth,
                                heightSin, multiplyHeight, millSin, isCustom,
                                getColorAlpha(chatModifier.normalColor), chatModifier.stopDesyncNormal.getValue());
                        width = temp[0];
                        rainbowColor = temp[1];
                    } else
                        // Else, customColor
                        width += writeCustom(strings.get(1), width, x, y, getColorAlpha(chatModifier.normalColor), isCustom);
                    break;
                // If special
                case "\u200Especial":
                    // If rainbow desync
                    if (chatModifier.desyncRainbowSpecial.getValue()) {
                        temp = writeDesync(strings.get(1).replace("\u2063", ""),
                                width, x, y, rainbowColor, rainbowDesyncSmooth, heightSin, multiplyHeight,
                                millSin, isCustom, getColorAlpha(chatModifier.specialColor),
                                chatModifier.stopDesyncSpecial.getValue());
                        width = temp[0];
                        rainbowColor = temp[1];
                    } else
                        // Else, custom
                        width += writeCustom(strings.get(1), width, x, y,
                                getColorAlpha(chatModifier.specialColor), isCustom);
                    break;
                default:
                    // Write custom with the color in input
                    width += writeCustom(strings.get(1), width, x, y, Integer.parseInt(strings.get(0)), isCustom);
                    break;
            }
        }

    }

    int getColorAlpha(ColorSetting startColor) {
        return -new GSColor(startColor.getValue(), 255).getRGB();
    }

    int getColorAlpha(ColorSetting startColor, IntegerSetting alpha) {
        return new GSColor(startColor.getValue(), alpha.getValue()).getRGB();
    }

    private int getIntFromat(char value) {
        switch (value) {
            // aqua
            case 'b':
                return getColorAlpha(chatModifier.aqua);
            // purple
            case 'd':
                return getColorAlpha(chatModifier.purple);
            // Dark purple
            case '5':
                return getColorAlpha(chatModifier.dark_purple);
            // Blue
            case '9':
                return getColorAlpha(chatModifier.blue);
            // Gray
            case '7':
                return getColorAlpha(chatModifier.gray);
            // Dark Aqua
            case '3':
                return getColorAlpha(chatModifier.dark_aqua);
            // Dark Blue
            case '1':
                return getColorAlpha(chatModifier.dark_blue);
            // Yellow
            case 'e':
                return getColorAlpha(chatModifier.yellow);
            // Red
            case 'c':
                return getColorAlpha(chatModifier.red);
            // Green
            case 'a':
                return getColorAlpha(chatModifier.green);
            // Dark Gray
            case '8':
                return getColorAlpha(chatModifier.dark_gray);
            // Gold
            case '6':
                return getColorAlpha(chatModifier.gold);
            // Dark Red
            case '4':
                return getColorAlpha(chatModifier.dark_red);
            // Dark Green
            case '2':
                return getColorAlpha(chatModifier.dark_green);
            // Black
            case '0':
                return getColorAlpha(chatModifier.black);
            default:
                return getColorAlpha(chatModifier.white);
        }

    }

    private int writeCustom(String text, int width, float x, float y, int color, boolean isCustom) {
        // Write it and return the new width
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x + width, y, -color);
        return isCustom ? GameSense.INSTANCE.cFontRenderer.getStringWidth(text) : mc.fontRenderer.getStringWidth(text);
    }

    private int[] writeDesync(String text, int width, float x, float y, int rainbowColor, int rainbowDesyncSmooth, double heightSin, int multiplyHeight, double millSin, boolean isCustom, int startColor, boolean stop) {
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
            GSColor colorOut;
            switch (chatModifier.rainbowType.getValue().toLowerCase()) {
                case "sin":
                    colorOut = getSinRainbow(rainbowColor, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin, startColor, stop);
                    break;
                case "tan":
                    colorOut = getTanRainbow(rainbowColor, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin, startColor, stop);
                    break;
                case "secant":
                    colorOut = getSecantRainbow(rainbowColor, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin, startColor, stop);
                    break;
                case "cosecant":
                    colorOut = getCosecRainbow(rainbowColor, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin, startColor, stop);
                    break;
                case "cotangent":
                    colorOut = getCoTanRainbow(rainbowColor, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin, startColor, stop);
                    break;
                case "custom":
                    colorOut = getRainbowCustom(rainbowColor);
                    break;
                default:
                    colorOut = getRainbow(rainbowColor, startColor, stop);
                    break;
            }
            // Color 1 character
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(character, x + width, y, new GSColor(colorOut.getRGB()).getRGB());
            // Add width
            width += isCustom ? GameSense.INSTANCE.cFontRenderer.getStringWidth(character) : mc.fontRenderer.getStringWidth(character);
            // Add rainbow
            if (chatModifier.rainbowType.getValue().equalsIgnoreCase("custom"))
                rainbowColor += chatModifier.rainbowDesyncSmooth.getValue() * chatModifier.cutomDesync.getValue();
            else rainbowColor += 1;
        }
        return new int[] {width, rainbowColor};
    }

    private GSColor getRainbowCustom(int incr) {
        GSColor color = ColorSetting.getRainbowColor(count + incr);
        return new GSColor(color.getRed(), color.getBlue(), color.getGreen(), 255);
    }

    // Get rainbow color
    private GSColor getRainbow(int incr, int start, boolean stop) {
        GSColor color = ColorSetting.getRainbowColor(incr, chatModifier.rainbowDesyncSmooth.getValue(), start, stop);
        return new GSColor(color.getRed(), color.getBlue(), color.getGreen(), 255);
    }

    // Get rainbow color
    private GSColor getSinRainbow(int incr, int rainbowDesyncSmooth, double heightSin, int multiplyHeight, double millSin, int start, boolean stop) {
        GSColor color = ColorSetting.getRainbowSin(incr, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin, start, stop);
        return new GSColor(color.getRed(), color.getBlue(), color.getGreen(), 255);
    }

    // Get rainbow color
    private GSColor getTanRainbow(int incr, int rainbowDesyncSmooth, double heightSin, int multiplyHeight, double millSin, int start, boolean stop) {
        GSColor color = ColorSetting.getRainbowTan(incr, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin, start, stop);
        return new GSColor(color.getRed(), color.getBlue(), color.getGreen(), 255);
    }

    // Get rainbow color
    private GSColor getCosecRainbow(int incr, int rainbowDesyncSmooth, double heightSin, int multiplyHeight, double millSin, int start, boolean stop) {
        GSColor color = ColorSetting.getRainbowCosec(incr, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin, start, stop);
        return new GSColor(color.getRed(), color.getBlue(), color.getGreen(), 255);
    }

    // Get rainbow color
    private GSColor getSecantRainbow(int incr, int rainbowDesyncSmooth, double heightSin, int multiplyHeight, double millSin, int start, boolean stop) {
        GSColor color = ColorSetting.getRainbowSec(incr, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin, start, stop);
        return new GSColor(color.getRed(), color.getBlue(), color.getGreen(), 255);
    }

    // Get rainbow color
    private GSColor getCoTanRainbow(int incr, int rainbowDesyncSmooth, double heightSin, int multiplyHeight, double millSin, int start, boolean stop) {
        GSColor color = ColorSetting.getRainbowCoTan(incr, rainbowDesyncSmooth, heightSin, multiplyHeight, millSin, start, stop);
        return new GSColor(color.getRed(), color.getBlue(), color.getGreen(), 255);
    }

    /**
     * Returns the chatscale from mc.gameSettings.chatScale
     */
    public float getChatScale() {
        return this.mc.gameSettings.chatScale;
    }

    public static int calculateChatboxWidth(float scale) {
        /*
        int i = 320;
        int j = 40;*/
        return MathHelper.floor(scale * 280.0F + 40.0F);
    }

    public static int calculateChatboxHeight(float scale) {
        /*
        int i = 180;
        int j = 20;*/
        return MathHelper.floor(scale * 160.0F + 20.0F);
    }

    public int getLineCount() {
        return this.getChatHeight() / 9;
    }
}