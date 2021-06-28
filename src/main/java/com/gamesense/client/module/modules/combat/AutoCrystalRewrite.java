package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.OnUpdateWalkingPlayerEvent;
import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Arrays;
import java.util.Comparator;

@Module.Declaration(name = "AutoCrystalRewrite", category = Category.Combat, priority = 100)
public class AutoCrystalRewrite extends Module {

    ModeSetting logic = registerMode("Logic", Arrays.asList("Place->Break", "Break->Place", "Place", "Break"), "Place->Break");
    ModeSetting targetPlacing = registerMode("Target Placing", Arrays.asList("Nearest", "Lowest", "Damage"), "Nearest");
    ModeSetting targetBreaking = registerMode("Target Breaking", Arrays.asList("Nearest", "Lowest", "Damage"), "Nearest");
    DoubleSetting rangeEnemy = registerDouble("RangeEnemy", 7, 0, 12);

    public static boolean stopAC = false;

    EntityPlayer target;

    @Override
    public void onUpdate() {
        if (mc.world == null || mc.player == null || mc.player.isDead || stopAC) return;

        switch (logic.getValue()) {
            case "Place->Break":
                placeCrystals();
                breakCrystals();
                break;
            case "Break->Place":
                breakCrystals();
                placeCrystals();
                break;
            case "Place":
                placeCrystals();
                break;
            case "Break":
                breakCrystals();
                break;
        }

    }

    void placeCrystals() {

    }

    void breakCrystals() {

    }

    EntityPlayer getTarget(String mode, boolean placing) {
        switch (mode) {
            case "Lowest":
                return mc.world.playerEntities.stream()
                        .filter(entity -> entity.getDistanceSq(entity) <= rangeEnemy.getValue())
                        .filter(entity -> !EntityUtil.basicChecksEntity(entity))
                        .filter(entity -> entity.getHealth() > 0.0f).min((x, y) -> (int) x.getHealth()).orElse(null);
            case "Nearest":
                return mc.world.playerEntities.stream()
                        .filter(entity -> entity.getDistanceSq(entity) <= rangeEnemy.getValue())
                        .filter(entity -> !EntityUtil.basicChecksEntity(entity))
                        .filter(entity -> entity.getHealth() > 0.0f).min(Comparator.comparingDouble(x -> x.getDistanceSq(mc.player))).orElse(null);
            case "Damage":
                // If we are placing
                if (placing) {

                // If we are breaking
                } else {

                }
                break;
        }
        return mc.player;
    }



    public void onWorldRender(RenderEvent event) {

    }


    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {

    });

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Receive> packetReceiveListener = new Listener<>(event -> {

    });

    public void onEnable() {

    }

    public void onDisable() {

    }

    public String getHudInfo() {
        String t = "";

        return t;
    }
}