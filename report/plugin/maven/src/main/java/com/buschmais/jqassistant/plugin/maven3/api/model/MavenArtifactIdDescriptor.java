package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenGroupIdDescriptor.ContainsArtifactId;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Label(value = "ArtifactId", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface MavenArtifactIdDescriptor extends MavenDescriptor, FullQualifiedNameDescriptor, NamedDescriptor {

    @Relation("CONTAINS_VERSION")
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface ContainsVersion {
    }

    @Incoming
    @ContainsArtifactId
    MavenGroupIdDescriptor getGroupId();

    void setGroupId(MavenGroupIdDescriptor groupId);

    @ContainsVersion
    List<MavenVersionDescriptor> getVersions();

}
