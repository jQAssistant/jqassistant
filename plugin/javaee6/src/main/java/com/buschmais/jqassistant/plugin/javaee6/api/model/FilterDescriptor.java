package com.buschmais.jqassistant.plugin.javaee6.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Filter")
public interface FilterDescriptor extends WebDescriptor, NamedDescriptor, AsyncSupportedDescriptor, TypedDescriptor {

    @Relation("HAS_DESCRIPTION")
    List<DescriptionDescriptor> getDescriptions();

    @Relation("HAS_DISPLAY_NAME")
    List<DisplayNameDescriptor> getDisplayNames();

    @Relation("HAS_ICON")
    List<IconDescriptor> getIcons();

    @Relation("HAS_INIT_PARAM")
    List<ParamValueDescriptor> getInitParams();

    @Relation("HAS_MAPPING")
    List<FilterMappingDescriptor> getMappings();
}
