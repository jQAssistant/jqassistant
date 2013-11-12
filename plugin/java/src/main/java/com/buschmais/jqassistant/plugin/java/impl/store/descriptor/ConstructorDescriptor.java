package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;

@Label(value = "CONSTRUCTOR", usingIndexOf = FullQualifiedNameDescriptor.class)
public interface ConstructorDescriptor extends MethodDescriptor {
}
