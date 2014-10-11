package com.buschmais.jqassistant.plugin.common.test.scanner;

import com.buschmais.jqassistant.core.store.api.model.DirectoryDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Custom")
public interface CustomDirectoryDescriptor extends DirectoryDescriptor {
}
