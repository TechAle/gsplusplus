package com.gamesense.client.module.modules.misc;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.Packet;
import net.minecraft.network.login.server.SPacketEnableCompression;
import net.minecraft.network.login.server.SPacketEncryptionRequest;
import net.minecraft.network.login.server.SPacketLoginSuccess;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.server.SPacketPong;
import net.minecraft.network.status.server.SPacketServerInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

@Module.Declaration(name = "PacketLogger", category = Category.Misc)
public class PacketLogger extends Module {

    BooleanSetting incoming = registerBoolean("Receive", true);
    BooleanSetting AdvancementInfo = registerBoolean("SPacketAdvancementInfo", false, () -> incoming.getValue());
    BooleanSetting SAnimation = registerBoolean("SPacketAnimation", false, () -> incoming.getValue());
    BooleanSetting SBlockAction = registerBoolean("SPacketBlockAction", false, () -> incoming.getValue());
    BooleanSetting SBlockBreakAnim = registerBoolean("SPacketBlockBreakAnim", false, () -> incoming.getValue());
    BooleanSetting SBlockChange = registerBoolean("SPacketBlockChange", false, () -> incoming.getValue());
    BooleanSetting SCamera = registerBoolean("SPacketCamera", false, () -> incoming.getValue());
    BooleanSetting SChat = registerBoolean("SPacketChat", false, () -> incoming.getValue());
    BooleanSetting SCooldown = registerBoolean("SPacketCooldown", false, () -> incoming.getValue());
    BooleanSetting SChunkData = registerBoolean("SPacketChunkData", false, () -> incoming.getValue());
    BooleanSetting SChangeGameState = registerBoolean("SPacketChangeGameState", false, () -> incoming.getValue());
    BooleanSetting SCloseWindow = registerBoolean("SPacketCloseWindow", false, () -> incoming.getValue());
    BooleanSetting SCollectItem = registerBoolean("SPacketCollectItem", false, () -> incoming.getValue());
    BooleanSetting SCombatEvent = registerBoolean("SPacketCombatEvent", false, () -> incoming.getValue());
    BooleanSetting SConfirmTransaction = registerBoolean("SPacketConfirmTransaction", false, () -> incoming.getValue());
    BooleanSetting SCustomPayload = registerBoolean("SPacketCustomPayload", false, () -> incoming.getValue());
    BooleanSetting SCustomSound = registerBoolean("SPacketCustomSound", false, () -> incoming.getValue());
    BooleanSetting SDestroyEntities = registerBoolean("SPacketDestroyEntities", false, () -> incoming.getValue());
    BooleanSetting SDisconnect = registerBoolean("SPacketDisconnect", false, () -> incoming.getValue());
    BooleanSetting SDisplayObjective = registerBoolean("SPacketDisplayObjective", false, () -> incoming.getValue());
    BooleanSetting SEffect = registerBoolean("SPacketEffect", false, () -> incoming.getValue());
    BooleanSetting SEntity = registerBoolean("SPacketEntity", false, () -> incoming.getValue());
    BooleanSetting SEntityAttach = registerBoolean("SPacketEntityAttach", false, () -> incoming.getValue());
    BooleanSetting SEntityEffect = registerBoolean("SPacketEntityEffect", false, () -> incoming.getValue());
    BooleanSetting SEntityEquipment = registerBoolean("SPacketEntityEquipment", false, () -> incoming.getValue());
    BooleanSetting SEntityHeadLook = registerBoolean("SPacketEntityHeadLook", false, () -> incoming.getValue());
    BooleanSetting SEntityMetadata = registerBoolean("SPacketEntityMetadata", false, () -> incoming.getValue());
    BooleanSetting SEntityProperties = registerBoolean("SPacketEntityProperties", false, () -> incoming.getValue());
    BooleanSetting SEntityStatus = registerBoolean("SPacketEntityStatus", false, () -> incoming.getValue());
    BooleanSetting SEntityTeleport = registerBoolean("SPacketEntityTeleport", false, () -> incoming.getValue());
    BooleanSetting SEntityVelocity = registerBoolean("SPacketEntityVelocity", false, () -> incoming.getValue());
    BooleanSetting SExplosion = registerBoolean("SPacketExplosion", false, () -> incoming.getValue());
    BooleanSetting SEnableCompression = registerBoolean("SPacketEnableCompression", false, () -> incoming.getValue());
    BooleanSetting SEncryptionRequest = registerBoolean("SPacketEncryptionRequest", false, () -> incoming.getValue());
    BooleanSetting SHeldItemChange = registerBoolean("SPacketHeldItemChange", false, () -> incoming.getValue());
    BooleanSetting SJoinGame = registerBoolean("SPacketJoinGame", false, () -> incoming.getValue());
    BooleanSetting SKeepAlive = registerBoolean("SPacketKeepAlive", false, () -> incoming.getValue());
    BooleanSetting SLoginSuccess = registerBoolean("SPacketLoginSuccess", false, () -> incoming.getValue());
    BooleanSetting SMaps = registerBoolean("SPacketMaps", false, () -> incoming.getValue());
    BooleanSetting SMoveVehicle = registerBoolean("SPacketMoveVehicle", false, () -> incoming.getValue());
    BooleanSetting SMultiBlockChange = registerBoolean("SPacketMultiBlockChange", false, () -> incoming.getValue());
    BooleanSetting SOpenWindow = registerBoolean("SPacketOpenWindow", false, () -> incoming.getValue());
    BooleanSetting SParticles = registerBoolean("SPacketParticles", false, () -> incoming.getValue());
    BooleanSetting SPlayerAbilities = registerBoolean("SPacketPlayerAbilities", false, () -> incoming.getValue());
    BooleanSetting SPlayerListHeaderFooter = registerBoolean("SPacketPlayerListHeaderFooter", false, () -> incoming.getValue());
    BooleanSetting SPlayerListItem = registerBoolean("SPacketPlayerListItem", false, () -> incoming.getValue());
    BooleanSetting SPlayerPosLook = registerBoolean("SPacketPlayerPosLook", false, () -> incoming.getValue());
    BooleanSetting SPong = registerBoolean("SPacketPong", false, () -> incoming.getValue());
    BooleanSetting SRecipeBook = registerBoolean("SPacketRecipeBook", false, () -> incoming.getValue());
    BooleanSetting SRespawn = registerBoolean("SPacketRespawn", false, () -> incoming.getValue());
    BooleanSetting SRemoveEntityEffect = registerBoolean("SPacketRemoveEntityEffect", false, () -> incoming.getValue());
    BooleanSetting SScoreboardObjective = registerBoolean("SPacketScoreboardObjective", false, () -> incoming.getValue());
    BooleanSetting SServerDifficulty = registerBoolean("SPacketServerDifficulty", false, () -> incoming.getValue());
    BooleanSetting SSelectAdvancementsTab = registerBoolean("SPacketSelectAdvancementsTab", false, () -> incoming.getValue());
    BooleanSetting SServerInfo = registerBoolean("SPacketServerInfo", false, () -> incoming.getValue());
    BooleanSetting SSetExperience = registerBoolean("SPacketSetExperience", false, () -> incoming.getValue());
    BooleanSetting SSetPassengers = registerBoolean("SPacketSetPassengers", false, () -> incoming.getValue());
    BooleanSetting SSetSlot = registerBoolean("SPacketSetSlot", false, () -> incoming.getValue());
    BooleanSetting SSignEditorOpen = registerBoolean("SPacketSignEditorOpen", false, () -> incoming.getValue());
    BooleanSetting SSoundEffect = registerBoolean("SPacketSoundEffect", false, () -> incoming.getValue());
    BooleanSetting SSpawnGlobalEntity = registerBoolean("SPacketSpawnGlobalEntity", false, () -> incoming.getValue());
    BooleanSetting SSpawnMob = registerBoolean("SPacketSpawnMob", false, () -> incoming.getValue());
    BooleanSetting SSpawnPlayer = registerBoolean("SPacketSpawnPlayer", false, () -> incoming.getValue());
    BooleanSetting SSpawnExperienceOrb = registerBoolean("SPacketSpawnExperienceOrb", false, () -> incoming.getValue());
    BooleanSetting SSpawnPainting = registerBoolean("SPacketSpawnPainting", false, () -> incoming.getValue());
    BooleanSetting SSpawnObject = registerBoolean("SPacketSpawnObject", false, () -> incoming.getValue());
    BooleanSetting SSpawnPosition = registerBoolean("SPacketSpawnPosition", false, () -> incoming.getValue());
    BooleanSetting STabComplete = registerBoolean("SPacketTabComplete", false, () -> incoming.getValue());
    BooleanSetting SUnloadChunk = registerBoolean("SPacketUnloadChunk", false, () -> incoming.getValue());
    BooleanSetting SUseBed = registerBoolean("SPacketUseBed", false, () -> incoming.getValue());
    BooleanSetting SUpdateHealth = registerBoolean("SPacketUpdateHealth", false, () -> incoming.getValue());


    BooleanSetting outgoing = registerBoolean("Outgoing", true);
    BooleanSetting CAnimation = registerBoolean("CPacketAnimation", false, () -> outgoing.getValue());
    BooleanSetting CChatMessage = registerBoolean("CPacketChatMessage", false, () -> outgoing.getValue());
    BooleanSetting CClickWindow = registerBoolean("CPacketClickWindow", false, () -> outgoing.getValue());
    BooleanSetting CConfirmTeleport = registerBoolean("CPacketConfirmTeleport", false, () -> outgoing.getValue());
    BooleanSetting CClientStatus = registerBoolean("CPacketClientStatus", false, () -> outgoing.getValue());
    BooleanSetting CCustomPayload = registerBoolean("CPacketCustomPayload", false, () -> outgoing.getValue());
    BooleanSetting CCreativeInventoryAction = registerBoolean("CPacketCreativeInventoryAction", false, () -> outgoing.getValue());

    
    BooleanSetting printChat = registerBoolean("Print Chat", false);
    BooleanSetting logFile = registerBoolean("Log File", false);
    BooleanSetting showTick = registerBoolean("Show Tick", false);
    BooleanSetting separator = registerBoolean("Separator", false);
    
    int tick;

    @Override
    protected void onEnable() {
        tick = 0;
        file = new StringBuilder();
    }

    @Override
    public void onUpdate() {
        tick++;
    }

    @Override
    protected void onDisable() {
        if (logFile.getValue()) {
            try {
                // Save stringBuilder to file
                if (!Files.exists(Paths.get("gs++"))) {
                    Files.createDirectories(Paths.get("gs++"));
                }
                if (!Files.exists(Paths.get("gs++/logs"))) {
                    Files.createDirectories(Paths.get("gs++/logs"));
                }

                OutputStreamWriter fileOutputStreamWriter = new OutputStreamWriter(new FileOutputStream("gs++/logs/" + System.currentTimeMillis() + ".txt"), StandardCharsets.UTF_8);
                fileOutputStreamWriter.write(file.toString());
                fileOutputStreamWriter.close();
            }catch (IOException e) {

            }
        }
    }

    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if(!outgoing.getValue())return;
        Packet<?> pack = event.getPacket();
        if(pack instanceof CPacketAnimation && CAnimation.getValue()){
            CPacketAnimation s = (CPacketAnimation) pack;
            sendMessage("CPacketAnimation"
                    +"\n - Hand name: " + s.getHand().name()
            );
        } else if(pack instanceof CPacketChatMessage && CChatMessage.getValue()){
            CPacketChatMessage s = (CPacketChatMessage) pack;
            sendMessage("CPacketChatMessage"
                    +"\n - Message: " + s.message
            );
        } else if(pack instanceof CPacketClickWindow && CClickWindow.getValue()){
            CPacketClickWindow s = (CPacketClickWindow) pack;
            sendMessage("CPacketClickWindow"
                    +"\n - Acton Number: " + s.getActionNumber()
                    +"\n - Window ID: " + s.getWindowId()
                    +"\n - Item Name: " + s.getClickedItem().getDisplayName()
                    +"\n - Click Type Name: " + s.getClickType().name()
            );
        } else if(pack instanceof CPacketConfirmTeleport && CConfirmTeleport.getValue()){
            CPacketConfirmTeleport s = (CPacketConfirmTeleport) pack;
            sendMessage("CPacketConfirmTeleport"
                    +"\n - Tp id: " + s.getTeleportId()
            );
        } else if(pack instanceof CPacketClientStatus && CClientStatus.getValue()){
            CPacketClientStatus s = (CPacketClientStatus) pack;
            sendMessage("CPacketClientStatus"
                    +"\n - Status Name: " + s.getStatus().name()
            );
        } else if(pack instanceof CPacketCustomPayload && CCustomPayload.getValue()){
            CPacketCustomPayload s = (CPacketCustomPayload) pack;
            sendMessage("CPacketCustomPayload"
                    +"\n - Channel: " + s.getChannelName()
                    +"\n - Data: " + s.getBufferData().readString(10000)
            );
        } else if(pack instanceof CPacketCreativeInventoryAction && CCreativeInventoryAction.getValue()){
            CPacketCreativeInventoryAction s = (CPacketCreativeInventoryAction) pack;
            sendMessage("CPacketCreativeInventoryAction"
                    +"\n - Item name: " + s.getStack().getDisplayName()
                    +"\n - Slot Id: " + s.getSlotId()
            );
        }
    });



    @EventHandler
    private final Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (!incoming.getValue()) return;
        Packet<?> pack = event.getPacket();
        if (pack instanceof SPacketAdvancementInfo && AdvancementInfo.getValue()) {
            SPacketAdvancementInfo s = (SPacketAdvancementInfo) pack;
            sendMessage("SPacketAdvancementInfo:\n"
                    + " -Is First Sync: " + s.isFirstSync());
        } else if (pack instanceof SPacketAnimation && SAnimation.getValue()) {
            SPacketAnimation s = (SPacketAnimation) pack;
            sendMessage("SPacketAnimation:\n" +
                    " - Animation Type: " + s.getAnimationType()
                    + "\n - Entity Id: " + s.getEntityID()
            );
        } else if (pack instanceof SPacketCamera && SCamera.getValue()) {
            SPacketCamera s = (SPacketCamera) pack;
            try {
                //noinspection ConstantConditions
                sendMessage("SPacketCamera:\n" +
                        " - Entity name: " + s.getEntity(mc.world).getName()
                        + "\n - Entity Id: " + s.entityId
                );
            }catch (NullPointerException e) {
                sendMessage("SPacketCamera:\n" +
                        " - Entity name: null"
                        + "\n - Entity Id: " + s.entityId
                );
            }
        } else if (pack instanceof SPacketChat && SChat.getValue()) {
            SPacketChat s = (SPacketChat) pack;
            sendMessage("SPacketChat:\n" +
                    " - Chat Type: " + s.type.name()
                    + "\n - Formatted Text: " + s.chatComponent.getFormattedText()
            );
        } else if (pack instanceof SPacketBlockAction && SBlockAction.getValue()) {
            SPacketBlockAction s = (SPacketBlockAction) pack;
            sendMessage("SPacketBlockAction:\n" +
                    " - Block Type Name: " + s.getBlockType().getLocalizedName()
                    + "\n - Block Type: " + s.getBlockType()
                    + "\n - Block Pos: " + s.getBlockPosition()
                    + "\n - Data1: " + s.getData1()
                    + "\n - Data2: " + s.getData2()
            );
        } else if (pack instanceof SPacketBlockBreakAnim && SBlockBreakAnim.getValue()) {
            SPacketBlockBreakAnim s = (SPacketBlockBreakAnim) pack;
            sendMessage("SPacketBlockBreakAnim:\n" +
                    " - Break Id: " + s.getBreakerId()
                    + "\n - Block Pos: " + s.getPosition()
                    + "\n - Progress: " + s.getProgress()
            );
        } else if (pack instanceof SPacketBlockChange && SBlockChange.getValue()) {
            SPacketBlockChange s = (SPacketBlockChange) pack;
            sendMessage("SPacketBlockChange:\n" +
                    " - Block Pos: " + s.getBlockPosition()
                    + "\n - Block Name: " + s.blockState.getBlock().getLocalizedName()
                    + "\n - Block State: " + s.getBlockState()
            );
        } else if (pack instanceof SPacketCooldown && SCooldown.getValue()) {
            SPacketCooldown s = (SPacketCooldown) pack;
            sendMessage("SPacketCooldown:\n" +
                    " - Item: " + s.getItem()
                    + "\n - Ticks: " + s.getTicks()
            );
        } else if (pack instanceof SPacketChunkData && SChunkData.getValue()) {
            SPacketChunkData s = (SPacketChunkData) pack;
            sendMessage("SPacketChunkData:\n" +
                    " - Chunk Pos: " + s.getChunkX() + " " + s.getChunkZ()
            );
        } else if (pack instanceof SPacketChangeGameState && SChangeGameState.getValue()) {
            SPacketChangeGameState s = (SPacketChangeGameState) pack;
            sendMessage("SPacketChangeGameState:\n" +
                    " - Game State Value: " + s.getValue()
                    + "\n - Game State: " + s.getGameState()
            );
        } else if (pack instanceof SPacketCloseWindow && SCloseWindow.getValue()) {
            sendMessage("SPacketCloseWindow" );
        } else if (pack instanceof SPacketCollectItem && SCollectItem.getValue()) {
            SPacketCollectItem s = (SPacketCollectItem) pack;
            sendMessage("SPacketCollectItem:\n" +
                    " - Entity ID: " + s.getEntityID()
                    + "\n - Amount: " + s.getAmount()
                    + "\n - Collected Item Id: " + s.getCollectedItemEntityID()
            );
        } else if (pack instanceof SPacketCombatEvent && SCombatEvent.getValue()) {
            SPacketCombatEvent s = (SPacketCombatEvent) pack;
            sendMessage("SPacketCombatEvent:\n" +
                    " - Entity ID: " + s.entityId
                    + "\n - Player Id: " + s.playerId
                    + "\n - Event Name: " + s.eventType.name()
                    + "\n - Duration: " + s.duration
                    + "\n - Death Message: " + s.deathMessage.getFormattedText()
            );
        } else if (pack instanceof SPacketConfirmTransaction && SConfirmTransaction.getValue()) {
            SPacketConfirmTransaction s = (SPacketConfirmTransaction) pack;
            sendMessage("SPacketConfirmTransaction:\n" +
                    " - Action Number: " + s.getActionNumber()
                    + "\n - Window Id: " + s.getWindowId()
                    + "\n - Was Accepted: " + s.wasAccepted()
            );
        } else if (pack instanceof SPacketCustomPayload && SCustomPayload.getValue()) {
            SPacketCustomPayload s = (SPacketCustomPayload) pack;
            sendMessage("SPacketCustomPayload:\n" +
                    " - Channel Name: " + s.getChannelName()
                    + "\n - Buffer Data: " + s.getBufferData().readString(1000)
            );
        } else if (pack instanceof SPacketCustomSound && SCustomSound.getValue()) {
            SPacketCustomSound s = (SPacketCustomSound) pack;
            sendMessage("SPacketCustomSound:\n" +
                    " - Sound Name: " + s.getSoundName()
                    + "\n - Sound Category: " + s.getCategory().getName()
                    + "\n - Sound Pos: " + s.getX() + " " + s.getY() + " " + s.getZ()
                    + "\n - Sound Pitch: " + s.getPitch()
                    + "\n - Sound Volume: " + s.getVolume()
            );
        } else if (pack instanceof SPacketDestroyEntities && SDestroyEntities.getValue()) {
            SPacketDestroyEntities s = (SPacketDestroyEntities) pack;
            sendMessage("SPacketDestroyEntities:\n" );
            Arrays.stream(s.getEntityIDs()).forEach(id -> sendMessage("Removed Id: " + id));
        } else if (pack instanceof SPacketDisconnect && SDisconnect.getValue()) {
            SPacketDisconnect s = (SPacketDisconnect) pack;
            sendMessage("SPacketDisconnect:\n" +
                    " - Disconnect Reason: " + s.getReason().getFormattedText()
            );
        } else if (pack instanceof SPacketDisplayObjective && SDisplayObjective.getValue()) {
            SPacketDisplayObjective s = (SPacketDisplayObjective) pack;
            sendMessage("SPacketDisplayObjective:\n" +
                    " - Objective Name: " + s.getName()
                    + "\n - Objective Pos: " + s.getPosition()
            );
        } else if (pack instanceof SPacketEffect && SEffect.getValue()) {
            SPacketEffect s = (SPacketEffect) pack;
            sendMessage("SPacketEffect:\n" +
                    " - Sound Data: " + s.getSoundData()
                    + "\n - Sound Pos: " + s.getSoundPos()
                    + "\n - Sound Type: " + s.getSoundType()
                    + "\n - Is Sound Server Wide: " + s.isSoundServerwide()
            );
        } else if (pack instanceof SPacketEntity && SEntity.getValue()) {
            SPacketEntity s = (SPacketEntity) pack;
            sendMessage("SPacketEntity:\n" +
                    " - Entity Name: " + s.getEntity(mc.world).getName()
                    + "\n - Entity Id: " + s.getEntity(mc.world).entityId
                    + "\n - Entity Pitch: " + s.getPitch()
                    + "\n - Is Entity OnGround: " + s.getOnGround()
                    + "\n - Entity Yaw: " + s.getYaw()
                    + "\n - Entity Pos: " + s.getX() + " " + s.getY() + " " + s.getZ()
            );
        } else if (pack instanceof SPacketEntityAttach && SEntityAttach.getValue()) {
            SPacketEntityAttach s = (SPacketEntityAttach) pack;
            sendMessage("SPacketEntityAttach:\n" +
                    " - Entity Id: " + s.getEntityId()
                    + "\n - Entity Vehicle Id: " + s.getVehicleEntityId()
            );
        } else if (pack instanceof SPacketEntityEffect && SEntityEffect.getValue()) {
            SPacketEntityEffect s = (SPacketEntityEffect) pack;
            sendMessage("SPacketEntityEffect:\n" +
                    " - Entity Id: " + s.getEntityId()
                    + "\n - Effect Amplifier: " + s.getAmplifier()
                    + "\n - Effect ID: " + s.getEffectId()
                    + "\n - Effect Duration: " + s.getDuration()
                    + "\n - Is Effect Ambient: " + s.getIsAmbient()
            );
        } else if (pack instanceof SPacketEntityEquipment && SEntityEquipment.getValue()) {
            SPacketEntityEquipment s = (SPacketEntityEquipment) pack;
            sendMessage("SPacketEntityEquipment:\n" +
                    " - Entity Id: " + s.getEntityID()
                    + "\n - Equipment Slot Name: " + s.getEquipmentSlot().getName()
                    + "\n - Item Name: " + s.getItemStack().getDisplayName()
            );
        } else if (pack instanceof SPacketEntityHeadLook && SEntityHeadLook.getValue()) {
            SPacketEntityHeadLook s = (SPacketEntityHeadLook) pack;
            sendMessage("SPacketEntityHeadLook:\n" +
                    " - Entity Id: " + s.getEntity(mc.world).entityId
                    + "\n - Entity Name: " + s.getEntity(mc.world).getName()
                    + "\n - Yaw: " + s.getYaw()
            );
        } else if (pack instanceof SPacketEntityMetadata && SEntityMetadata.getValue()) {
            SPacketEntityMetadata s = (SPacketEntityMetadata) pack;
            sendMessage("SPacketEntityMetadata:\n" +
                    " - Entity Id: " + s.getEntityId()
            );
        } else if (pack instanceof SPacketEntityProperties && SEntityProperties.getValue()) {
            SPacketEntityProperties s = (SPacketEntityProperties) pack;
            sendMessage("SPacketEntityProperties:\n" +
                    " - Entity Id: " + s.getEntityId()
            );
        } else if (pack instanceof SPacketEntityStatus && SEntityStatus.getValue()) {
            SPacketEntityStatus s = (SPacketEntityStatus) pack;
            sendMessage("SPacketEntityStatus:\n" +
                    " - Entity Id: " + s.getEntity(mc.world).getEntityId()
                    + "\n - Entity Name: " + s.getEntity(mc.world).getName()
                    + "\n - Entity OP code: " + s.getOpCode()
            );
        } else if (pack instanceof SPacketEntityTeleport && SEntityTeleport.getValue()) {
            SPacketEntityTeleport s = (SPacketEntityTeleport) pack;
            sendMessage("SPacketEntityTeleport:\n" +
                    " - Entity Id: " + s.getEntityId()
                    + "\n - Entity Pos: " + s.getX() + " " + s.getY() + " " + s.getZ()
                    + "\n - Entity Yaw: " + s.getYaw()
                    + "\n - Entity Pitch: " + s.getPitch()
                    + "\n - Is Entity On Ground: " + s.getOnGround()
            );
        } else if (pack instanceof SPacketEntityVelocity && SEntityVelocity.getValue()) {
            SPacketEntityVelocity s = (SPacketEntityVelocity) pack;
            sendMessage("SPacketEntityVelocity:\n" +
                    " - Entity Id: " + s.getEntityID()
                    + "\n - MotionX: " + s.motionX
                    + "\n - MotionY: " + s.motionY
                    + "\n - MotionZ: " + s.motionZ
            );
        } else if (pack instanceof SPacketExplosion && SExplosion.getValue()) {
            SPacketExplosion s = (SPacketExplosion) pack;
            sendMessage("SPacketExplosion:\n" +
                    " - Explosion Pos: " + s.posX + " " + s.getY() + " " + s.getZ()
                    + "\n - MotionX: " + s.motionX
                    + "\n - MotionY: " + s.motionY
                    + "\n - MotionZ: " + s.motionZ
                    + "\n - Strength: " + s.getStrength()
            );
        } else if (pack instanceof SPacketEnableCompression && SEnableCompression.getValue()) {
            SPacketEnableCompression s = (SPacketEnableCompression) pack;
            sendMessage("SPacketEnableCompression:\n" +
                    " - Compression Threshold: " + s.getCompressionThreshold()
            );
        } else if (pack instanceof SPacketEncryptionRequest && SEncryptionRequest.getValue()) {
            SPacketEncryptionRequest s = (SPacketEncryptionRequest) pack;
            sendMessage("SPacketEncryptionRequest:\n" +
                    " - Server Id: " + s.getServerId()
                    + "\n - Public key: " + s.getPublicKey()
            );
        } else if (pack instanceof SPacketHeldItemChange && SHeldItemChange.getValue()) {
            SPacketHeldItemChange s = (SPacketHeldItemChange) pack;
            sendMessage("SPacketEncryptionRequest:\n" +
                    " - Held Item Hotbar Index: " + s.getHeldItemHotbarIndex()
            );
        } else if (pack instanceof SPacketJoinGame && SJoinGame.getValue()) {
            SPacketJoinGame s = (SPacketJoinGame) pack;
            sendMessage("SPacketJoinGame:\n" +
                    " - Player ID: " + s.getPlayerId()
                    + "\n - Difficulty: " + s.getDifficulty().name()
                    + "\n - Dimension: " + s.getDimension()
                    + "\n - Game Type: " + s.getGameType().getName()
                    + "\n - World Type: " + s.getWorldType().getName()
                    + "\n - Max Players: " + s.getMaxPlayers()
                    + "\n - Is Hardcore Mode: " + s.isHardcoreMode()
            );
        } else if (pack instanceof SPacketKeepAlive && SKeepAlive.getValue()) {
            SPacketKeepAlive s = (SPacketKeepAlive) pack;
            sendMessage("SPacketKeepAlive:\n" +
                    " - ID: " + s.getId()
            );
        } else if (pack instanceof SPacketLoginSuccess && SLoginSuccess.getValue()) {
            SPacketLoginSuccess s = (SPacketLoginSuccess) pack;
            sendMessage("SPacketLoginSuccess:\n" +
                    " - Name: " + s.getProfile().getName()
            );
        } else if (pack instanceof SPacketMaps && SMaps.getValue()) {
            SPacketMaps s = (SPacketMaps) pack;
            sendMessage("SPacketMaps:\n" +
                    " - Map ID: " + s.getMapId()
            );
        } else if (pack instanceof SPacketMoveVehicle && SMoveVehicle.getValue()) {
            SPacketMoveVehicle s = (SPacketMoveVehicle) pack;
            sendMessage("SPacketMoveVehicle:\n" +
                    " - Pitch: " + s.getPitch()
                    + "\n - Yaw: " + s.getYaw()
                    + "\n - Pos: " + s.getX() + " " + s.getY() + " " + s.getZ()
            );
        } else if (pack instanceof SPacketMultiBlockChange && SMultiBlockChange.getValue()) {
            SPacketMultiBlockChange s = (SPacketMultiBlockChange) pack;
            sendMessage("SPacketMultiBlockChange " + "" +
                    "\n " + Arrays.toString(s.getChangedBlocks())
            );
        } else if (pack instanceof SPacketOpenWindow && SOpenWindow.getValue()) {
            SPacketOpenWindow s = (SPacketOpenWindow) pack;
            sendMessage("SPacketOpenWindow:"
                    + "\n - Gui ID: " + s.getGuiId()
                    + "\n - Entity ID: " + s.getEntityId()
                    + "\n - Window ID: " + s.getWindowId()
                    + "\n - Window Title: " + s.getWindowTitle()
                    + "\n - Slot Count: " + s.getSlotCount()
            );
        } else if (pack instanceof SPacketParticles && SParticles.getValue()) {
            SPacketParticles s = (SPacketParticles) pack;
            sendMessage("SPacketParticles:"
                    + "\n - Particle Count: " + s.getParticleCount()
                    + "\n - Particle Speed: " + s.getParticleSpeed()
                    + "\n - Particle Name: " + s.getParticleType().getParticleName()
                    + "\n - Pos: " + s.getXCoordinate() + " " + s.getYCoordinate() + " " + s.getZCoordinate()
            );
        } else if (pack instanceof SPacketPlayerAbilities && SPlayerAbilities.getValue()) {
            SPacketPlayerAbilities s = (SPacketPlayerAbilities) pack;
            sendMessage("SPacketPlayerAbilities:"
                    + "\n - Walk Speed: " + s.getWalkSpeed()
                    + "\n - Fly Speed: " + s.getFlySpeed()
                    + "\n - Is Allow Flying: " + s.isAllowFlying()
                    + "\n - Is Creative Mode: " + s.isCreativeMode()
                    + "\n - Is Flying: " + s.isFlying()
                    + "\n - Is Flying: " + s.isInvulnerable()
            );
        } else if (pack instanceof SPacketPlayerListHeaderFooter && SPlayerListHeaderFooter.getValue()) {
            SPacketPlayerListHeaderFooter s = (SPacketPlayerListHeaderFooter) pack;
            sendMessage("SPacketPlayerListHeaderFooter:"
                    + "\n - Footer: " + s.getFooter().getFormattedText()
                    + "\n - Header: " + s.getHeader()
            );
        } else if (pack instanceof SPacketPlayerListItem && SPlayerListItem.getValue()) {
            SPacketPlayerListItem s = (SPacketPlayerListItem) pack;
            sendMessage("SPacketPlayerListItem:"
                    + "\n - Action Name: " + s.getAction().name()
            );
        } else if (pack instanceof SPacketPlayerPosLook && SPlayerPosLook.getValue()) {
            SPacketPlayerPosLook s = (SPacketPlayerPosLook) pack;
            sendMessage("SPacketPlayerPosLook:"
                    + "\n - Pitch: " + s.getPitch()
                    + "\n - Yaw: " + s.getYaw()
                    + "\n - Pos: " + s.getX() + " " + s.getY() + " " + s.getZ()
                    + "\n - Teleport ID: " + s.getTeleportId()
            );
        } else if (pack instanceof SPacketPong && SPong.getValue()) {
            sendMessage("SPacketPong" );
        } else if (pack instanceof SPacketRecipeBook && SRecipeBook.getValue()) {
            SPacketRecipeBook s = (SPacketRecipeBook) pack;
            sendMessage("SPacketRecipeBook " +
                    "\n" + s.getDisplayedRecipes().toString() +
                    "\n" + s.getRecipes().toString() );
        } else if (pack instanceof SPacketRespawn && SRespawn.getValue()) {
            SPacketRespawn s = (SPacketRespawn) pack;
            sendMessage("SPacketRecipeBook: "
                    + "\n - Dimension ID " + s.getDimensionID()
                    + "\n - WorldType Name " + s.getWorldType().getName()
                    + "\n - Difficulty " + s.getDifficulty().name()
                    + "\n - GameType name " + s.getGameType().name()
            );
        } else if (pack instanceof SPacketRemoveEntityEffect && SRemoveEntityEffect.getValue()) {
            SPacketRemoveEntityEffect s = (SPacketRemoveEntityEffect) pack;
            try {
                //noinspection ConstantConditions
                sendMessage("SPacketRemoveEntityEffect: "
                        + "\n - Entity Name " + s.getEntity(mc.world).getName()
                        + "\n - Potion Name " + s.getPotion().getName()
                        + "\n - Entity ID " + s.getEntity(mc.world).getEntityId()
                );
            }catch (NullPointerException e) {
                sendMessage("SPacketRemoveEntityEffect: "
                        + "\n - Entity Name null"
                        + "\n - Potion Name null" +
                        "\n - Entity ID null"
                );
            }
        } else if (pack instanceof SPacketScoreboardObjective && SScoreboardObjective.getValue()) {
            SPacketScoreboardObjective s = (SPacketScoreboardObjective) pack;
            sendMessage("SPacketScoreboardObjective: "
                    + "\n - Objective Name " + s.getObjectiveName()
                    + "\n - Acton " + s.getAction()
                    + "\n - Render Type Name" + s.getRenderType().name()
            );
        } else if (pack instanceof SPacketServerDifficulty && SServerDifficulty.getValue()) {
            SPacketServerDifficulty s = (SPacketServerDifficulty) pack;
            sendMessage("SPacketServerDifficulty: "
                    + "\n - Difficulty Name " + s.getDifficulty().name()
            );
        } else if (pack instanceof SPacketSelectAdvancementsTab && SSelectAdvancementsTab.getValue()) {
            SPacketSelectAdvancementsTab s = (SPacketSelectAdvancementsTab) pack;
            try {
                //noinspection ConstantConditions
                sendMessage("SPacketSelectAdvancementsTab " + "" +
                        "\n" + s.getTab().toString());
            }catch (NullPointerException e) {
                sendMessage("SPacketSelectAdvancementsTab null");
            }
        } else if (pack instanceof SPacketServerInfo && SServerInfo.getValue()) {
            SPacketServerInfo s = (SPacketServerInfo) pack;
            sendMessage("SPacketServerInfo: "
                    + "\n - Server Info " + s.getResponse().getJson()
            );
        } else if (pack instanceof SPacketSetExperience && SSetExperience.getValue()) {
            SPacketSetExperience s = (SPacketSetExperience) pack;
            sendMessage("SPacketSetExperience: "
                    + "\n - Experience Bar " + s.getExperienceBar()
                    + "\n - Total Experience " + s.getTotalExperience()
                    + "\n - Level " + s.getLevel()
            );
        } else if (pack instanceof SPacketSetPassengers && SSetPassengers.getValue()) {
            SPacketSetPassengers s = (SPacketSetPassengers) pack;
            sendMessage("SPacketSetPassengers: "
                    + "\n - Entity ID " + s.getEntityId()
                    + "\n - Passengers ID " + Arrays.toString(s.getPassengerIds())
            );
        } else if (pack instanceof SPacketSetSlot && SSetSlot.getValue()) {
            SPacketSetSlot s = (SPacketSetSlot) pack;
            sendMessage("SPacketSetSlot: "
                    + "\n - Window ID " + s.getWindowId()
                    + "\n - Slot " + s.getSlot()
                    + "\n - Item Name " + s.getStack().getDisplayName()
            );
        } else if (pack instanceof SPacketSignEditorOpen && SSignEditorOpen.getValue()) {
            SPacketSignEditorOpen s = (SPacketSignEditorOpen) pack;
            sendMessage("SPacketSignEditorOpen: "
                    + "\n - Sign Pos " + s.getSignPosition()
            );
        } else if (pack instanceof SPacketSoundEffect && SSoundEffect.getValue()) {
            SPacketSoundEffect s = (SPacketSoundEffect) pack;
            sendMessage("SPacketSoundEffect: "
                    + "\n - Sound Name: " + s.getSound().getSoundName()
                    + "\n - Sound Category: " + s.getCategory().getName()
                    + "\n - Sound Pos: " + s.getX() + " " + s.getY() + " " + s.getZ()
                    + "\n - Sound Pitch: " + s.getPitch()
                    + "\n - Sound Volume: " + s.getVolume()
            );
        } else if (pack instanceof SPacketSpawnGlobalEntity && SSpawnGlobalEntity.getValue()) {
            SPacketSpawnGlobalEntity s = (SPacketSpawnGlobalEntity) pack;
            sendMessage("SPacketSpawnGlobalEntity: "
                    + "\n - Entity ID: " + s.getEntityId()
                    + "\n - Pos: " + s.getX() + " " + s.getY() + " " + s.getZ()
                    + "\n - Type: " + s.getType()
            );
        } else if (pack instanceof SPacketSpawnMob && SSpawnMob.getValue()) {
            SPacketSpawnMob s = (SPacketSpawnMob) pack;
            sendMessage("SPacketSpawnMob: "
                    + "\n - Entity ID: " + s.getEntityID()
                    + "\n - Pos: " + s.getX() + " " + s.getY() + " " + s.getZ()
                    + "\n - UUID: " + s.getUniqueId()
                    + "\n - Yaw " + s.getYaw()
                    + "\n - Pitch " + s.getPitch()
                    + "\n - Type: " + s.getEntityType()
            );
        } else if (pack instanceof SPacketSpawnPlayer && SSpawnPlayer.getValue()) {
            SPacketSpawnPlayer s = (SPacketSpawnPlayer) pack;
            sendMessage("SPacketSpawnPlayer: "
                    + "\n - Entity ID: " + s.getEntityID()
                    + "\n - Pos: " + s.getX() + " " + s.getY() + " " + s.getZ()
                    + "\n - UUID: " + s.getUniqueId()
                    + "\n - Yaw " + s.getYaw()
                    + "\n - Pitch " + s.getPitch()
            );
        } else if (pack instanceof SPacketSpawnExperienceOrb && SSpawnExperienceOrb.getValue()) {
            SPacketSpawnExperienceOrb s = (SPacketSpawnExperienceOrb) pack;
            sendMessage("SPacketSpawnExperienceOrb: "
                    + "\n - Entity ID: " + s.getEntityID()
                    + "\n - Pos: " + s.getX() + " " + s.getY() + " " + s.getZ()
                    + "\n - XP value: " + s.getXPValue()
            );
        } else if (pack instanceof SPacketSpawnPainting && SSpawnPainting.getValue()) {
            SPacketSpawnPainting s = (SPacketSpawnPainting) pack;
            sendMessage("SPacketSpawnPainting: "
                    + "\n - Entity ID: " + s.getEntityID()
                    + "\n - Title: " + s.getTitle()
                    + "\n - Pos: " + s.getPosition()
                    + "\n - UUID: " + s.getUniqueId()
                    + "\n - Facing: " + s.getFacing().getName()
            );
        } else if (pack instanceof SPacketSpawnObject && SSpawnObject.getValue()) {
            SPacketSpawnObject s = (SPacketSpawnObject) pack;
            sendMessage("SPacketSpawnObject: "
                    + "\n - Entity ID: " + s.getEntityID()
                    + "\n - Pos: " + s.getX() + " " + s.getY() + " " + s.getZ()
                    + "\n - Speed Pos: " + s.getSpeedX() + " " + s.getSpeedY() + " " + s.getSpeedZ()
                    + "\n - UUID: " + s.getUniqueId()
                    + "\n - Data: " + s.getData()
                    + "\n - Type: " + s.getType()
                    + "\n - Pitch: " + s.getPitch()
                    + "\n - Yaw: " + s.getYaw()
            );
        } else if (pack instanceof SPacketSpawnPosition && SSpawnPosition.getValue()) {
            SPacketSpawnPosition s = (SPacketSpawnPosition) pack;
            sendMessage("SPacketSpawnPosition: "
                    + "\n - Pos: " + s.getSpawnPos()
            );
        } else if (pack instanceof SPacketTabComplete && STabComplete.getValue()) {
            SPacketTabComplete s = (SPacketTabComplete) pack;
            sendMessage("SPacketTabComplete" + "\n" + Arrays.toString(s.getMatches())
            );
        } else if (pack instanceof SPacketUnloadChunk && SUnloadChunk.getValue()) {
            SPacketUnloadChunk s = (SPacketUnloadChunk) pack;
            sendMessage("SPacketUnloadChunk"
                    + "\n - Chunk Pos: " + s.getX() + " " + s.getZ()
            );
        } else if (pack instanceof SPacketUseBed && SUseBed.getValue()) {
            SPacketUseBed s = (SPacketUseBed) pack;
            sendMessage("SPacketUseBed"
                    + "\n - Pos: " + s.getBedPosition()
                    + "\n - Player name: " + s.getPlayer(mc.world).getName()
            );
        } else if (pack instanceof SPacketUpdateHealth && SUpdateHealth.getValue()) {
            SPacketUpdateHealth s = (SPacketUpdateHealth) pack;
            sendMessage("SPacketUpdateHealth"
                    + "\n - Health: " + s.getHealth()
                    + "\n - Food: " + s.getFoodLevel()
                    + "\n - Saturation: " + s.getSaturationLevel()
            );
        } else if (pack instanceof SPacketUpdateTileEntity && SUpdateHealth.getValue()) {
            SPacketUpdateTileEntity s = (SPacketUpdateTileEntity) pack;
            sendMessage("SPacketUpdateTileEntity"
                    + "\n - Pos: " + s.getPos()
                    + "\n - Type: " + s.getTileEntityType()
                    + "\n - NBT tag: " + s.getNbtCompound()
            );
        }

    });

    StringBuilder file = new StringBuilder();
    
    void sendMessage(String message) {
        StringBuilder e = new StringBuilder();
        if (showTick.getValue())
            e.append("\nTick: ").append(tick);
        e.append("\n").append(message);
        if (separator.getValue())
            e.append("\n----------");

        if (logFile.getValue())
            file.append(e);

        if (printChat.getValue())
            MessageBus.sendClientRawMessage(e.toString());

    }

}
