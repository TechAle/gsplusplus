package com.gamesense.client.module.modules.misc;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.misc.NewChat;
import com.gamesense.api.util.player.social.Enemy;
import com.gamesense.api.util.player.social.Friend;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.command.CommandManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Module.Declaration(name = "ChatModifier", category = Category.Misc)
public class ChatModifier extends Module {

    // General chat colors
    public ColorSetting backColor = registerColor("Background Color", new GSColor(0, 0, 0));
    public IntegerSetting alphaColor = registerInteger("Background Alpha", 255, 0, 255);
    public ColorSetting normalColor = registerColor("Normal Color", new GSColor(0, 0, 0));
    public ColorSetting specialColor = registerColor("Special Color", new GSColor(0, 125, 125));
    // Rainbow settings
    public BooleanSetting desyncRainbowNormal = registerBoolean("Desync Rainbow Normal", false);
    public BooleanSetting desyncRainbowSpecial = registerBoolean("Desync Rainbow Special", false);
    public ModeSetting rainbowType = registerMode("Rainbow Type", Arrays.asList("Slow", "Sin", "Tan", "Secant", "Cosecant", "Cotangent"), "Tan");
    public IntegerSetting rainbowDesyncSmooth = registerInteger("Rainbow Desync Smooth", 50, 1, 1000);
    public DoubleSetting heightSin = registerDouble("Height Sin", 1, 0.1, 20);
    public IntegerSetting multiplyHeight = registerInteger("Multiply Height Sin", 1, 1, 10);
    public DoubleSetting millSin = registerDouble("Mill Sin", 10, 0.1, 15);
    // Dimensions
    public IntegerSetting upPosition = registerInteger("Up Translation", -1, -100, 700);
    public IntegerSetting leftPosition = registerInteger("Left Translation", -1, -1, 700);
    public DoubleSetting yScale = registerDouble("Height Scale", 1, 0, 3);
    public DoubleSetting xScale = registerDouble("Width Scale", 1, 0, 3);
    public IntegerSetting maxH = registerInteger("Max Height", -1, -1, 500);
    public IntegerSetting maxW = registerInteger("Max Width", -1, -1, 500);
    public ModeSetting animationtext = registerMode("Animation Text", Arrays.asList("None", "Down Up", "Left Right"), "Down Up");
    // Animation
    BooleanSetting greenText = registerBoolean("Green Text", false);
    // Unformatted
    BooleanSetting unFormattedText = registerBoolean("Unformatted Text", false);
    // Time
    BooleanSetting chatTimeStamps = registerBoolean("Chat Time Stamp", false);
    ModeSetting format = registerMode("Format", Arrays.asList("H24:mm", "H12:mm", "H12:mm a", "H24:mm:ss", "H12:mm:ss", "H12:mm:ss a"), "H24:mm");
    ModeSetting decoration = registerMode("Deco", Arrays.asList("< >", "[ ]", "{ }", " "), "[ ]");
    public ColorSetting timeColor = registerColor("Time Color", new GSColor(85,255,255));
    BooleanSetting specialTime = registerBoolean("Special Color Time", false);
    BooleanSetting space = registerBoolean("Space", false);
    // Player name
    BooleanSetting customName = registerBoolean("Custom Name", true);
    public ColorSetting friendColor = registerColor("Friend Color", new GSColor(85,255,255));
    BooleanSetting specialFriend = registerBoolean("Special Color Friend", false);
    public ColorSetting enemyColor = registerColor("Enemy Color", new GSColor(85,255,255));
    public ColorSetting playerColor = registerColor("Player Color", new GSColor(85,255,255));
    // Watermark special
    public BooleanSetting watermarkSpecial = registerBoolean("Watermark Special", true);
    // Colors
    public ColorSetting aqua = registerColor("Aqua", new GSColor(85,255,255));
    public ColorSetting black = registerColor("Black", new GSColor(0, 0, 0));
    public ColorSetting blue = registerColor("Blue", new GSColor(85,85,255));
    public ColorSetting dark_aqua = registerColor("Dark Aqua", new GSColor(0,170,170));
    public ColorSetting dark_blue = registerColor("Dark Blue", new GSColor(0,0,170));
    public ColorSetting dark_cyan = registerColor("Dark Cyan", new GSColor(0,170,170));
    public ColorSetting dark_gray = registerColor("Dark Gray", new GSColor(85,85,85));
    public ColorSetting dark_green = registerColor("Dark Green", new GSColor(0,170,0));
    public ColorSetting dark_purple = registerColor("Dark Purple", new GSColor(170,0,170));
    public ColorSetting dark_red = registerColor("Dark Red", new GSColor(170,0,0));
    public ColorSetting gray = registerColor("Gray", new GSColor(170,170,170));
    public ColorSetting green = registerColor("Green", new GSColor(85,255,85));
    public ColorSetting gold = registerColor("Gold", new GSColor(255,170,0));
    public ColorSetting yellow = registerColor("Yellow", new GSColor(255,255,85));
    public ColorSetting purple = registerColor("Purple", new GSColor(255,85,255));
    public ColorSetting red = registerColor("Red", new GSColor(255,85,85));
    public ColorSetting white = registerColor("White", new GSColor(255,255,255));

    public float clamp(float number, float min, float max) {
        return number < min ? min : Math.min(number, max);
    }


    boolean iniz = false;

    @Override
    protected void onEnable() {
        iniz = false;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) {
            return;
        }
        // Replace minecraft chat with our chat (https://www.curseforge.com/minecraft/mc-mods/better-chat)
        if (!iniz) {
            ObfuscationReflectionHelper.setPrivateValue(GuiIngame.class, Minecraft.getMinecraft().ingameGUI, new NewChat(Minecraft.getMinecraft()), "field_73840_e");
            iniz = true;
        }
    }

    // Reset chat to the default one
    @Override
    protected void onDisable() {
        ObfuscationReflectionHelper.setPrivateValue(GuiIngame.class, Minecraft.getMinecraft().ingameGUI, new GuiNewChat(Minecraft.getMinecraft()), "field_73840_e");
    }

    // When recived a message
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<ClientChatReceivedEvent> chatReceivedEventListener = new Listener<>(event -> {
        // Get it
        ITextComponent output = event.getMessage();
        try {
            // if custom name
            if (customName.getValue()) {
                // Get name
                String name = event.getMessage().getUnformattedText().split(" ")[0];
                // Add his special color
                output = new TextComponentString((
                        isFriend(name) ? (specialFriend.getValue() ?
                                "\u2063" : "\u2064") :
                                isEnemy(name) ? "\u2065" :
                                        // he is normal
                                        "\u2066"
                )
                        + name + ChatFormatting.RESET + output.getFormattedText().substring(output.getFormattedText().split(" ")[0].length()));
            }
            // If we have to add the time
            if (chatTimeStamps.getValue()) {
                // Add deco
                String decoLeft = decoration.getValue().equalsIgnoreCase(" ") ? "" : decoration.getValue().split(" ")[0];
                String decoRight = decoration.getValue().equalsIgnoreCase(" ") ? "" : decoration.getValue().split(" ")[1];
                if (space.getValue()) decoRight += " ";
                // Add date
                String dateFormat = format.getValue().replace("H24", "k").replace("H12", "h");
                String date = new SimpleDateFormat(dateFormat).format(new Date());
                // If we have, add time
                TextComponentString time = new TextComponentString(
                        (specialTime.getValue() ? "\u2063" : "\u2067")
                                + decoLeft + date + decoRight + TextFormatting.RESET);
                output = time.appendSibling(output);
            }
            // Get unformatted text
            if (unFormattedText.getValue())
                output = new TextComponentString(output.getUnformattedText());
        // Preventing dumb things when the server send to us an empty message (WHY)
        } catch (ArrayIndexOutOfBoundsException e) {

        }
        // Set the enw message
        event.setMessage(output);
    });

    // Get if he is a friend. We use contains because well, i wanna even if it is in a word
    private boolean isFriend(String name) {
        name = name.toLowerCase();
        for (Friend friend : SocialManager.getFriends()) {
            if (name.contains(friend.getName().toLowerCase())) {
                return true;
            }
        }
        return false;

    }

    // Get if he is a Enemy. We use contains because well, i wanna even if it is in a word
    private boolean isEnemy(String name) {
        name = name.toLowerCase();
        for (Enemy enemy : SocialManager.getEnemies()) {
            if (name.contains(enemy.getName().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // When we send a message
    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        // If greentext
        if (greenText.getValue()) {
            if (event.getPacket() instanceof CPacketChatMessage) {
                // If it's a command
                if (((CPacketChatMessage) event.getPacket()).getMessage().startsWith("/") || ((CPacketChatMessage) event.getPacket()).getMessage().startsWith(CommandManager.getCommandPrefix()))
                    return;
                // Add it
                String message = ((CPacketChatMessage) event.getPacket()).getMessage();
                String prefix = "";
                prefix = ">";
                String s = prefix + message;
                // Only if it's not above 255
                if (s.length() > 255) return;
                // And set message
                ((CPacketChatMessage) event.getPacket()).message = s;
            }
        }
    });
}