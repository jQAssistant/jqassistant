package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

/**
 * Defines a Java artifact.
 */
public interface JavaArtifactFileDescriptor extends JavaDescriptor, ArtifactFileDescriptor {

    /**
     * Determine the number of dependencies of this artifact.
     *
     * @return The number of dependencies.
     */
    @ResultOf
    @Cypher("MATCH (artifact:Artifact)-[d:DEPENDS_ON]->(:Artifact) WHERE id(artifact)=$this RETURN count(d)")
    long getNumberOfDependencies();

    /**
     * Resolves a required type with a given name from a dependency (direct or
     * transitive).
     *
     * @param fqn
     *            The fully qualified name.
     * @return The type.
     */
    @ResultOf
    @Cypher("MATCH\n" +
        "  (dependency:Artifact)-[:CONTAINS|REQUIRES]->(type:Type)\n" +
        "WHERE\n" +
        "  type.fqn=$fqn\n" +
        "WITH\n" +
        "  dependency, type\n" +
        "MATCH\n" +
        "  shortestPath((artifact)-[:DEPENDS_ON*]->(dependency))\n" +
        "WHERE\n" +
        "  id(artifact)=$this\n" +
        "RETURN\n" +
        "  type\n" +
        "LIMIT 1")
    TypeDescriptor resolveRequiredType(@Parameter("fqn") String fqn);
}
