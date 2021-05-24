package com.buschmais.jqassistant.plugin.java.api.model.generics;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaByteCodeDescriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Abstract
@Label("GenericType")
public interface GenericTypeDescriptor extends JavaByteCodeDescriptor, Descriptor {
}
