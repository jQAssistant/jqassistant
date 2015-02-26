package com.buschmais.jqassistant.plugin.java.api.model;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Defines a Java artifact.
 */
public interface JavaArtifactFileDescriptor extends JavaDescriptor, ArtifactFileDescriptor {

    @ResultOf
    @Cypher("match (type:Type)<-[:CONTAINS]-(a:Artifact) where type.fqn={fqn} and id(a) in {dependencies} return type")
    Query.Result<TypeDescriptor> resolveRequiredType(@Parameter("fqn") String fqn, @Parameter("dependencies") List<? extends ArtifactDescriptor> dependencies);

    /**
     * Return the list of Java types required by this artifact (i.e. which are
     * referenced from it).
     * 
     * @return The list of required java types.
     */
    @Outgoing
    @RequiresType
    List<TypeDescriptor> getRequiresTypes();

    /**
     * Defines the REQUIRES relation.
     */
    @Relation("REQUIRES")
    @Retention(RUNTIME)
    public @interface RequiresType {
    }

}
