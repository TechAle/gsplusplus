package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.GeometryMasks;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.modules.combat.KillAura;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;

@Module.Declaration(name = "ChorusViewer", category = Category.Render)
public class ChorusViewer extends Module {

    ModeSetting render = registerMode("Render", Arrays.asList("None", "Rectangle", "Circle"), "None");
    IntegerSetting life = registerInteger("Life", 300, 0, 1000, () -> !render.getValue().equals("None"));
    DoubleSetting circleRange = registerDouble("Circle Range", 1, 0, 3);
    ColorSetting color = registerColor("Color", new GSColor(255, 255, 255, 255), () -> true, true);
    BooleanSetting desyncCircle = registerBoolean("Desync Circle", false);
    IntegerSetting stepRainbowCircle = registerInteger("Step Rainbow Circle", 1, 1, 100);
    BooleanSetting increaseHeight = registerBoolean("Increase Height", true);
    DoubleSetting speedIncrease = registerDouble("Speed Increase", 0.01, 0.3, 0.001);

    static class renderClass {
        final Vec3d center;
        long start;
        final long life;
        final String mode;
        final double circleRange;
        final GSColor color;
        final boolean desyncCircle;
        final int stepRainbowCircle;
        final double range;
        final int desync;
        final boolean increaseHeight;
        final double speedIncrease;
        double nowHeigth = 0;
        boolean up = true;


        public renderClass(Vec3d center, long life, String mode, GSColor color, double circleRange, boolean desyncCircle, int stepRainbowCircle, double range, int desync, boolean increaseHeight, double speedIncrease) {
            this.center = center;
            this.increaseHeight = increaseHeight;
            this.speedIncrease = speedIncrease;
            this.range = range;
            start = System.currentTimeMillis();
            this.life = life;
            this.mode = mode;
            this.desync = desync;
            this.circleRange = circleRange;
            this.color = color;
            this.desyncCircle = desyncCircle;
            this.stepRainbowCircle = stepRainbowCircle;
        }

        boolean update() {
            return System.currentTimeMillis() - start > life;
        }

        void render() {
            switch (mode) {
                case "Rectangle":
                    // BlockPos blockPos, double height, GSColor color, int sides
                    RenderUtil.drawBox(new BlockPos(center.x, center.y, center.z), 1.8, color, GeometryMasks.Quad.ALL);
                    break;
                case "Circle":
                    double inc = 0;
                    if (increaseHeight) {
                        nowHeigth += speedIncrease * (up ? 1 : -1);
                        if (nowHeigth > 1.8)
                            up = false;
                        else if (nowHeigth < 0)
                            up = true;
                        inc = nowHeigth;
                    }
                    if (desyncCircle) {
                        RenderUtil.drawCircle((float) center.x, (float) (center.y + inc), (float) center.z, range, desync, color.getAlpha());
                    } else {
                        RenderUtil.drawCircle((float) center.x, (float) (center.y + inc), (float) center.z, range, color);
                    }
                    break;
            }
        }
    }

    ArrayList<renderClass> toRender = new ArrayList<>();

    @EventHandler
    private final Listener<PacketEvent.Receive> sendListener = new Listener<>(event -> {
        if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect soundPacket = (SPacketSoundEffect) event.getPacket();
            if (soundPacket.getSound() == SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT) {
                toRender.add(new renderClass(new Vec3d(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ()), life.getValue(), render.getValue(), color.getValue(), circleRange.getValue(), desyncCircle.getValue(), stepRainbowCircle.getValue(), circleRange.getValue(), stepRainbowCircle.getValue(), increaseHeight.getValue(), speedIncrease.getValue()));
            }
        }

    });


    @Override
    public void onWorldRender(RenderEvent event) {
        if (mc.world == null || mc.player == null)
            return;
        for(int i = 0; i < toRender.size(); i++)
            if (toRender.get(i).update()) {
                toRender.remove(i);
                i--;
            }
        toRender.forEach(renderClass::render);

    }
}