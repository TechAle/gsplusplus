package com.gamesense.client.manager.managers;

import com.gamesense.api.util.world.combatRewrite.ac.ACSettings;
import com.gamesense.api.util.world.combatRewrite.ac.CrystalInfo;
import com.gamesense.client.manager.Manager;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.combat.AutoCrystalRewrite2;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.ConcurrentHashMap;

public enum AutoCrystalManager implements Manager {

    INSTANCE;

    // stores all the locations we have attempted to place crystals
    // and the corresponding crystal for that location (if there is any)
    private final ConcurrentHashMap<BlockPos, Boolean> placedCrystals = new ConcurrentHashMap<>();

    private volatile ACSettings settings = null;
    private volatile CrystalInfo.PlaceInfo renderInfo = null;

    public void onPlaceCrystal(BlockPos target) {
        BlockPos up = target.up();
        placedCrystals.put(up, Boolean.FALSE);
    }

    public void onBreakCrystal(BlockPos target) {
        placedCrystals.remove(target);
    }

    public boolean isOwn(BlockPos crystals) {
        return this.placedCrystals.containsKey(crystals);
    }

    public void onDisable() {
        this.placedCrystals.clear();
        renderInfo = null;
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<TickEvent.ClientTickEvent> onUpdate = new Listener<>(event -> {
        Minecraft mc = getMinecraft();
        if (mc.player == null || mc.world == null) {
            placedCrystals.clear();
            return;
        }

        this.settings = ModuleManager.getModule(AutoCrystalRewrite2.class).getSettings();
    });

    public ACSettings getSettings() {
        return settings;
    }

    public CrystalInfo.PlaceInfo getRenderInfo() {
        return renderInfo;
    }

    public void setRenderInfo(CrystalInfo.PlaceInfo renderInfo) {
        this.renderInfo = renderInfo;
    }
}
