package com.buschmais.jqassistant.plugin.java.api.model.generics;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.IndexTemplate;
import com.buschmais.jqassistant.plugin.java.api.model.JavaByteCodeDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("TypeParameter")
public interface TypeParameterDescriptor extends JavaByteCodeDescriptor, NamedDescriptor, IndexTemplate {

    TypeParameterHasBoundaryDescriptor getBoundary();

}
