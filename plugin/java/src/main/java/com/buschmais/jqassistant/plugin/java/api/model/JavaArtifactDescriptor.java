package com.buschmais.jqassistant.plugin.java.api.model;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

public interface JavaArtifactDescriptor extends JavaDescriptor, ArtifactDescriptor {

    @ResultOf
    @Cypher("match (a:Artifact)-[r:CONTAINS|REQUIRES]->(type:Type) where (id(a)={this} or (type(r)='CONTAINS' and id(a) in {dependencies})) and type.fqn={fqn} return type")
    TypeDescriptor resolveType(@Parameter("fqn") String fqn, @Parameter("dependencies") List<? extends ArtifactDescriptor> dependencies);

    @Outgoing
    List<TypeDescriptor> getRequiresTypes();

    @Relation("REQUIRES")
    @Retention(RUNTIME)
    public @interface RequiresType {
    }

}
