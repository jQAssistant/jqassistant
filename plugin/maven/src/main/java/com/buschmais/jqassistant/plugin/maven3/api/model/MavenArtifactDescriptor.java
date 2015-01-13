package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

/**
 * Describes a maven artifact.
 * 
 * @author ronald.kunzmann@buschmais.com
 */
@Label("MavenArtifact")
public interface MavenArtifactDescriptor extends MavenDescriptor, ArtifactDescriptor {

    @Incoming
    List<PomManagesDependencyDescriptor> getPomManagedDependents();

    @Incoming
    List<ProfileManagesDependencyDescriptor> getProfileManagedDependents();

    @Incoming
    List<ProfileDependsOnDescriptor> getProfileDependents();

}
