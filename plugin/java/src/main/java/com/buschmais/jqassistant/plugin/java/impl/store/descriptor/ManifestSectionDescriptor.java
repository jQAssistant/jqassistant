package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Relation;
import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;

import java.util.List;

import static com.buschmais.cdo.neo4j.api.annotation.Relation.Outgoing;

@Label("MANIFESTSECTION")
public interface ManifestSectionDescriptor extends NamedDescriptor {

    @Relation("HAS")
    @Outgoing
    List<ManifestEntryDescriptor> getManifestEntries();

}
