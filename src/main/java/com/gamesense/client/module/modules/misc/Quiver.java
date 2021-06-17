package com.gamesense.client.module.modules.misc;

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

@Module.Declaration(name = "Quiver", category = Category.Misc, priority = 250)
public class Quiver extends Module {


    ArrayList<String> arrowType = new ArrayList<String>() {
        {
            add("none");
            add("strength");
            add("swiftness");
            add("leaping");
        }
    };

    ModeSetting firstArrow = registerMode("First Arrow", Arrays.asList(arrowType.toArray(new String[0])), "strength");
    ModeSetting secondArrow = registerMode("Second Arrow", Arrays.asList(arrowType.toArray(new String[0])), "none");
    ModeSetting active = registerMode("Active", Arrays.asList("On Bow", "Switch"), "On Bow");
    IntegerSetting drawLength = registerInteger("Draw Length", 4, 0, 21);
    IntegerSetting tickWait = registerInteger("Tick Finish Wait", 10, 1, 50);

    int[] slot;
    int oldslot;
    int firstWait, secondWait;

    boolean blockedUp,
            beforeActive,
            hasBow,
            isPowering,
            isFirst;

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<OnUpdateWalkingPlayerEvent> onUpdateWalkingPlayerEventListener = new Listener<>(event -> {
        if ( mc.player == null || mc.world == null || !isPowering) return;

        PlayerPacket packet = new PlayerPacket(this, new Vec2f(0, -90));
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
        blockedUp = beforeActive = isPowering = false;
        hasBow = isFirst = true;
        firstWait = secondWait = 0;
    }

    boolean playerCheck() {
        // Check if above we have a block
        blockedUp = !(BlockUtil.getBlock(EntityUtil.getPosition(mc.player).add(0, 2, 0)) instanceof BlockAir);
        if (blockedUp) {
            disable();
            return false;
        }
        // Get every materials he want
        // mc.player.inventory.getStackInSlot(0).getItem() == Items.TIPPED_ARROW
        // mc.player.inventory.getStackInSlot(0).getTagCompound().getTag("Potion")
        // mc.player.inventory.getStackInSlot(0).getTagCompound().getTag("Potion").getString().contains("strength")
        return true;
    }

    public void onDisable() {
        if (mc.world == null || mc.player == null) {
            return;
        }

        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBow && mc.player.isHandActive())
            mc.player.stopActiveHand();

    }

    public void onUpdate() {
        if (mc.world == null || mc.player == null) {
            return;
        }

        if (mc.player.inventory.getCurrentItem().getItem() != Items.BOW) {
            if (active.getValue().equals("Switch")) {
                int slot = InventoryUtil.findFirstItemSlot(Items.BOW.getClass(), 0, 8);
                if (slot == -1) {
                    hasBow = false;
                    disable();
                    return;
                } else {
                    oldslot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = slot;
                }
            } else if (active.getValue().equals("On Bow")) {
                isPowering = false;
                return;
            }
        }

        if (!beforeActive) {
            resetValues();
            if (!playerCheck()) {
                disable();
                return;
            }
        }

        if (!isPowering) {
            if (--firstWait < 0) {
                slot = getSlotArrow(firstArrow.getValue());
                isFirst = true;
            }
            if (slot[1] == -1) {
                if (--secondWait < 0) {
                    slot = getSlotArrow(secondArrow.getValue());
                    secondWait = tickWait.getValue();
                }
                isFirst = false;
            } else firstWait = tickWait.getValue();
            if (slot[1] == -1) {
                return;
            }
            if (slot[0] != -1) {
                mc.playerController.windowClick(0, slot[0], 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, slot[1], 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, slot[0], 0, ClickType.PICKUP, mc.player);
            }
        }


        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
        isPowering = beforeActive = true;

        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBow && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= drawLength.getValue()) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
            mc.player.stopActiveHand();
            if (slot[0] != -1) {
                mc.playerController.windowClick(0, slot[0], 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, slot[1], 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, slot[0], 0, ClickType.PICKUP, mc.player);
            }
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            mc.player.stopActiveHand();
            slot = new int[] {-1, -1};
            isPowering = false;
            if (isFirst) {
                isFirst = false;
                firstWait = tickWait.getValue();
            }
            else {
                secondWait = tickWait.getValue();
                if (active.getValue().equals("Switch")) {
                    beforeActive = false;
                    mc.player.inventory.currentItem = oldslot;
                    disable();
                }
            }
        }

    }

    int[] getSlotArrow(String wanted) {
        int[] returnValeus = new int[] {-1, -1};

        if (haveEffect(wanted))
            return returnValeus;

        Item temp;
        for(int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
            temp = mc.player.inventory.getStackInSlot(i).getItem();

            //noinspection ConstantConditions
            if (returnValeus[0] == -1 && ((temp) == Items.ARROW || temp == Items.SPECTRAL_ARROW
                    || ( temp == Items.TIPPED_ARROW && !mc.player.inventory.getStackInSlot(i).getTagCompound().getTag("Potion").toString().contains(wanted)))) {
                returnValeus[0] = i + (i < 9 ? 36 : 0);
            }
            if (temp == Items.TIPPED_ARROW
                    && mc.player.inventory.getStackInSlot(i).getTagCompound().getTag("Potion").toString().contains(wanted)) {
                returnValeus[1] = i + (i < 9 ? 36 : 0);
                return returnValeus;
            }

        }

        return returnValeus;
    }

    boolean haveEffect(String wanted) {
        for(int i = 0; i < mc.player.getActivePotionEffects().toArray().length; i++) {
            PotionEffect effect = (PotionEffect) mc.player.getActivePotionEffects().toArray()[i];
            String name = I18n.format(effect.getPotion().getName());
            if (name.toLowerCase().contains(wanted.toLowerCase()))
                return true;
            if (wanted.equals("swiftness") && name.equals("Speed"))
                return true;
        }
        return false;
    }

}