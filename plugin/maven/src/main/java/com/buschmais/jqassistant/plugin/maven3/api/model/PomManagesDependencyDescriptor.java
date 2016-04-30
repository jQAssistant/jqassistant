package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;

/**
 * Descriptor for relation between POM and a managed dependency.
 * 
 * @see DependencyManagement
 * @see Dependency
 * @author ronald.kunzmann@buschmais.com
 */
@Relation("MANAGES_DEPENDENCY")
public interface PomManagesDependencyDescriptor extends MavenDependencyDescriptor {

    /**
     * Get the dependent POM.
     * 
     * @return The dependent POM.
     */
    @Outgoing
    MavenPomDescriptor getDependent();

}
