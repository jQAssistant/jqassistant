package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileContainerDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

/**
 * Defines a Java artifact.
 */
public interface JavaArtifactFileDescriptor extends JavaDescriptor, ArtifactFileDescriptor, FileContainerDescriptor {

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
     * @param fqn The fully qualified name.
     * @return The type.
     */
    @ResultOf
    @Cypher("MATCH\n" +
        "  (dependency:Artifact)-[:CONTAINS|REQUIRES]->(type:Type{fqn:$fqn})\n" +
        "WITH\n" +
        "  dependency, type\n" +
        "MATCH\n" +
        "  shortestPath((artifact)-[:DEPENDS_ON*]->(dependency))\n" +
        "WHERE\n" +
        "  id(artifact)=$this\n" +
        "  and artifact <> dependency\n" +
        "RETURN\n" +
        "  type\n" +
        "LIMIT 1")
    TypeDescriptor resolveRequiredType(@Parameter("fqn") String fqn);

    /**
     * Find a contained or required {@link ModuleDescriptor} for the given module name and version in the dependencies of the current artifact
     *
     * @param moduleName the module name.
     * @param version    the module version.
     * @return The {@link ModuleDescriptor} or <code>null</code>
     */
    @ResultOf
    @Cypher("MATCH\n" +
        "  (dependency:Artifact)-[:CONTAINS|REQUIRES]->(module:Java:Module{fqn:$moduleName})\n" +
        "WITH\n" +
        "  dependency, module\n" +
        "MATCH\n" +
        "  shortestPath((artifact)-[:DEPENDS_ON*0..]->(dependency))\n" +
        "WHERE\n" +
        "  id(artifact)=$this\n" +
        "  and artifact <> dependency\n" +
        "RETURN\n" +
        "  module\n" +
        "LIMIT 1")
    ModuleDescriptor findModuleInDependencies(@Parameter("moduleName") String moduleName,
                                              @Parameter("version") String version);

    @ResultOf
    @Cypher("MATCH\n" +
        "  (artifact:Artifact)\n" +
        "WHERE\n" +
        "  id(artifact)=$this\n" +
        "MERGE\n" +
        "  (artifact)-[:REQUIRES]->(module:Java:ByteCode:Module{fqn:$moduleName})\n" +
        "SET\n" +
        "  module.version=$version\n" +
        "RETURN\n" +
        "  module")
    ModuleDescriptor requireModule(@Parameter("moduleName") String moduleName, @Parameter("version") String version);
}
