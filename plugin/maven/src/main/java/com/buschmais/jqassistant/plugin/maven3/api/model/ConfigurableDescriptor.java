package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Abstract
public interface ConfigurableDescriptor {

    @Relation("HAS_CONFIGURATION")
    MavenConfigurationDescriptor getConfiguration();

    void setConfiguration(MavenConfigurationDescriptor configuration);
}
