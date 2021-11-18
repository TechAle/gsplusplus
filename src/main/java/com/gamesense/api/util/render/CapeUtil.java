package com.gamesense.api.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.texture.DynamicTexture;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CapeUtil {

    private static final List<UUID> uuids = new ArrayList<>();
    public static final List<ResourceLocation> capes = new ArrayList<>();
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void init() {

        try {
            capes.add(mc.getTextureManager().getDynamicTextureLocation("black", new DynamicTexture(ImageIO.read(new URL("https://i.toxicaven.dev/Bm11ZriMSjHn/direct.png")))));
            capes.add(mc.getTextureManager().getDynamicTextureLocation("white", new DynamicTexture(ImageIO.read(new URL("https://i.toxicaven.dev/hiHyRHocHDQD/direct.png")))));
            capes.add(mc.getTextureManager().getDynamicTextureLocation("amber", new DynamicTexture(ImageIO.read(new URL("https://i.toxicaven.dev/2XtPEM75HImX/direct.png")))));
            URL capesList = new URL("https://raw.githubusercontent.com/TechAle/gsplusplus-assets/main/capeslist.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(capesList.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                uuids.add(UUID.fromString(inputLine));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasCape(UUID id) {
        return uuids.contains(id);
    }
}