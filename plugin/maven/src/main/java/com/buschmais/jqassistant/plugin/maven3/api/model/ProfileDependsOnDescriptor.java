package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Relation("DECLARES_DEPENDENCY")
public interface ProfileDependsOnDescriptor extends MavenDependencyDescriptor {

    @Outgoing
    MavenProfileDescriptor getDependent();

}
