package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.AbstractDependencyDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Dependency")
public interface MavenDependencyDescriptor extends MavenDescriptor, AbstractDependencyDescriptor {

    @Relation("TO_ARTIFACT")
    MavenArtifactDescriptor getToArtifact();

    void setToArtifact(MavenArtifactDescriptor toArtifact);

    @Relation("EXCLUDES")
    List<MavenExcludesDescriptor> getExclusions();

}
