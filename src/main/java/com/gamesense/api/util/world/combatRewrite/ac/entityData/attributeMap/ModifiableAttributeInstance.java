package com.gamesense.api.util.world.combatRewrite.ac.entityData.attributeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ModifiableAttributeInstance implements IAttributeInstance
{
    /** The BaseAttributeMap this attributeInstance can be found in */
    private final AttributeMap attributeMap;
    /** The Attribute this is an instance of */
    private final IAttribute genericAttribute;
    private final ConcurrentHashMap<Integer, Set<AttributeModifier>> mapByOperation = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<AttributeModifier>> mapByName = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, AttributeModifier> mapByUUID = new ConcurrentHashMap<>();
    private volatile double baseValue;
    private volatile boolean needsUpdate = true;
    private volatile double cachedValue;

    public ModifiableAttributeInstance(AttributeMap attributeMapIn, IAttribute genericAttributeIn) {
        this.attributeMap = attributeMapIn;
        this.genericAttribute = genericAttributeIn;
        this.baseValue = genericAttributeIn.getDefaultValue();

        for (int i = 0; i < 3; ++i) {
            this.mapByOperation.put(i, Sets.newConcurrentHashSet());
        }
    }

    /**
     * Get the Attribute this is an instance of
     */
    public IAttribute getAttribute() {
        return this.genericAttribute;
    }

    public double getBaseValue() {
        return this.baseValue;
    }

    public void setBaseValue(double baseValue) {
        if (baseValue != this.getBaseValue())
        {
            this.baseValue = baseValue;
            this.flagForUpdate();
        }
    }

    public Collection<AttributeModifier> getModifiersByOperation(int operation) {
        return this.mapByOperation.get(operation);
    }

    public Collection<AttributeModifier> getModifiers() {
        Set<AttributeModifier> set = Sets.newHashSet();

        for (int i = 0; i < 3; ++i) {
            set.addAll(this.getModifiersByOperation(i));
        }

        return set;
    }

    /**
     * Returns attribute modifier, if any, by the given UUID
     */
    @Nullable
    public AttributeModifier getModifier(UUID uuid) {
        return this.mapByUUID.get(uuid);
    }

    public boolean hasModifier(AttributeModifier modifier) {
        return this.mapByUUID.get(modifier.getID()) != null;
    }

    public void applyModifier(AttributeModifier modifier) {
        if (this.getModifier(modifier.getID()) != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        } else {
            Set<AttributeModifier> set = this.mapByName.computeIfAbsent(modifier.getName(), k -> Sets.<AttributeModifier>newHashSet());

            (this.mapByOperation.get(modifier.getOperation())).add(modifier);
            set.add(modifier);
            this.mapByUUID.put(modifier.getID(), modifier);
            this.flagForUpdate();
        }
    }

    public void flagForUpdate() {
        this.needsUpdate = true;
        this.attributeMap.onAttributeModified(this);
    }

    public void removeModifier(AttributeModifier modifier) {
        for (int i = 0; i < 3; ++i) {
            Set<AttributeModifier> set = this.mapByOperation.get(i);
            set.remove(modifier);
        }

        Set<AttributeModifier> set = this.mapByName.get(modifier.getName());

        if (set != null) {
            set.remove(modifier);

            if (set.isEmpty()) {
                this.mapByName.remove(modifier.getName());
            }
        }

        this.mapByUUID.remove(modifier.getID());
        this.flagForUpdate();
    }

    public void removeModifier(UUID uuid) {
        AttributeModifier attributemodifier = this.getModifier(uuid);

        if (attributemodifier != null) {
            this.removeModifier(attributemodifier);
        }
    }

    @SideOnly(Side.CLIENT)
    public void removeAllModifiers() {
        Collection<AttributeModifier> collection = this.getModifiers();

        if (collection != null) {
            for (AttributeModifier attributemodifier : Lists.newArrayList(collection)) {
                this.removeModifier(attributemodifier);
            }
        }
    }

    public double getAttributeValue() {
        if (this.needsUpdate) {
            this.cachedValue = this.computeValue();
            this.needsUpdate = false;
        }

        return this.cachedValue;
    }

    private double computeValue() {
        double initial = this.getBaseValue();

        for (AttributeModifier attributemodifier : this.getAppliedModifiers(0)) {
            initial += attributemodifier.getAmount();
        }

        double total = initial;

        for (AttributeModifier attributemodifier : this.getAppliedModifiers(1)) {
            total += initial * attributemodifier.getAmount();
        }

        for (AttributeModifier attributemodifier : this.getAppliedModifiers(2)) {
            total *= 1.0D + attributemodifier.getAmount();
        }

        return this.genericAttribute.clampValue(total);
    }

    private Collection<AttributeModifier> getAppliedModifiers(int operation) {
        Set<AttributeModifier> set = Sets.newHashSet(this.getModifiersByOperation(operation));

        for (IAttribute iattribute = this.genericAttribute.getParent(); iattribute != null; iattribute = iattribute.getParent()) {
            IAttributeInstance iattributeinstance = this.attributeMap.getAttributeInstance(iattribute);

            set.addAll(iattributeinstance.getModifiersByOperation(operation));
        }

        return set;
    }
}
