package com.buschmais.jqassistant.plugin.maven3.api.model;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;

import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Descriptor for relation between profile and a managed dependency.
 * 
 * @see DependencyManagement
 * @see Dependency
 * @author ronald.kunzmann@buschmais.com
 */
@Relation("MANAGES_DEPENDENCY")
public interface ProfileManagesDependencyDescriptor extends MavenDependencyDescriptor {

    /**
     * Get the dependent POM.
     * 
     * @return The dependent POM.
     */
    @Outgoing
    MavenProfileDescriptor getDependent();

}
