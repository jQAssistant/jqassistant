package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.type.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("ManifestSection")
public interface ManifestSectionDescriptor extends NamedDescriptor {

    @Relation("HAS")
    @Outgoing
    List<ManifestEntryDescriptor> getManifestEntries();

}
