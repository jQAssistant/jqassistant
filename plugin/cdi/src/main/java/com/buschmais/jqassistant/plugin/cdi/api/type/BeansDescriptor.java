package com.buschmais.jqassistant.plugin.cdi.api.type;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Beans")
public interface BeansDescriptor extends CdiDescriptor, FileDescriptor {
}
