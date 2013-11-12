package com.buschmais.jqassistant.plugin.common.impl.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Property;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.ParentDescriptor;

/**
 * Describes an artifact.
 *
 * @author Herklotz
 */
@Label(value = "ARTIFACT", usingIndexOf = FullQualifiedNameDescriptor.class)
public interface ArtifactDescriptor extends ParentDescriptor, NamedDescriptor {

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
}
