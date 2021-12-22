package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.RenderEvent;
import com.gamesense.api.setting.values.*;
import com.gamesense.api.util.misc.Pair;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlayerPacket;
import com.gamesense.api.util.player.RotationUtil;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.api.util.world.GeometryMasks;
import com.gamesense.client.manager.managers.PlayerPacketManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.misc.AutoGG;
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
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * @author 0b00101010
 * @since 07/02/2021
 */

@Module.Declaration(name = "KillAura", category = Category.Combat)
public class KillAura extends Module {


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
    ModeSetting render = registerMode("Render", Arrays.asList("None", "Rectangle", "Circle"), "None");
    IntegerSetting life = registerInteger("Life", 300, 0, 1000, () -> !render.getValue().equals("None"));
    DoubleSetting circleRange = registerDouble("Circle Range", 1, 0, 3);
    ColorSetting color = registerColor("Color", new GSColor(255, 255, 255, 255), () -> true, true);
    BooleanSetting desyncCircle = registerBoolean("Desync Circle", false);
    IntegerSetting stepRainbowCircle = registerInteger("Step Rainbow Circle", 1, 1, 100);
    BooleanSetting increaseHeight = registerBoolean("Increase Height", true);
    DoubleSetting speedIncrease = registerDouble("Speed Increase", 0.01, 0.3, 0.001);

    ArrayList<renderClass> toRender = new ArrayList<>();

    static class renderClass {
        final int id;
        long start;
        final long life;
        final String mode;
        final double circleRange;
        final GSColor color;
        final boolean desyncCircle;
        final int stepRainbowCircle;
        final double range;
        final int desync;
        final boolean increaseHeight;
        final double speedIncrease;
        double nowHeigth = 0;
        boolean up = true;


        public renderClass(int id, long life, String mode, GSColor color, double circleRange, boolean desyncCircle, int stepRainbowCircle, double range, int desync, boolean increaseHeight, double speedIncrease) {
            this.increaseHeight = increaseHeight;
            this.speedIncrease = speedIncrease;
            this.id = id;
            this.range = range;
            start = System.currentTimeMillis();
            this.life = life;
            this.mode = mode;
            this.desync = desync;
            this.circleRange = circleRange;
            this.color = color;
            this.desyncCircle = desyncCircle;
            this.stepRainbowCircle = stepRainbowCircle;
        }

        boolean update() {
            return System.currentTimeMillis() - start > life;
        }

        boolean reset(int id) {
            if (this.id == id) {
                start = System.currentTimeMillis();
                return true;
            }
            return false;
        }

        void render() {
            Entity e = mc.world.getEntityByID(id);
            if (e != null)
                switch (mode) {
                    case "Rectangle":
                        RenderUtil.drawBox(e.getEntityBoundingBox(), false, e.height, color, GeometryMasks.Quad.ALL);
                        break;
                    case "Circle":
                        double inc = 0;
                        if (increaseHeight) {
                            nowHeigth += speedIncrease * (up ? 1 : -1);
                            if (nowHeigth > e.height)
                                up = false;
                            else if (nowHeigth < 0)
                                up = true;
                            inc = nowHeigth;
                        }
                        if (desyncCircle) {
                            RenderUtil.drawCircle((float) e.posX, (float) (e.posY + inc), (float) e.posZ, range, desync, color.getAlpha());
                        } else {
                            RenderUtil.drawCircle((float) e.posX, (float) (e.posY + inc), (float) e.posZ, range, color);
                        }
                        break;
            }
        }
    }

    boolean calcDelay = true;

    public void onUpdate() {
        if (mc.player == null || !mc.player.isEntityAlive()) return;

        toRender.removeIf(renderClass::update);
        for(int i = 0; i < toRender.size(); i++)
            if (toRender.get(i).update()) {
                toRender.remove(i);
                i--;
            }

        final double rangeSq = Math.pow(range.getValue(), 2);
        Optional<Entity> optionalTarget = mc.world.loadedEntityList.stream()
            .filter(entity -> entity instanceof EntityLivingBase)
            .filter(entity -> !EntityUtil.basicChecksEntity(entity))
            .filter(entity -> mc.player.getDistanceSq(entity) <= rangeSq)
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

                if (!render.getValue().equals("None")) {

                    boolean found = false;
                    for(renderClass rend : toRender)
                        if (rend.reset(target.entityId)) {
                            found = true;
                            break;
                        }

                    if (!found) {
                        toRender.add(new renderClass(target.entityId, life.getValue(), render.getValue(), color.getValue(), circleRange.getValue(), desyncCircle.getValue(), stepRainbowCircle.getValue(), circleRange.getValue(), stepRainbowCircle.getValue(), increaseHeight.getValue(), speedIncrease.getValue()));
                    }
                }

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
            && (!caCheck.getValue() || ModuleManager.getModule(AutoCrystalRewrite.class).bestBreak.crystal != null);
    }

    private void attack(Entity e) {
        if (mc.player.getCooledAttackStrength(0.0f) >= 1.0f) {
            mc.playerController.attackEntity(mc.player, e);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            calcDelay = true;
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

    @Override
    public void onWorldRender(RenderEvent event) {
        toRender.forEach(renderClass::render);
    }
}