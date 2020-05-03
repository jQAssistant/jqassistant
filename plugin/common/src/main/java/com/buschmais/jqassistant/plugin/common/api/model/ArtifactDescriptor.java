package com.buschmais.jqassistant.plugin.common.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Describes an artifact.
 *
 * @author ronald.kunzmann@buschmais.com
 *
 */
@Label(value = "Artifact", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface ArtifactDescriptor extends NamedDescriptor, FullQualifiedNameDescriptor {

    /**
     * @return the group
     */
    @Property("group")
    String getGroup();

    /**
     * @param group
     *            the group to set
     */
    void setGroup(String group);

    /**
     * @return the name
     */
    @Override
    @Property("name")
    String getName();

    /**
     * @param name
     *            the name to set
     */
    @Override
    void setName(String name);

    /**
     * @return the version
     */
    @Property("version")
    String getVersion();

    /**
     * @param version
     *            the version to set
     */
    void setVersion(String version);

    @Property("classifier")
    String getClassifier();

    void setClassifier(String classifier);

    @Property("type")
    String getType();

    void setType(String type);

    @Outgoing
    List<DependsOnDescriptor> getDependencies();

    @Incoming
    List<DependsOnDescriptor> getDependents();

    /**
     * Create a dependency to another {@link ArtifactDescriptor}.
     *
     * @param dependency
     *            The {@link ArtifactDescriptor} representing the dependency.
     * @param scope
     *            The scope.
     * @param optional
     *            <code>true</code> if the dependency is optional.
     */
    @ResultOf
    @Cypher("MATCH (artifact:Artifact),(dependency:Artifact) WHERE id(artifact)=$this and id(dependency)=$dependency MERGE (artifact)-[dependsOn:DEPENDS_ON{scope:$scope,optional:$optional}]->(dependency) RETURN dependsOn")
    void addDependency(@Parameter("dependency") ArtifactDescriptor dependency, @Parameter("scope") String scope, @Parameter("optional") boolean optional);
}
