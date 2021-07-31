package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.TransformSideFirstPersonEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.Pair;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlayerPacket;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.client.manager.managers.PlayerPacketManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.misc.AutoGG;
import com.gamesense.client.module.modules.render.ViewModel;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.Vec2f;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.glRotatef;

/**
 * @author 0b00101010
 * @author Doogie13
 * @since 07/02/2021
 */

@Module.Declaration(name = "KillAura", category = Category.Combat)
public class KillAura extends Module {

    public BooleanSetting animation = registerBoolean("Animations", true);
    DoubleSetting animSpeed = registerDouble("Animation Speed", 125,0,250, () -> animation.getValue());
    DoubleSetting animx = registerDouble("Anim X", 0,-500,500,() -> animation.getValue());
    DoubleSetting animy = registerDouble("Anim Y", 0,-500,500,() -> animation.getValue());
    DoubleSetting animz = registerDouble("Anim Z", 0,-500,500,() -> animation.getValue());

    BooleanSetting players = registerBoolean("Players", true);
    BooleanSetting hostileMobs = registerBoolean("Monsters", false);
    BooleanSetting passiveMobs = registerBoolean("Animals", false);
    ModeSetting itemUsed = registerMode("Item used", Arrays.asList("Sword", "Axe", "Both", "All"), "Sword");
    ModeSetting enemyPriority = registerMode("Enemy Priority", Arrays.asList("Closest", "Health"), "Closest");
    BooleanSetting swordPriority = registerBoolean("Prioritise Sword", true);
    BooleanSetting caCheck = registerBoolean("AC Check", false);
    BooleanSetting rotation = registerBoolean("Rotation", true);
    BooleanSetting autoSwitch = registerBoolean("Switch", false);
    DoubleSetting switchHealth = registerDouble("Min Switch Health", 0f, 0f, 20f);
    DoubleSetting range = registerDouble("Range", 5, 0, 10);

    private boolean isAttacking = false;
    private Timer animTimer = new Timer();

    boolean animattack;
    boolean doAnim;

    double xright;
    double yright;
    double zright;

    double xscaleright;
    double yscaleright;
    double zscaleright;


    public void onUpdate() {
        /**/
        if (animation.getValue()) {

            if (animattack) {

                doAnim = true;
                animattack = false;
            }

            if (doAnim) {




            } else {

                animTimer.reset();

            }

        }
        /**/
        if (mc.player == null || !mc.player.isEntityAlive()) return;

        final double rangeSq = range.getValue() * range.getValue();
        Optional<Entity> optionalTarget = mc.world.loadedEntityList.stream()
            .filter(entity -> entity instanceof EntityLivingBase)
            .filter(entity -> !EntityUtil.basicChecksEntity(entity))
            .filter(entity -> mc.player.getDistanceSq(entity) <= rangeSq)
            .filter(this::attackCheck)
            .min(Comparator.comparing(e -> (enemyPriority.getValue().equals("Closest") ? mc.player.getDistanceSq(e) : ((EntityLivingBase) e).getHealth())));

        boolean sword = itemUsed.getValue().equalsIgnoreCase("Sword");
        boolean axe = itemUsed.getValue().equalsIgnoreCase("Axe");
        boolean both = itemUsed.getValue().equalsIgnoreCase("Both");
        boolean all = itemUsed.getValue().equalsIgnoreCase("All");

        if (optionalTarget.isPresent()) {
            Pair<Float, Integer> newSlot = new Pair<>(0.0f, -1);

            if (autoSwitch.getValue() && (mc.player.getHealth() + mc.player.getAbsorptionAmount() >= switchHealth.getValue())) {
                if (sword || both || all) {
                    newSlot = findSwordSlot();
                }
                if ((axe || both || all) && !(swordPriority.getValue() && newSlot.getValue() != -1)) {
                    Pair<Float, Integer> possibleSlot = findAxeSlot();
                    if (possibleSlot.getKey() > newSlot.getKey()) {
                        newSlot = possibleSlot;
                    }
                }
            }

            int temp = mc.player.inventory.currentItem;
            if ((newSlot.getValue() != -1)) {
                mc.player.inventory.currentItem = newSlot.getValue();
            }

            if (shouldAttack(sword, axe, both, all)) {
                Entity target = optionalTarget.get();

                if (rotation.getValue()) {
                    Vec2f rotation = RotationUtil.getRotationTo(target.getEntityBoundingBox());
                    PlayerPacket packet = new PlayerPacket(this, rotation);
                    PlayerPacketManager.INSTANCE.addPacket(packet);
                }

                if (ModuleManager.isModuleEnabled(AutoGG.class)) {
                    AutoGG.INSTANCE.addTargetedPlayer(target.getName());
                }

                attack(target);
            } else {
                mc.player.inventory.currentItem = temp;
            }
        }
    }

    private Pair<Float, Integer> findSwordSlot() {
        List<Integer> items = InventoryUtil.findAllItemSlots(ItemSword.class);
        List<ItemStack> inventory = mc.player.inventory.mainInventory;

        float bestModifier = 0f;
        int correspondingSlot = -1;
        for (Integer integer : items) {
            if (integer > 8) {
                continue;
            }

            ItemStack stack = inventory.get(integer);
            float modifier = (EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED) + 1f) * ((ItemSword) stack.getItem()).getAttackDamage();

            if (modifier > bestModifier) {
                bestModifier = modifier;
                correspondingSlot = integer;
            }
        }

        return new Pair<>(bestModifier, correspondingSlot);
    }

    private Pair<Float, Integer> findAxeSlot() {
        List<Integer> items = InventoryUtil.findAllItemSlots(ItemAxe.class);
        List<ItemStack> inventory = mc.player.inventory.mainInventory;

        float bestModifier = 0f;
        int correspondingSlot = -1;
        for (Integer integer : items) {
            if (integer > 8) {
                continue;
            }

            ItemStack stack = inventory.get(integer);
            float modifier = (EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED) + 1f) * ((ItemAxe) stack.getItem()).attackDamage;

            if (modifier > bestModifier) {
                bestModifier = modifier;
                correspondingSlot = integer;
            }
        }

        return new Pair<>(bestModifier, correspondingSlot);
    }

    private boolean shouldAttack(boolean sword, boolean axe, boolean both, boolean all) {
        Item item = mc.player.getHeldItemMainhand().getItem();
        return (all
            || (sword || both) && item instanceof ItemSword
            || (axe || both) && item instanceof ItemAxe)
            && (!caCheck.getValue() || !ModuleManager.getModule(AutoCrystal.class).isAttacking);
    }

    private void attack(Entity e) {
        if (mc.player.getCooledAttackStrength(0.0f) >= 1.0f) {
            isAttacking = true;
            mc.playerController.attackEntity(mc.player, e);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            isAttacking = false;
            animattack = true;
        }
    }

    private boolean attackCheck(Entity entity) {
        if (players.getValue() && entity instanceof EntityPlayer && !SocialManager.isFriend(entity.getName())) {
            if (((EntityPlayer) entity).getHealth() > 0) {
                return true;
            }
        }

        if (passiveMobs.getValue() && entity instanceof EntityAnimal) {
            return !(entity instanceof EntityTameable);
        }

        return hostileMobs.getValue() && entity instanceof EntityMob;
    }

    @EventHandler
    private final Listener<TransformSideFirstPersonEvent> eventListener = new Listener<>(event -> {

        ViewModel viewmodel = ModuleManager.getModule(ViewModel.class);

            if (event.getEnumHandSide() == EnumHandSide.RIGHT) {  // ViewModel will work hopefully lol
                GlStateManager.scale(xscaleright + viewmodel.xScaleRight.getValue(), yscaleright + viewmodel.yScaleRight.getValue(), zscaleright + viewmodel.zScaleRight.getValue());
                GlStateManager.translate(xright + viewmodel.xRight.getValue(), yright + viewmodel.yRight.getValue(), zright + viewmodel.zRight.getValue());
            }
    });
}