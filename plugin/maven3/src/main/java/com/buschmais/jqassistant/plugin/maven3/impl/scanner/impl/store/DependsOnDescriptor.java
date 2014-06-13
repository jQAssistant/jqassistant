package com.buschmais.jqassistant.plugin.maven3.impl.scanner.impl.store;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Relation("DEPENDS_ON")
public interface DependsOnDescriptor extends Descriptor {

    @Outgoing
    MavenProjectDescriptor getDependent();

    @Incoming
    MavenProjectDescriptor getDependency();

    @Property("type")
    String getType();

    void setType(String type);

    @Property("scope")
    String getScope();

    void setScope(String scope);
}
