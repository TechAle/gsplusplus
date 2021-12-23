package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEntityEvent;
import com.gamesense.api.event.events.TotemPopEvent;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.render.ChamsUtil;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.mojang.authlib.GameProfile;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.GameType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Techale
 * @author Hoosiers
 */

@Module.Declaration(name = "PopChams", category = Category.Render)
public class PopChams extends Module {

    IntegerSetting range = registerInteger("Range", 100, 10, 260);
    ModeSetting chamsPopType = registerMode("Chams Type Pop", Arrays.asList("Color", "WireFrame"), "WireFrame");
    ColorSetting chamsColor = registerColor("Chams Color", new GSColor(255, 255, 255, 255), () -> true, true);
    IntegerSetting wireFramePop = registerInteger("WireFrame Pop", 4, 0, 10);
    BooleanSetting gradientAlpha = registerBoolean("Gradient Alpha", true);
    ModeSetting Movement = registerMode("Movement", Arrays.asList("None", "Heaven", "Hell"), "None");
    DoubleSetting yMovement = registerDouble("Y Movement", .2, 0, 1, () -> !Movement.getValue().equals("None"));
    IntegerSetting life = registerInteger("Time", 100, 10, 300);


    private int fpNum = 0;

    ArrayList<Entity> toSpawn = new ArrayList<>();

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<TotemPopEvent> totemPopEventListener = new Listener<>(event -> {
        if (event.getEntity() != null)
            toSpawn.add(event.getEntity());
    });

    @Override
    public void onUpdate() {

        if (mc.world == null || mc.player == null)
            toSpawn.clear();

        toSpawn.removeIf(this::spawnPlayer);

        for(int i = 0; i < listPlayers.size(); i++) {
            if (listPlayers.get(i).onUpdate()) {
                try {
                    mc.world.removeEntityFromWorld(listPlayers.get(i).id);
                }catch (NullPointerException ignored) {
                }
                listPlayers.remove(i);
                i--; // -1237
            } else spawnPlayer(listPlayers.get(i).id, listPlayers.get(i).coordinates);
        }


    }


    boolean spawnPlayer(Entity entity) {
        if (entity != null) {
            // Add entity id
            //mc.world.addEntityToWorld((-1235 - fpNum), clonedPlayer);
            double movement = 0;
            switch (Movement.getValue()) {
                case "Heaven":
                    movement = yMovement.getValue();
                    break;
                case "Hell":
                    movement = -yMovement.getValue();
                    break;
            }
            listPlayers.add(new playerChams(-1235 - fpNum, life.getValue(), new double[] {entity.posX, entity.posY, entity.posZ}, movement));
            fpNum++;
        }
        return true;
    }

    void spawnPlayer(int num, double[] positions) {
        // Clone empty player
        EntityOtherPlayerMP clonedPlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("fdee323e-7f0c-4c15-8d1c-0f277442342a"), ""));
        // Copy angles
        clonedPlayer.setPosition(positions[0], positions[1], positions[2]);
        /// Trying to make others ca not target this
        // idk maybe some ca not considerate spectator
        clonedPlayer.setGameType(GameType.SPECTATOR);
        clonedPlayer.isSpectator();
        clonedPlayer.setHealth(20);
        // Add resistance for 0 damage
        clonedPlayer.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 100, 100, false, false));
        mc.world.addEntityToWorld(num, clonedPlayer);
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<RenderEntityEvent.Head> renderEntityHeadEventListener = new Listener<>(event -> {
        if (event.getType() == RenderEntityEvent.Type.COLOR && chamsPopType.getValue().equalsIgnoreCase("Texture")) {
            return;
        } else if (event.getType() == RenderEntityEvent.Type.TEXTURE && (chamsPopType.getValue().equalsIgnoreCase("Color") || chamsPopType.getValue().equalsIgnoreCase("WireFrame"))) {
            return;
        } else if (event.getEntity() == null || event.getEntity().getName().length() > 0)
            return;

        if (mc.player == null || mc.world == null) {
            return;
        }

        Entity entity1 = event.getEntity();

        if (entity1.getDistance(mc.player) > range.getValue()) {
            return;
        }

        if (entity1 instanceof EntityPlayer && entity1 != mc.player) {
            renderChamsPopPre(entity1);
        }

    });

    static class playerChams {
        private int tick;
        final private int finalTick;
        final private int id;
        final double[] coordinates;
        final double movement;

        public playerChams(int id, int finalTick, double[] coordinates, double movement) {
            this.id = id;
            this.finalTick = finalTick;
            this.tick = 0;
            this.coordinates = coordinates;
            this.movement = movement;
        }

        public boolean onUpdate() {
            coordinates[1] += movement;
            return tick++ > finalTick;
        }

        public int returnGradient() {
            return 250 - ((int) (tick / (float) finalTick * 250));
        }
    }

    ArrayList<playerChams> listPlayers = new ArrayList<>();

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<RenderEntityEvent.Return> renderEntityReturnEventListener = new Listener<>(event -> {
        if (event.getType() == RenderEntityEvent.Type.COLOR && chamsPopType.getValue().equalsIgnoreCase("Texture")) {
            return;
        } else if (event.getType() == RenderEntityEvent.Type.TEXTURE && (chamsPopType.getValue().equalsIgnoreCase("Color") || chamsPopType.getValue().equalsIgnoreCase("WireFrame"))) {
            return;
        }

        if (mc.player == null || mc.world == null) {
            return;
        }

        Entity entity1 = event.getEntity();

        if (entity1.getDistance(mc.player) > range.getValue()) {
            return;
        }

        if (entity1 instanceof EntityPlayer && entity1 != mc.player) {
            renderChamsPopPost();
        }

        if (entity1.getName().equals(""))
            mc.world.removeEntityFromWorld(entity1.getEntityId());
        int a = 0;
    });

    private void renderChamsPopPre(Entity player) {

        int alpha = chamsColor.getColor().getAlpha();
        if (gradientAlpha.getValue()) {
            Optional<playerChams> prova = listPlayers.stream().filter(e -> e.id == player.entityId).findAny();
            if (prova.isPresent()) {
                alpha = prova.get().returnGradient();
            }
        }

        if (alpha < 0)
            alpha = 0;

        switch (chamsPopType.getValue()) {
            case "Color": {
                ChamsUtil.createColorPre(new GSColor(chamsColor.getColor(), alpha), true);
                break;
            }
            case "WireFrame": {
                ChamsUtil.createWirePre(new GSColor(chamsColor.getColor(), alpha), wireFramePop.getValue(), true);
                break;
            }
        }
    }

    private void renderChamsPopPost() {
        switch (chamsPopType.getValue()) {
            case "Color": {
                ChamsUtil.createColorPost(true);
                break;
            }
            case "WireFrame": {
                ChamsUtil.createWirePost(true);
                break;
            }
        }
    }


}