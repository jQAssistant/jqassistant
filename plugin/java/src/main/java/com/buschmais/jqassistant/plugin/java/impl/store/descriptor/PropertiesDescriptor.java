package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.jqassistant.core.store.api.descriptor.AbstractDescriptor;

import java.util.Properties;

/**
 * A descriptor containing properties.
 */
public class PropertiesDescriptor extends AbstractDescriptor {

    private Properties properties;

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
