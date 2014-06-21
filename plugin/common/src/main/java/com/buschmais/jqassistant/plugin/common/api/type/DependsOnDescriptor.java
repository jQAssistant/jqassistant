package com.buschmais.jqassistant.plugin.common.api.type;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Relation("DEPENDS_ON")
public interface DependsOnDescriptor extends Descriptor {

    @Outgoing
    ArtifactDescriptor getDependent();

    @Incoming
    ArtifactDescriptor getDependency();

    @Property("scope")
    String getScope();

    void setScope(String scope);

    @Property("optional")
    boolean isOptional();

    void setOptional(boolean optional);
}
