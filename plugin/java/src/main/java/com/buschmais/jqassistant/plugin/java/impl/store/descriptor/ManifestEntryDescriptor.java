package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;

@Label("MANIFESTENTRY")
public interface ManifestEntryDescriptor extends ValueDescriptor<String> {

    /**
     * Set the value.
     *
     * @param value The value.
     */
    void setValue(String value);

    /**
     * Return the value.
     *
     * @return The value.
     */
    String getValue();
}
