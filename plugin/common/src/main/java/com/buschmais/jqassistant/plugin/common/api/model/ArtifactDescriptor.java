package com.buschmais.jqassistant.plugin.common.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
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
}
