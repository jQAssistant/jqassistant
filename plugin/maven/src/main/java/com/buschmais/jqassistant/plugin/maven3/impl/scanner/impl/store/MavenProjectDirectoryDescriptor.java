package com.buschmais.jqassistant.plugin.maven3.impl.scanner.impl.store;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface MavenProjectDirectoryDescriptor extends MavenProjectDescriptor, FileDescriptor {

    @Relation("CREATES")
    List<ArtifactDescriptor> getCreatesArtifacts();

    @Relation("HAS_PARENT")
    MavenProjectDescriptor getParent();

    void setParent(MavenProjectDescriptor parentDescriptor);
}
