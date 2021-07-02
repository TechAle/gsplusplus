package com.gamesense.api.util.world.combatRewrite.ac.entityData.attributeMap;

import com.google.common.collect.Sets;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeMap extends AbstractAttributeMap {
    protected final ConcurrentHashMap<String, IAttributeInstance> instancesByName = new ConcurrentHashMap<>();

    public ModifiableAttributeInstance getAttributeInstance(IAttribute attribute) {
        return (ModifiableAttributeInstance)super.getAttributeInstance(attribute);
    }

    public ModifiableAttributeInstance getAttributeInstanceByName(String attributeName) {
        IAttributeInstance iattributeinstance = super.getAttributeInstanceByName(attributeName);

        if (iattributeinstance == null) {
            iattributeinstance = this.instancesByName.get(attributeName);
        }

        return (ModifiableAttributeInstance)iattributeinstance;
    }

    /**
     * Registers an attribute with this AttributeMap, returns a modifiable AttributeInstance associated with this map
     */
    public IAttributeInstance registerAttribute(IAttribute attribute) {
        IAttributeInstance iattributeinstance = super.registerAttribute(attribute);

        if (attribute instanceof RangedAttribute && ((RangedAttribute) attribute).getDescription() != null) {
            this.instancesByName.put(((RangedAttribute) attribute).getDescription(), iattributeinstance);
        }

        return iattributeinstance;
    }

    protected IAttributeInstance createInstance(IAttribute attribute) {
        return new ModifiableAttributeInstance(this, attribute);
    }

    public void onAttributeModified(IAttributeInstance instance) {
        for (IAttribute iattribute : this.descendantsByParent.get(instance.getAttribute())) {
            ModifiableAttributeInstance modifiableattributeinstance = this.getAttributeInstance(iattribute);

            if (modifiableattributeinstance != null) {
                modifiableattributeinstance.flagForUpdate();
            }
        }
    }

    public Collection<IAttributeInstance> getWatchedAttributes() {
        Set<IAttributeInstance> set = Sets.newHashSet();

        for (IAttributeInstance iattributeinstance : this.getAllAttributes()) {
            if (iattributeinstance.getAttribute().getShouldWatch()) {
                set.add(iattributeinstance);
            }
        }

        return set;
    }
}
