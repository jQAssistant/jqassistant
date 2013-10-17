package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.jqassistant.core.store.api.descriptor.AbstractFullQualifiedNameDescriptor;

import java.util.HashSet;
import java.util.Set;

/**
 * A descriptor representing a property file.
 */
public class PropertyFileDescriptor extends AbstractFullQualifiedNameDescriptor {

    private Set<PropertyDescriptor> properties = new HashSet<>();

    public Set<PropertyDescriptor> getProperties() {
        return properties;
    }

    public void setProperties(Set<PropertyDescriptor> properties) {
        this.properties = properties;
    }
}
