package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenRepositoryDescriptor.ContainsGroupId;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Label(value = "GroupId", usingIndexedPropertyOf = NamedDescriptor.class)
public interface MavenGroupIdDescriptor extends MavenDescriptor, NamedDescriptor {

    @Relation("CONTAINS_ARTIFACT_ID")
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface ContainsArtifactId {
    }

    @Incoming
    @ContainsGroupId
    MavenRepositoryDescriptor getRepository();

    void setRepository(MavenRepositoryDescriptor repository);

    @ContainsArtifactId
    List<MavenArtifactIdDescriptor> getArtifactIds();

}
