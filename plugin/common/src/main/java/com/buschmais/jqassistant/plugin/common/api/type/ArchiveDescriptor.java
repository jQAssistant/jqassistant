package com.buschmais.jqassistant.plugin.common.api.type;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Label("Archive")
public interface ArchiveDescriptor extends FileDescriptor {

    @Relation("CONTAINS")
    @Outgoing
    List<FileDescriptor> getContents();
}
