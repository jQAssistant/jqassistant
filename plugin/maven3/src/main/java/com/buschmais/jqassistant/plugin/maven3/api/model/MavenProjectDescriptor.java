package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label(value = "Project", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface MavenProjectDescriptor extends MavenDescriptor, FullQualifiedNameDescriptor, NamedDescriptor {

    @Property("groupId")
    String getGroupId();

    void setGroupId(String groupId);

    @Property("artifactId")
    String getArtifactId();

    void setArtifactId(String artifactId);

    @Property("version")
    String getVersion();

    void setVersion(String version);

    @Property("packaging")
    String getPackaging();

    void setPackaging(String packaging);

}
