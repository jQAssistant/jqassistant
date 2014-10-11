package com.buschmais.jqassistant.plugin.common.api.model;

import static com.buschmais.xo.api.annotation.ResultOf.Parameter;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.*;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Describes an artifact.
 * 
 * @author Herklotz
 */
@Label(value = "Artifact", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
@Generic(Generic.GenericLanguageElement.Artifact)
public interface ArtifactDescriptor extends Descriptor, NamedDescriptor, FullQualifiedNameDescriptor, FileContainerDescriptor {

    /**
     * @return the group
     */
    @Property("group")
    public String getGroup();

    /**
     * @param group
     *            the group to set
     */
    public void setGroup(String group);

    /**
     * @return the name
     */
    @Property("name")
    public String getName();

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name);

    /**
     * @return the version
     */
    @Property("version")
    public String getVersion();

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(String version);

    @Property("classifier")
    public String getClassifier();

    public void setClassifier(String classifier);

    @Property("type")
    public String getType();

    public void setType(String type);

    @ResultOf
    @Cypher("match (a),(f) where id(a)={this} and id(f)={file} create unique (a)-[:CONTAINS]->(f)")
    public void addContains(@Parameter("file") FileDescriptor file);

    @Outgoing
    List<DependsOnDescriptor> getDependencies();

    @Incoming
    List<DependsOnDescriptor> getDependents();
}
