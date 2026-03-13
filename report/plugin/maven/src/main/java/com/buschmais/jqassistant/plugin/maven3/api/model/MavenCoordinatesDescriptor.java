package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Property;

public interface MavenCoordinatesDescriptor {

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

    @Property("classifier")
    String getClassifier();

    void setClassifier(String classifier);
}
