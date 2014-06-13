package com.buschmais.jqassistant.plugin.maven3.impl.scanner.impl.store;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label(value = "Project", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface MavenProjectDescriptor extends MavenDescriptor, FullQualifiedNameDescriptor {

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

    @Relation("CREATES")
    List<ArtifactDescriptor> getCreatesArtifacts();

    @Outgoing
    List<DependsOnDescriptor> getDependencies();

    @Incoming
    List<DependsOnDescriptor> getDependents();
}
