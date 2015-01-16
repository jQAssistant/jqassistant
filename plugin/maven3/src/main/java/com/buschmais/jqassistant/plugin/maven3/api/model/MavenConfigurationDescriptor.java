package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.ValueDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Descriptor for configuration information.
 * 
 * @author ronald.kunzmann@buschmais.com
 */
@Label("Configuration")
public interface MavenConfigurationDescriptor extends MavenDescriptor {

    /**
     * Get hierarchical configuration entries.
     * 
     * @return entries
     */
    @Relation("CONTAINS")
    List<ValueDescriptor<?>> getValues();
}
