package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.report.Generic;
import com.buschmais.xo.neo4j.api.annotation.Indexed;

import static com.buschmais.jqassistant.plugin.common.api.report.Generic.GenericLanguageElement.Named;

/**
 * Defines a descriptor having a name.
 */
@Generic(Named)
public interface NamedDescriptor extends Descriptor {

    @Indexed
    String getName();

    void setName(String name);
}
