package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Exclusion")
public interface MavenExcludesDescriptor extends MavenDescriptor {

    String getGroupId();

    void setGroupId(String groupId);

    String getArtifactId();

    void setArtifactId(String artifactId);
}
