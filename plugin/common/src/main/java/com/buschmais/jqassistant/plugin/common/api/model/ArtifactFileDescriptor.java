package com.buschmais.jqassistant.plugin.common.api.model;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import com.buschmais.jqassistant.plugin.common.api.report.Generic;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Describes an artifact as a file.
 * 
 * @author Herklotz
 */
@Generic(Generic.GenericLanguageElement.ArtifactFile)
public interface ArtifactFileDescriptor extends ArtifactDescriptor, FileDescriptor, FileContainerDescriptor {

    /**
     * Defines the REQUIRES relation.
     */
    @Relation("REQUIRES")
    @Retention(RUNTIME)
    @interface RequiresType {
    }
}
