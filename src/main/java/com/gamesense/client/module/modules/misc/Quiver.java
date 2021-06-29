package com.gamesense.client.module.modules.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.resources.I18n;
import com.gamesense.api.event.events.OnUpdateWalkingPlayerEvent;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlayerPacket;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.client.manager.managers.PlayerPacketManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;

import java.util.*;

import static com.gamesense.api.util.player.SpoofRotationUtil.ROTATION_UTIL;

/*
    @Author TechAle
 */

@SuppressWarnings("unused")
@Module.Declaration(name = "Quiver", category = Category.Misc, priority = 250)
public class Quiver extends Module {


    ArrayList<String> arrowType = new ArrayList<String>() {
        {
            add("none");
            add("strength");
            add("swiftness");
        }
    };

    ArrayList<String> disableWhen = new ArrayList<String>() {
        {
            add("none");
            add("moving");
            add("stand");
        }
    };

    ModeSetting firstArrow = registerMode("First Arrow", Arrays.asList(arrowType.toArray(new String[0])), "strength");
    ModeSetting disableFirst = registerMode("Disable First", Arrays.asList(disableWhen.toArray(new String[0])), "none");
    ModeSetting secondArrow = registerMode("Second Arrow", Arrays.asList(arrowType.toArray(new String[0])), "none");
    ModeSetting disableSecond = registerMode("Disable Second", Arrays.asList(disableWhen.toArray(new String[0])), "none", () -> !secondArrow.getValue().equals("none"));
    ModeSetting active = registerMode("Active", Arrays.asList("On Bow", "Switch"), "On Bow");
    IntegerSetting pitchMoving = registerInteger("Pitch Moving", -45, 0, -70);
    IntegerSetting standDrawLength = registerInteger("Stand Draw Length", 4, 0, 21);
    IntegerSetting movingDrawLength = registerInteger("Moving Draw Length", 3, 0, 21);
    IntegerSetting tickWait = registerInteger("Tick Retry Wait", 20, 1, 50);
    IntegerSetting tickWaitEnd = registerInteger("Tick Arrow Wait", 0, 0, 100);

    int[] slot;
    int oldslot;
    int firstWait, secondWait, slotCheck, endWait;

    boolean blockedUp,
            beforeActive,
            hasBow,
            isPowering,
            isFirst;
    String arrow = "";

    boolean isMoving() {
        return Math.abs(mc.player.motionX) + Math.abs(mc.player.motionZ) > 0.01;
    }

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {
        if ( mc.player == null || mc.world == null || !isPowering) return;
        PlayerPacket packet;
        // If he is not moving
        if (!isMoving())
            // Aim up
            packet = new PlayerPacket(this, new Vec2f(0, -90));
        // Else, aim down
        else packet = new PlayerPacket(this, new Vec2f(mc.player.rotationYaw, pitchMoving.getValue()));
        PlayerPacketManager.INSTANCE.addPacket(packet);
    });

    public void onEnable() {
        // Disable in case it's enabled at beginning
        if (mc.world == null || mc.player == null) {
            disable();
            return;
        }
        ROTATION_UTIL.onEnable();
        // Reset
        resetValues();

    }

    void resetValues() {
        blockedUp = beforeActive = isPowering =false;
        hasBow = isFirst = true;
        firstWait = secondWait = 0;
        endWait = 0;
        oldslot = slotCheck = -1;
        arrow = "";
    }

    boolean playerCheck() {
        // Check if above we have a block (so it's impossible to get with the arrow)
        blockedUp = !(BlockUtil.getBlock(EntityUtil.getPosition(mc.player).add(0, 2, 0)) instanceof BlockAir);
        if (blockedUp) {
            disable();
            return false;
        }
        return true;
    }

    public void onDisable() {
        if (mc.world == null || mc.player == null) {
            return;
        }

        // Disable bow
        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBow && mc.player.isHandActive()) {
            mc.player.stopActiveHand();
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        }

        // Output message
        String output = "";

        if (blockedUp)
            output = "There is a block above you";
        else if (!hasBow)
            output = "No bow detected";


        // Output in chat
        setDisabledMessage(output + "Quiver turned OFF!");

        if (oldslot != -1)
            mc.player.inventory.currentItem = oldslot;

    }

    boolean canArrow(boolean isMoving, String notWanted) {
        switch (notWanted) {
            case "none":
                return true;
            case "moving":
                return !isMoving;
            case "stand":
                return isMoving;
            default:
                return false;
        }
    }

    public void onUpdate() {
        if (mc.world == null || mc.player == null) {
            return;
        }

        if (endWait > 0) {
            endWait--;
            return;
        }

        // If we havent a bow
        if (mc.player.inventory.getCurrentItem().getItem() != Items.BOW) {
            if (isPowering) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            }
            // If switch
            if (active.getValue().equals("Switch")) {
                // Check for a bow
                int slot = InventoryUtil.findFirstItemSlot(Items.BOW.getClass(), 0, 8);
                // If not found
                if (slot == -1) {
                    // Disable
                    hasBow = false;
                    disable();
                    return;
                // Else, switch
                } else {
                    oldslot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = slot;
                }
            // If we have onBow
            } else if (active.getValue().equals("On Bow")) {
                // Just set isPowering to false
                isPowering = false;
                return;
            }
        }

        // If it's the first time
        if (!beforeActive) {
            // Reset
            resetValues();
            // If something about the player is not right
            if (!playerCheck()) {
                // Return
                return;
            }
        }
        boolean isMoving = isMoving();
        // If we are not powering
        if (!isPowering) {
            // If firstWait is < 0 (we have to wait for not bow spamming)
            if (--firstWait < 0) {
                boolean enter = canArrow(isMoving, disableFirst.getValue());
                if (enter) {
                    // Get the slot of the arrow
                    slot = getSlotArrow(firstArrow.getValue());
                    isFirst = true;
                } else slot = new int[] {-1, -1};
            }
            // If before we found nothing
            if (slot[1] == -1) {
                // Wait 2 time
                if (--secondWait < 0) {
                    boolean enter = canArrow(isMoving, disableFirst.getValue());
                    if (enter) {
                        // Get slot
                        slot = getSlotArrow(secondArrow.getValue());
                        // Set new wait
                        secondWait = tickWait.getValue();
                    } else slot = new int[] {-1, -1};
                }
                // Lets say that this is the second
                isFirst = false;
            // Set new wait for first
            } else firstWait = tickWait.getValue();

            // If we found nothing, return
            if (slot[1] == -1) {
                return;
            }

            // If we have to switch (if slot[0] == -1 then the arrow will be the first)
            switchArrow();
        }

        // Set rightClick to true
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
        // Active everything
        isPowering = beforeActive = true;

        // If we have to draw
        if (mc.player.getItemInUseMaxCount() >= (isMoving() ? movingDrawLength.getValue() : standDrawLength.getValue())) {
            // release
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
            mc.player.stopActiveHand();
            // If we have to switch back
            switchArrow();
            // Reset some values
            slot = new int[] {-1, -1};
            isPowering = false;
            arrow = "";
            // Add new wait
            endWait = tickWaitEnd.getValue();
            if (isFirst) {
                isFirst = false;
                firstWait = tickWait.getValue();
            }
            else {
                // Add new wait
                secondWait = tickWait.getValue();
                // If switch
                if (active.getValue().equals("Switch")) {
                    // Then, we have to disable
                    disable();
                    //noinspection UnnecessaryReturnStatement
                    return;
                }
            }
        }

    }

    private void switchArrow() {
        if (slot[0] != -1) {
            // Switch items
            mc.playerController.windowClick(0, slot[0], 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, slot[1], 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, slot[0], 0, ClickType.PICKUP, mc.player);
            // Check that the last item was placed correctly
            slotCheck = slot[0];
            mc.playerController.updateController();
        }
    }

    int[] getSlotArrow(String wanted) {
        // Set the return value
        int[] returnValeus = new int[] {-1, -1};

        // If we have the effect, return
        if (haveEffect(wanted))
            return returnValeus;

        // Else, lets check for the arrow
        Item temp;
        for(int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            temp = mc.player.inventory.getStackInSlot(i).getItem();

            // Check if the item is a normal arrow or if we dont need it
            //noinspection ConstantConditions
            if (returnValeus[0] == -1 && ((temp) == Items.ARROW || temp == Items.SPECTRAL_ARROW
                    || ( temp == Items.TIPPED_ARROW && !mc.player.inventory.getStackInSlot(i).getTagCompound().getTag("Potion").toString().contains(wanted)))) {
                returnValeus[0] = i + (i < 9 ? 36 : 0);
            }

            // Check if the arrow is what we need
            //noinspection ConstantConditions
            if (temp == Items.TIPPED_ARROW
                    && mc.player.inventory.getStackInSlot(i).getTagCompound().getTag("Potion").toString().contains(wanted)) {
                returnValeus[1] = i + (i < 9 ? 36 : 0);
                // We can return now
                return returnValeus;
            }

        }

        return returnValeus;
    }

    // Check if we already have that effect
    boolean haveEffect(String wanted) {
        arrow = wanted;
        // Iterate for every effects
        for(int i = 0; i < mc.player.getActivePotionEffects().toArray().length; i++) {
            // Get it
            PotionEffect effect = (PotionEffect) mc.player.getActivePotionEffects().toArray()[i];
            // Get the name
            String name = I18n.format(effect.getPotion().getName());
            // If it's the same as what we want
            if (name.toLowerCase().contains(wanted.toLowerCase()))
                return true;
            // Swiftness and speed dont like eachothers lmao, why mojang do you change names
            if (wanted.equals("swiftness") && name.equals("Speed"))
                return true;
        }
        return false;
    }

    public String getHudInfo() {
        if (!arrow.equals(""))
            return "[" + ChatFormatting.WHITE + arrow + ChatFormatting.GRAY + "]";
        return "";
    }

}