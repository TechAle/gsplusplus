package com.gamesense.api.util.world.combatRewrite.ac.entityData.attributeMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractAttributeMap {
    protected final ConcurrentHashMap<IAttribute, IAttributeInstance> attributes = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, IAttributeInstance> attributesByName = new ConcurrentHashMap<>();
    protected final Multimap<IAttribute, IAttribute> descendantsByParent = Multimaps.synchronizedMultimap(HashMultimap.create());
    public IAttributeInstance getAttributeInstance(IAttribute attribute) {
        return this.attributes.get(attribute);
    }

    @Nullable
    public IAttributeInstance getAttributeInstanceByName(String attributeName) {
        return this.attributesByName.get(attributeName);
    }

    /**
     * Registers an attribute with this AttributeMap, returns a modifiable AttributeInstance associated with this map
     */
    public IAttributeInstance registerAttribute(IAttribute attribute) {
        if (this.attributesByName.containsKey(attribute.getName())) {
            throw new IllegalArgumentException("Attribute is already registered!");
        } else {
            IAttributeInstance iattributeinstance = this.createInstance(attribute);
            this.attributesByName.put(attribute.getName(), iattributeinstance);
            this.attributes.put(attribute, iattributeinstance);

            for (IAttribute iattribute = attribute.getParent(); iattribute != null; iattribute = iattribute.getParent()) {
                this.descendantsByParent.put(iattribute, attribute);
            }

            return iattributeinstance;
        }
    }

    protected abstract IAttributeInstance createInstance(IAttribute attribute);

    public Collection<IAttributeInstance> getAllAttributes()
    {
        return this.attributesByName.values();
    }

    public void onAttributeModified(IAttributeInstance instance)
    {
    }

    public void removeAttributeModifiers(Multimap<String, AttributeModifier> modifiers) {
        for (Map.Entry<String, AttributeModifier> entry : modifiers.entries()) {
            IAttributeInstance iattributeinstance = this.getAttributeInstanceByName(entry.getKey());

            if (iattributeinstance != null) {
                iattributeinstance.removeModifier(entry.getValue());
            }
        }
    }

    public void applyAttributeModifiers(Multimap<String, AttributeModifier> modifiers) {
        for (Map.Entry<String, AttributeModifier> entry : modifiers.entries()) {
            IAttributeInstance iattributeinstance = this.getAttributeInstanceByName(entry.getKey());

            if (iattributeinstance != null) {
                iattributeinstance.removeModifier(entry.getValue());
                iattributeinstance.applyModifier(entry.getValue());
            }
        }
    }
}
