package com.gamesense.client.module.modules.misc;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

import java.util.Arrays;

import static com.gamesense.api.util.player.InventoryUtil.swap;

@Module.Declaration(name = "MouseClickAction", category = Category.Misc)
public class MouseClickAction extends Module {

    BooleanSetting friend = registerBoolean("friend", true);
    ModeSetting friendButton = registerMode("FriendButton", Arrays.asList("MOUSE3", "MOUSE4", "MOUSE5"), "MOUSE3",() -> friend.getValue());
    BooleanSetting pearl = registerBoolean("pearl", true);
    ModeSetting pearlButton = registerMode("PearlButton", Arrays.asList("MOUSE3", "MOUSE4", "MOUSE5"), "MOUSE4",() -> pearl.getValue());
    BooleanSetting clipRotate = registerBoolean("clipRotate", false, () -> pearl.getValue());
    IntegerSetting pearlPitch = registerInteger("Pitch", 85, -90, 90, () -> clipRotate.getValue());

    int MCPButtonCode;
    int MCFButtonCode;
    int pearlInvSlot;

    public void onUpdate() {

        if (pearlButton.getValue().equalsIgnoreCase("MOUSE3")) {
            MCPButtonCode = 2; //Mouse3 (used for MCF so using this will retard your gameplay)
        } else if (pearlButton.getValue().equalsIgnoreCase("MOUSE4")) {
            MCPButtonCode = 3; //Mouse4
        } else if (pearlButton.getValue().equalsIgnoreCase("MOUSE5")) {
            MCPButtonCode = 4; //Mouse5
        } else {
            MCPButtonCode = 2; //User Error Protection
        }

        if (friendButton.getValue().equalsIgnoreCase("MOUSE3")) {
            MCFButtonCode = 2; //Mouse3 (used for MCF so using this will retard your gameplay)
        } else if (friendButton.getValue().equalsIgnoreCase("MOUSE4")) {
            MCFButtonCode = 3; //Mouse4
        } else if (friendButton.getValue().equalsIgnoreCase("MOUSE5")) {
            MCFButtonCode = 4; //Mouse5
        } else {
            MCFButtonCode = 2; //User Error Protection
        }

        if (Mouse.isButtonDown(MCPButtonCode)) {
            if (clipRotate.getValue() && mc.player.onGround)
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, pearlPitch.getValue().floatValue(), mc.player.onGround));

            if (clipRotate.getValue() && !mc.player.onGround)
                return;

            pearlInvSlot = InventoryUtil.findFirstItemSlot(ItemEnderPearl.class, 0, 35);
            int pearlHotSlot = InventoryUtil.findFirstItemSlot(ItemEnderPearl.class, 0,8);

            int currentItem = mc.player.inventory.currentItem;

            if (pearlHotSlot == -1) {
                swap(pearlInvSlot, currentItem + 36);
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                swap(pearlInvSlot, currentItem + 36);
            } else {

                int oldSlot = mc.player.inventory.currentItem;
                mc.player.connection.sendPacket(new CPacketHeldItemChange(pearlHotSlot));
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

            }

        }

    }

    @EventHandler
        final Listener<InputEvent.MouseInputEvent> listener = new Listener<>(event -> {
        if (Mouse.isButtonDown(MCFButtonCode)) {
            if (mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.ENTITY) && mc.objectMouseOver.entityHit instanceof EntityPlayer && friend.getValue()) {
                if (SocialManager.isFriendForce(mc.objectMouseOver.entityHit.getName())) {
                    SocialManager.delFriend(mc.objectMouseOver.entityHit.getName());
                    MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "Removed " + mc.objectMouseOver.entityHit.getName() + " from friends list");
                } else {
                    SocialManager.addFriend(mc.objectMouseOver.entityHit.getName());
                    MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getEnabledColor() + "Added " + mc.objectMouseOver.entityHit.getName() + " to friends list");
                }
            }
        }
        });

}

