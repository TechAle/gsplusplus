package com.gamesense.client.manager.managers;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityInfo;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.EntityLivingInfo;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.InfoCreator;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.PlayerInfo;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.objects.PaintingInfo;
import com.gamesense.client.manager.Manager;
import com.gamesense.mixin.mixins.accessor.ISPacketEntity;
import com.gamesense.mixin.mixins.accessor.ISPacketEntityStatus;
import com.gamesense.mixin.mixins.accessor.ISPacketUseBed;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public enum EntityTrackerManager implements Manager {

    INSTANCE;

    private final ConcurrentHashMap<Integer, PlayerInfo> players = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, EntityInfo> all = new ConcurrentHashMap<>();

    public List<PlayerInfo> getPlayerInfo() {
        return new ArrayList<>(players.values());
    }

    public List<EntityInfo> getEntitiesInAABB(AxisAlignedBB aabb) {
        return all.values().stream().filter(entityInfo -> entityInfo.aabb.intersects(aabb)).collect(Collectors.toList());
    }

    public List<EntityInfo> getEntitiesInRange(Vec3d center, double range) {
        double finalRange = range * range;
        return all.values().stream().filter(entityInfo -> entityInfo.position.squareDistanceTo(center) < finalRange).collect(Collectors.toList());
    }

    public void addPlayer(PlayerInfo player) {
        players.put(player.entityID, player);
    }

    public void removePlayer(int playerID) {
        players.remove(playerID);
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<PacketEvent.Receive> packetReceiveListener = new Listener<>(event -> {
        Packet<?> packet = event.getPacket();
        if (packet instanceof SPacketJoinGame || packet instanceof SPacketRespawn) {
            this.players.clear();
            this.all.clear();
        } else if (packet instanceof SPacketSpawnPlayer) {
            SPacketSpawnPlayer packetSpawnPlayer = (SPacketSpawnPlayer) packet;
            int id = packetSpawnPlayer.getEntityID();
            PlayerInfo player = new PlayerInfo(packetSpawnPlayer);
            this.players.put(id, player);
            this.all.put(id, player);
        } else if (packet instanceof SPacketSpawnMob) {
            SPacketSpawnMob packetSpawnMob = (SPacketSpawnMob) packet;
            int type = packetSpawnMob.getEntityType();
            int id = packetSpawnMob.getEntityID();
            EntityLivingInfo entity = InfoCreator.createMob(packetSpawnMob);
            this.all.put(id, entity);
        } else if (packet instanceof SPacketSpawnObject) {
            SPacketSpawnObject packetSpawnObject = (SPacketSpawnObject) packet;
            int id = packetSpawnObject.getEntityID();
            EntityInfo entity = InfoCreator.createObject(packetSpawnObject);
            this.all.put(id, entity);
        } else if (packet instanceof SPacketSpawnExperienceOrb) {
            SPacketSpawnExperienceOrb packetSpawnExperienceOrb = (SPacketSpawnExperienceOrb) packet;
            int id = packetSpawnExperienceOrb.getEntityID();
            EntityInfo entity = new EntityInfo(id);
            entity.width = 0.5D;
            entity.height = 0.5D;
            entity.updateSize();
            this.all.put(id, entity);
        } else if (packet instanceof SPacketSpawnPainting) {
            SPacketSpawnPainting packetSpawnPainting = (SPacketSpawnPainting) packet;
            int id = packetSpawnPainting.getEntityID();
            EntityInfo entity = new PaintingInfo(packetSpawnPainting);
            this.all.put(id, entity);
        } else if (packet instanceof SPacketUseBed) {
            SPacketUseBed packetUseBed = (SPacketUseBed) packet;
            BlockPos bedLocation = packetUseBed.getBedPosition();
            PlayerInfo player = this.players.get(((ISPacketUseBed) packetUseBed).getPlayerID());
            if (player != null) {
                player.isSleeping = true;
                player.updatePosition(bedLocation.getX() + 0.5D, bedLocation.getY() + 0.6875D, bedLocation.getZ() + 0.5D);

            }
        } else if (packet instanceof SPacketEntityEquipment) {
            SPacketEntityEquipment packetEntityEquipment = (SPacketEntityEquipment) packet;
            EntityEquipmentSlot slot = packetEntityEquipment.getEquipmentSlot();
            if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                EntityInfo entity = this.all.get(packetEntityEquipment.getEntityID());
                if (entity instanceof EntityLivingInfo) {
                    ((EntityLivingInfo) entity).updateArmour(slot.getIndex(), packetEntityEquipment.getItemStack());
                }
            }
        } else if (packet instanceof SPacketAnimation) {
            SPacketAnimation packetAnimation = (SPacketAnimation) packet;
            PlayerInfo player = this.players.get(packetAnimation.getEntityID());
            if (packetAnimation.getAnimationType() == 2) {
                player.isSleeping = false;
            }
        } else if (packet instanceof SPacketEntityMetadata) {
            SPacketEntityMetadata packetEntityMetadata = (SPacketEntityMetadata) packet;
            EntityInfo entity = this.all.get(packetEntityMetadata.getEntityId());
            if (entity != null) {
                entity.dataManager.setEntryValues(packetEntityMetadata.getDataManagerEntries());
                entity.updateSize();
            }
        } else if (packet instanceof SPacketEntityProperties) {
            SPacketEntityProperties packetEntityProperties = (SPacketEntityProperties) packet;
            EntityInfo entity = this.all.get(packetEntityProperties.getEntityId());
            if (entity instanceof EntityLivingInfo) {
                ((EntityLivingInfo) entity).updateAttributeMap(packetEntityProperties.getSnapshots());
            }
        } else if (packet instanceof SPacketEntityTeleport) {
            SPacketEntityTeleport packetEntityTeleport = (SPacketEntityTeleport) packet;
            EntityInfo entity = this.all.get(packetEntityTeleport.getEntityId());
            if (entity != null) {
                entity.updatePosition(packetEntityTeleport.getX(), packetEntityTeleport.getY(), packetEntityTeleport.getZ());
            }
        // TODO: rotations
        } else if (packet instanceof SPacketEntity) {
            SPacketEntity packetEntity = (SPacketEntity) packet;
            EntityInfo entity = this.all.get(((ISPacketEntity) packetEntity).getEntityID());
            if (entity != null) {
                entity.updateServerPosition(packetEntity.getX(), packetEntity.getY(), packetEntity.getZ());
            }
        } else if (packet instanceof SPacketSetPassengers) {
            SPacketSetPassengers packetSetPassengers = (SPacketSetPassengers) packet;
            EntityInfo entity = this.all.get(packetSetPassengers.getEntityId());
            if (entity != null) {
                entity.removeAllPassengers();
                for (int passengerId : packetSetPassengers.getPassengerIds()) {
                    entity.addPassenger(this.all.get(passengerId));
                }
            }
        // TODO: EntityInfo.onRemove
        } else if (packet instanceof SPacketDestroyEntities) {
            SPacketDestroyEntities packetDestroyEntities = (SPacketDestroyEntities) packet;
            for (int entityID : packetDestroyEntities.getEntityIDs()) {
                EntityInfo entityInfo = all.get(entityID);
                if (entityInfo != null) {
                    entityInfo.onRemove();
                }
                players.remove(entityID);
                all.remove(entityID);
            }
        // TODO: EntityInfo.onRemove
        } else if (packet instanceof SPacketEntityStatus) {
            SPacketEntityStatus packetEntityStatus = (SPacketEntityStatus) packet;
            byte code = packetEntityStatus.getOpCode();
            if (code == 3) {
                int entityID = ((ISPacketEntityStatus) packetEntityStatus).getEntityID();
                EntityInfo entityInfo = all.get(entityID);
                if (entityInfo != null) {
                    entityInfo.onRemove();
                }
                players.remove(entityID);
                all.remove(entityID);
            }
        }
    });

}
