package com.gamesense.api.util.world.combatRewrite.ac.entityData;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;

import static net.minecraft.entity.Entity.FLAGS;
import static net.minecraft.entity.player.EntityPlayer.ABSORPTION;
import static net.minecraft.entity.player.EntityPlayer.MAIN_HAND;

public class PlayerInfo extends EntityLivingInfo {
    private static final Potion RESISTANCE = Potion.getPotionById(11);

    public final GameProfile gameProfile;

    public volatile boolean hasResistance = false;

    public volatile boolean isSleeping = false;

    public PlayerInfo(@Nonnull EntityPlayer entity, float armorPercent) {
        super(entity.entityId);
        this.gameProfile = entity.getGameProfile();

        EntityDataManager dataManager = entity.dataManager;
        this.dataManager.setEntry(ABSORPTION, dataManager.get(ABSORPTION));
        this.dataManager.setEntry(MAIN_HAND, dataManager.get(MAIN_HAND));
        this.dataManager.setEntry(FLAGS, dataManager.get(FLAGS));

        this.dataManager.setEntry(HEALTH, dataManager.get(HEALTH));

        this.hasResistance = entity.isPotionActive(RESISTANCE);

        int i = 0;
        for (ItemStack stack : entity.getArmorInventoryList()) {
            this.updateArmour(i, stack);
            ++i;
        }

        this.updateAttributeMap(entity.getAttributeMap().getAllAttributes());

        this.isSleeping = entity.isPlayerSleeping();
        this.updatePosition(entity.posX, entity.posY, entity.posZ);
    }

    public PlayerInfo(@Nonnull SPacketSpawnPlayer player) {
        super(player.getEntityID());

        GameProfile gameProfile1 = null;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.networkManager != null) {
            INetHandler iNetHandler = mc.networkManager.getNetHandler();
            if (iNetHandler instanceof NetHandlerPlayClient) {
                NetHandlerPlayClient handlerPlayClient = (NetHandlerPlayClient) iNetHandler;
                gameProfile1 = handlerPlayClient.getPlayerInfo(player.getUniqueId()).getGameProfile();
            }
        }
        this.gameProfile = gameProfile1 == null ? new GameProfile(player.getUniqueId(), "") : gameProfile1;

        this.dataManager.setEntryValues(player.getDataManagerEntries());

        this.updatePosition(player.getX(), player.getY(), player.getZ());
        //this.recalculateArmourValues();
    }

    public PlayerInfo(@Nonnull SPacketJoinGame player) {
        super(player.getPlayerId());

        GameProfile gameProfile1 = null;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.networkManager != null) {
            INetHandler iNetHandler = mc.networkManager.getNetHandler();
            if (iNetHandler instanceof NetHandlerPlayClient) {
                NetHandlerPlayClient handlerPlayClient = (NetHandlerPlayClient) iNetHandler;
                gameProfile1 = handlerPlayClient.getGameProfile();
            }
        }
        this.gameProfile = gameProfile1;

        this.updatePosition(0, 0, 0);
        //this.recalculateArmourValues();
    }

    @Override
    protected void setupDataManager() {
        super.setupDataManager();

        this.dataManager.setEntry(ABSORPTION, 0.0F);
        this.dataManager.setEntry(MAIN_HAND, (byte) 1);
    }

    public float getAbsorption() {
        return this.dataManager.getEntryData(ABSORPTION);
    }

    public String getName() {
        return gameProfile.getName();
    }

    public void updateSize() {
        double width = 0.6D;
        double height = 1.8D;

        // ElytraFlying
        if (this.getFlag(7)) {
            height = 0.6D;
        // Sleeping
        } else if (isSleeping) {
            width = 0.2D;
            height = 0.2D;
        // Sneaking
        } else if (this.getFlag(1)) {
            height = 1.65D;
        }

        double halfWidth = width / 2.0D;
        this.aabb = new AxisAlignedBB(position.x - halfWidth, position.y, position.z - halfWidth, position.x + halfWidth, position.y + height, position.z + halfWidth);
    }

    public float getEyeHeight() {
        float eyeHeight = 1.62F;

        if (isSleeping) {
            eyeHeight = 0.2F;
        } else if (this.getFlag(1) && this.height != 1.65F) {
            if (this.getFlag(7) || this.height == 0.6F) {
                eyeHeight = 0.4F;
            }
        } else {
            eyeHeight -= 0.08F;
        }

        return eyeHeight;
    }

    public double getYOffset() {
        return -0.35D;
    }
}
