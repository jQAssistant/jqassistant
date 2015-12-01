package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Descriptor for relation between POM and a dependency.
 * 
 * @see org.apache.maven.model.Dependency
 * @author ronald.kunzmann@buschmais.com
 */
@Relation("DECLARES_DEPENDENCY")
public interface PomDependsOnDescriptor extends MavenDependencyDescriptor {

    /**
     * Get the dependent POM.
     * 
     * @return The dependent POM.
     */
    @Outgoing
    MavenPomDescriptor getDependent();
}
