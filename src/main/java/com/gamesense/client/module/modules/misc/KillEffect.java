package com.gamesense.client.module.modules.misc;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.modules.combat.PistonCrystal;
import com.gamesense.mixin.mixins.accessor.AccessorCPacketAttack;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

@Module.Declaration(name = "KillEffect", category = Category.Misc, enabled = true)
public class KillEffect extends Module {

    BooleanSetting thunder = registerBoolean("Thunder", true);
    IntegerSetting numbersThunder = registerInteger("Number Thunder", 1, 1, 10);
    BooleanSetting sound = registerBoolean("Sound", true);
    IntegerSetting numberSound = registerInteger("Number Sound", 1, 1, 10);
    BooleanSetting circle = registerBoolean("Circle", false);
    DoubleSetting startRay = registerDouble("Start Ray", 0, 0, 5);
    DoubleSetting increaseRay = registerDouble("Increase Ray", 0.1, 0, 2);
    IntegerSetting life = registerInteger("Life", 1000, 0, 5000);
    BooleanSetting multipleCircle = registerBoolean("Multiple Circle", false);
    IntegerSetting nCircles = registerInteger("N^ Circles", 2, 1, 5);
    IntegerSetting firstDelay = registerInteger("First Delay", 800, 0, 2000);
    IntegerSetting othersDelay = registerInteger("Others Delay", 200, 0, 1000);
    ColorSetting color = registerColor("Circle Color", new GSColor(255, 255, 255, 255), () -> true, true);
    BooleanSetting rainbowCircle = registerBoolean("Rainbow Circle", false);
    IntegerSetting stepRainbowCircle = registerInteger("Step Rainbow Circle", 1, 1, 100);

    static class circleRender {
        final Vec3d center;
        double ray;
        final double increaseRay;
        final long startLife;
        final int life;
        final GSColor color;
        final boolean rainbowColor;
        final int rainbowStep;

        public circleRender(double posX, double posY, double posZ, double ray, double increaseRay, int life, GSColor color, boolean rainbowColor, int rainbowStep) {
            this.center = new Vec3d(posX, posY, posZ);
            this.ray = ray;
            this.startLife = System.currentTimeMillis();
            this.increaseRay = increaseRay;
            this.life = life;
            this.color = color;
            this.rainbowColor = rainbowColor;
            this.rainbowStep = rainbowStep;
        }

        public boolean update() {
            ray += increaseRay;
            return System.currentTimeMillis() - startLife <= life;
        }

        void render() {
            if (rainbowColor) {
                RenderUtil.drawCircle((float) center.x, (float) center.y, (float) center.z, ray, rainbowStep, color.getAlpha());
            } else
           RenderUtil.drawCircle((float) center.x, (float) center.y, (float) center.z, ray, color);
        }
    }

    ArrayList<EntityPlayer> playersDead = new ArrayList<>();
    ArrayList<circleRender> circleList = new ArrayList<>();

    final Object sync = new Object();


    @Override
    protected void onEnable() {
        playersDead.clear();
    }

    @Override
    public void onUpdate() {

        if (mc.world == null) {
            playersDead.clear();
            return;
        }

        for(int i = 0; i < circleList.size(); i++)
            if (!circleList.get(i).update()) {
                synchronized (sync) {
                    circleList.remove(i);
                }
                i--;
            }

        mc.world.playerEntities.forEach(entity -> {
            if (playersDead.contains(entity)) {
                if (entity.getHealth() > 0)
                    playersDead.remove(entity);
            } else {
                if (entity.getHealth() == 0) {
                    if (thunder.getValue())
                        for(int i = 0; i < numbersThunder.getValue(); i++)
                            mc.world.spawnEntity(new EntityLightningBolt(mc.world, entity.posX, entity.posY, entity.posZ, true));
                    if (sound.getValue())
                        for(int i = 0; i < numberSound.getValue(); i++)
                            mc.player.playSound(SoundEvents.ENTITY_LIGHTNING_THUNDER, 0.5f, 1.f);
                    playersDead.add(entity);

                    if (circle.getValue()) {
                        synchronized (sync) {
                            circleList.add(new circleRender(entity.posX, entity.posY, entity.posZ, startRay.getValue(), increaseRay.getValue(), life.getValue(), color.getValue(), rainbowCircle.getValue(), stepRainbowCircle.getValue()));
                        }
                        if (multipleCircle.getValue()) {
                            for(int i = 0; i < nCircles.getValue(); i++) {
                                int delay = firstDelay.getValue() + i * othersDelay.getValue();

                                java.util.Timer t = new java.util.Timer();
                                t.schedule(
                                        new java.util.TimerTask() {
                                            @Override
                                            public void run() {
                                                synchronized (sync) {
                                                    circleList.add(new circleRender(entity.posX, entity.posY, entity.posZ, startRay.getValue(), increaseRay.getValue(), life.getValue(), color.getValue(), rainbowCircle.getValue(), stepRainbowCircle.getValue()));
                                                }
                                            }
                                        },
                                        delay
                                );
                            }
                        }
                    }
                }
            }
        });


    }

    @Override
    public void onWorldRender(RenderEvent event) {
        try {
            circleList.forEach(circleRender::render);
        }catch (ConcurrentModificationException ignored) {

        }
    }
}
