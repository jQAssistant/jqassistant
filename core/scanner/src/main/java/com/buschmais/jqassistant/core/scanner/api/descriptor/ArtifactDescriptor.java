package com.buschmais.jqassistant.core.scanner.api.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Property;
import com.buschmais.cdo.neo4j.api.annotation.Relation;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;

import java.util.Set;

/**
 * Describes an artifact.
 *
 * @author Herklotz
 */
@Label(value = "ARTIFACT", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface ArtifactDescriptor extends Descriptor, NamedDescriptor, FullQualifiedNameDescriptor {

    /**
     * @return the group
     */
    @Property("GROUP")
    public String getGroup();

    /**
     * @param group the group to set
     */
    public void setGroup(String group);

    /**
     * @return the name
     */
    @Property("NAME")
    public String getName();

    /**
     * @param name the name to set
     */
    public void setName(String name);

    /**
     * @return the version
     */
    @Property("VERSION")
    public String getVersion();

    /**
     * @param version the version to set
     */
    public void setVersion(String version);

    @Property("CLASSIFIER")
    public String getClassifier();

    public void setClassifier(String classifier);

    @Property("TYPE")
    public String getType();

    public void setType(String type);

    /**
     * Return the contained descriptors.
     *
     * @return The contained descriptors.
     */
    @Relation("CONTAINS")
    public Set<FileDescriptor> getContains();
}