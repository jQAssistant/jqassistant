package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface MavenProjectDirectoryDescriptor extends MavenProjectDescriptor, FileDescriptor {

    @Relation("CREATES")
    List<ArtifactDescriptor> getCreatesArtifacts();

    @Relation("HAS_PARENT")
    MavenProjectDescriptor getParent();

    void setParent(MavenProjectDescriptor parentDescriptor);

    @Relation("HAS_MODULE")
    List<MavenProjectDescriptor> getModules();
}
