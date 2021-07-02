package com.gamesense.api.util.world.combatRewrite.ac.entityData;

import com.gamesense.api.util.world.combatRewrite.ac.ACSettings;
import com.gamesense.api.util.world.combatRewrite.ac.entityData.attributeMap.AttributeMap;
import com.gamesense.client.manager.managers.AutoCrystalManager;
import com.gamesense.mixin.mixins.accessor.IEntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketSpawnMob;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static net.minecraft.entity.EntityLivingBase.SWIM_SPEED;

public class EntityLivingInfo extends EntityInfo {
    // this key is private
    protected static final DataParameter<Float> HEALTH = IEntityLivingBase.getHEALTH();

    private final AttributeMap attributeMap = new AttributeMap();

    protected final AtomicReferenceArray<ItemStack> armour = new AtomicReferenceArray<>(new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY});
    public volatile boolean lowArmour = false;
    protected final AtomicIntegerArray enchantModifiers = new AtomicIntegerArray(new int[] {0, 0 ,0, 0});
    private volatile int enchantModifier = 0;

    protected EntityLivingInfo(int entityID) {
        super(entityID);

        this.setupAttributeMap();
    }

    public EntityLivingInfo(SPacketSpawnMob mob) {
        this(mob.getEntityID());

        this.dataManager.setEntryValues(mob.getDataManagerEntries());

        switch (mob.getEntityType()) {
            case 35:
                this.width = 0.4D;
                this.height = 0.8D;
                break;
            case 65:
                this.width = 0.5D;
                this.height = 0.9D;
                break;
            case 50:
                this.width = 0.6F;
                this.height = 1.7D;
                break;
            case 34:
            case 36:
            case 37:
            case 66:
                this.width = 0.6D;
                this.height = 1.95D;
                break;
            case 58:
                this.width = 0.6D;
                this.height = 2.9D;
                break;
            case 97:
                this.width = 0.7D;
                this.height = 1.9D;
                break;
            case 68:
                this.width = 0.85D;
                this.height = 0.85D;
                break;
            case 64:
                this.width = 0.9D;
                this.height = 3.5D;
                break;
            case 4:
                this.width = 1.9975D;
                this.height = 1.9975D;
                break;
            case 99:
                this.width = 1.4D;
                this.height = 2.7D;
                break;
            // Giant Zombie *I think this is right?*
            case 53:
                this.width = 3.6D;
                this.height = 10.8;
                break;
            case 56:
                this.width = 4.0D;
                this.height = 4.0D;
        }

        this.updatePosition(mob.getX(), mob.getY(), mob.getZ());
    }

    @Override
    protected void setupDataManager() {
        super.setupDataManager();

        this.dataManager.setEntry(HEALTH, 1.0F);
    }

    public void updateArmour(int slot, ItemStack itemStack) {
        if (itemStack == null) {
            this.armour.set(slot, ItemStack.EMPTY);
            this.enchantModifiers.set(slot, 0);
        } else {
            this.armour.set(slot, itemStack);
            this.enchantModifiers.set(slot, getModifier(itemStack));
        }

        ACSettings settings = AutoCrystalManager.INSTANCE.getSettings();
        if (settings != null) {
            float armourPercent = settings.armourFacePlace / 100.0F;
            for (int j = 0; j < this.armour.length(); j++) {
                ItemStack stack = this.armour.get(j);
                if (stack.isEmpty()) {
                    continue;
                }
                if ((1.0f - ((float) stack.getItemDamage() / (float) stack.getMaxDamage())) < armourPercent) {
                    this.lowArmour = true;
                    break;
                }
            }
        }

        int enchantModifier = 0;
        for(int i = 0; i < enchantModifiers.length(); i++) {
            enchantModifier += enchantModifiers.get(i);
        }
        this.enchantModifier = enchantModifier;
    }

    @Override
    public void onRemove() {
        for (IAttributeInstance attribute : this.attributeMap.getAllAttributes()) {
            attribute.removeAllModifiers();
        }
    }

    protected void setupAttributeMap() {
        this.attributeMap.registerAttribute(SharedMonsterAttributes.MAX_HEALTH);
        this.attributeMap.registerAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        this.attributeMap.registerAttribute(SharedMonsterAttributes.ARMOR);
        this.attributeMap.registerAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
        this.attributeMap.registerAttribute(SWIM_SPEED);
    }

    public void updateAttributeMap(List<SPacketEntityProperties.Snapshot> snapshots) {
        for (SPacketEntityProperties.Snapshot snapshot : snapshots) {
            IAttributeInstance iAttributeInstance = attributeMap.getAttributeInstanceByName(snapshot.getName());

            if (iAttributeInstance == null) {
                continue;
            }

            iAttributeInstance.setBaseValue(snapshot.getBaseValue());
            iAttributeInstance.removeAllModifiers();

            for (AttributeModifier attributemodifier : snapshot.getModifiers()) {
                iAttributeInstance.applyModifier(attributemodifier);
            }
        }
    }

    public void updateAttributeMap(Collection<IAttributeInstance> attributes) {
        for (IAttributeInstance attribute : attributes) {
            IAttributeInstance iAttributeInstance = attributeMap.getAttributeInstanceByName(attribute.getAttribute().getName());

            if (iAttributeInstance == null) {
                continue;
            }

            iAttributeInstance.setBaseValue(attribute.getBaseValue());
            iAttributeInstance.removeAllModifiers();

            for (AttributeModifier attributemodifier : attribute.getModifiers()) {
                iAttributeInstance.applyModifier(attributemodifier);
            }
        }
    }

    public float getHealth() {
        return this.dataManager.getEntryData(HEALTH);
    }

    public double getArmour() {
        return this.attributeMap.getAttributeInstance(SharedMonsterAttributes.ARMOR).getAttributeValue();
    }

    public double getArmourToughness() {
        return this.attributeMap.getAttributeInstance(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue();
    }

    public int getEnchantModifier() {
        return enchantModifier;
    }

    // specifically designed for explosions
    public int getModifier(ItemStack itemStack) {
        NBTTagList nbttaglist = itemStack.getEnchantmentTagList();

        int modifier = 0;
        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            int id = nbttaglist.getCompoundTagAt(i).getShort("id");
            int level = nbttaglist.getCompoundTagAt(i).getShort("lvl");

            // 0 is protection, 3 is blast protection
            switch (id) {
                case 3:
                    modifier += level;
                case 0:
                    modifier += level;
            }
        }

        return modifier;
    }
}
