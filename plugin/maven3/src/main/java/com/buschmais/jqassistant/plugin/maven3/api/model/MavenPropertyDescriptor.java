package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Descriptor for properties defined in POMs.
 * 
 * @author ronald.kunzmann@buschmais.com
 */
@Label("Property")
public interface MavenPropertyDescriptor extends MavenDescriptor {

    /**
     * Get the property name.
     * 
     * @return The name.
     */
    @Property("name")
    String getName();

    /**
     * Set the property name.
     * 
     * @param name
     *            The name.
     */
    void setName(String name);

    /**
     * Get the property value.
     * 
     * @return The value.
     */
    @Property("value")
    String getValue();

    /**
     * Set the property value.
     * 
     * @param value
     *            The value.
     */
    void setValue(String value);
}
