package com.gamesense.client.module.modules.misc;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.api.util.misc.Timer;
import com.sun.org.apache.xpath.internal.operations.Bool;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;


import java.util.Arrays;

@Module.Declaration(name = "MouseClickAction", category = Category.Misc)
public class MouseClickAction extends Module {
    BooleanSetting friend = registerBoolean("friend", true);
    ModeSetting friendButton = registerMode("FriendButton", Arrays.asList("MOUSE3", "MOUSE4", "MOUSE5"), "MOUSE3",() -> friend.getValue());
    BooleanSetting pearl = registerBoolean("pearl", true);
    ModeSetting pearlButton = registerMode("PearlButton", Arrays.asList("MOUSE3", "MOUSE4", "MOUSE5"), "MOUSE4",() -> pearl.getValue());
    BooleanSetting clipRotate = registerBoolean("clipRotate", false);
    IntegerSetting pearlPitch = registerInteger("Pitch", 85, -90, 90, () -> clipRotate.getValue());
    BooleanSetting onGroundCheck = registerBoolean("onGround", true, () -> clipRotate.getValue());
    BooleanSetting silentSwitch = registerBoolean("silentSwitch", false, () -> clipRotate.getValue());
    IntegerSetting delaySwitch = registerInteger("silentReturnDelay", 1, 0, 10, () -> silentSwitch.getValue());

    int MCPButtonCode;
    int MCFButtonCode;

    public void onUpdate() {

        final Timer MCPdelayTimer = new Timer();

        float pearlPitchFloat = pearlPitch.getValue().floatValue();

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

        if ((Mouse.isButtonDown(MCPButtonCode) && onGroundCheck.getValue() && mc.player.onGround && pearl.getValue()) || Mouse.isButtonDown(MCPButtonCode) && !onGroundCheck.getValue() && pearl.getValue()) { //We check for button press and don't check for miss :rage:
            int oldSlot = mc.player.inventory.currentItem;

            int pearlSlot = InventoryUtil.findFirstItemSlot(ItemEnderPearl.class, 0, 8);

            if (pearlSlot != -1) {
                if (!silentSwitch.getValue()) {
                    mc.player.inventory.currentItem = pearlSlot;

                } else {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(pearlSlot));
                }

                if (clipRotate.getValue()) {

                    //ROUNDING
                    float yawRounded;

                    float yaw = Math.abs(Math.round(mc.player.rotationYaw) % 360);
                    float division = (int) Math.floor(yaw / 45);
                    float remainder = (int) (yaw % 45);
                    if (remainder < 45 / 2) {
                        yawRounded = 45 * division;
                    } else {
                        yawRounded = 45 * (division + 1);
                    }
                    //END OF ROUNDING

                    //new rotate

                    //mc.player.setPositionAndRotationDirect(mc.player.posX,mc.player.posY,mc.player.posZ,yawRounded,pearlPitchFloat,1,true);

                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yawRounded, pearlPitchFloat, true)); // rotate for phasing
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND); // Throw the pearl (thanks TechAle :D)

                    // mc.player.connection.sendPacket(new CPacketPlayer.Rotation(oldYaw, oldPitch, true)); // rotate back (disabled)
                    mc.player.inventory.currentItem = oldSlot; // return to old slot
                } else { // same as Hoosiers' code previous code
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);
                    if (!silentSwitch.getValue()) {
                        mc.player.inventory.currentItem = oldSlot;
                    } else { //Undo desync?
                        if (MCPdelayTimer.getTimePassed() / 50L >= delaySwitch.getValue()) {
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
                            MCPdelayTimer.reset();
                        }
                    }
                }
            }
        }
    }
    @EventHandler
        final Listener<InputEvent.MouseInputEvent> listener = new Listener<>(event -> {
            if (mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.ENTITY) && mc.objectMouseOver.entityHit instanceof EntityPlayer && Mouse.isButtonDown(MCFButtonCode) && friend.getValue()) {
                if (SocialManager.isFriend(mc.objectMouseOver.entityHit.getName())) {
                    SocialManager.delFriend(mc.objectMouseOver.entityHit.getName());
                    MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "Removed " + mc.objectMouseOver.entityHit.getName() + " from friends list");
                } else {
                    SocialManager.addFriend(mc.objectMouseOver.entityHit.getName());
                    MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getEnabledColor() + "Added " + mc.objectMouseOver.entityHit.getName() + " to friends list");
                }
            }
        });

}

