package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.jqassistant.core.store.api.descriptor.AbstractDescriptor;

import java.util.Properties;
import java.util.Set;

/**
 * A descriptor containing properties.
 */
public class PropertiesDescriptor extends AbstractDescriptor {

    private Set<PrimitiveValueDescriptor> properties;

    public Set<PrimitiveValueDescriptor> getProperties() {
        return properties;
    }

    public void setProperties(Set<PrimitiveValueDescriptor> properties) {
        this.properties = properties;
    }
}
