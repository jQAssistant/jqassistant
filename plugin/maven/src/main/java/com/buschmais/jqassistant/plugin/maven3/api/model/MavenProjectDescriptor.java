package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label(value = "Project", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface MavenProjectDescriptor extends MavenDescriptor, MavenCoordinatesDescriptor, FullQualifiedNameDescriptor, NamedDescriptor {

}
