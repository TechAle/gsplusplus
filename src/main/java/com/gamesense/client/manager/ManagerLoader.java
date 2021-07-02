package com.gamesense.client.manager;

import java.util.ArrayList;
import java.util.List;

import com.gamesense.client.GameSense;
import com.gamesense.client.manager.managers.*;

import net.minecraftforge.common.MinecraftForge;

public class ManagerLoader {

    private static final List<Manager> managers = new ArrayList<>();

    public static void init() {
        register(AutoCrystalManager.INSTANCE);
        register(ClientEventManager.INSTANCE);
        register(PlayerPacketManager.INSTANCE);
        register(EntityTrackerManager.INSTANCE);
        register(TotemPopManager.INSTANCE);
        register(WorldCopyManager.INSTANCE);
    }

    private static void register(Manager manager) {
        managers.add(manager);
        GameSense.EVENT_BUS.subscribe(manager);
        MinecraftForge.EVENT_BUS.register(manager);
    }
}