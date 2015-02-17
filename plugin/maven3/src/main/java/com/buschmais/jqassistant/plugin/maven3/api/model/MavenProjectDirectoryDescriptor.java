package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface MavenProjectDirectoryDescriptor extends MavenProjectDescriptor, DirectoryDescriptor {

    @Relation("CREATES")
    List<ArtifactFileDescriptor> getCreatesArtifacts();

    @Relation("HAS_PARENT")
    MavenProjectDescriptor getParent();

    void setParent(MavenProjectDescriptor parentDescriptor);

    @Relation("HAS_MODULE")
    List<MavenProjectDescriptor> getModules();
}
