package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.TotemPopEvent;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.mojang.authlib.GameProfile;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.GameType;

import java.util.*;


@Module.Declaration(name = "PopChams", category = Category.Render)
public class PopChams extends Module {

    IntegerSetting life = registerInteger("Time", 100, 10, 300);


    private int fpNum = 0;
    List<Integer> fakePlayers = new ArrayList<>();

    ArrayList<Entity> toSpawn = new ArrayList<>();

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<TotemPopEvent> totemPopEventListener = new Listener<>(event -> {
        toSpawn.add(event.getEntity());
    });

    @Override
    public void onUpdate() {

        if (mc.world == null || mc.player == null)
            toSpawn.clear();

        toSpawn.removeIf(this::spawnPlayer);

        for(int i = 0; i < fakePlayers.size(); i++) {

            try {
                if (mc.world.getEntityByID(fakePlayers.get(i)).ticksExisted > life.getValue()) {
                    mc.world.removeEntityFromWorld(fakePlayers.get(i));
                    fakePlayers.remove(fakePlayers.get(i));
                    i--;
                }
            }catch (NullPointerException e) {
                fakePlayers.remove(fakePlayers.get(i));
                i--;
            }
        }


    }

    boolean spawnPlayer(Entity entity) {
        // Clone empty player
        EntityOtherPlayerMP clonedPlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("fdee323e-7f0c-4c15-8d1c-0f277442342a"), ""));
        // Copy angles
        clonedPlayer.copyLocationAndAnglesFrom(entity);
        clonedPlayer.rotationYawHead = entity.getRotationYawHead();
        clonedPlayer.rotationYaw = entity.rotationYaw;
        clonedPlayer.rotationPitch = entity.rotationPitch;
        /// Trying to make others ca not target this
        // idk maybe some ca not considerate spectator
        clonedPlayer.setGameType(GameType.SPECTATOR);
        clonedPlayer.setHealth(20);
        // Add resistance for 0 damage
        clonedPlayer.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 100, 100, false, false));
        // Add armor for not making some ca target this because "naked"
        final ItemStack[] armors = new ItemStack[]{
                new ItemStack(Items.DIAMOND_BOOTS),
                new ItemStack(Items.DIAMOND_LEGGINGS),
                new ItemStack(Items.DIAMOND_CHESTPLATE),
                new ItemStack(Items.DIAMOND_HELMET)
        };
        for (int i = 0; i < 4; i++) {
            // Create base
            ItemStack item = armors[i];
            // Add enchants
            item.addEnchantment(
                    i == 2 ? Enchantments.BLAST_PROTECTION : Enchantments.PROTECTION,
                    50);
            // Add it to the player
            clonedPlayer.inventory.armorInventory.set(i, item);
        }
        // Add entity id
        mc.world.addEntityToWorld((-1235 - fpNum), clonedPlayer);
        clonedPlayer.onLivingUpdate();
        fakePlayers.add((-1235 - fpNum));
        fpNum++;
        return true;
    }


    }
