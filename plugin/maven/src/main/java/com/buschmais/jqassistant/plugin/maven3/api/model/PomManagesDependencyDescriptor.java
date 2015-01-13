package com.buschmais.jqassistant.plugin.maven3.api.model;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;

import com.buschmais.jqassistant.plugin.common.api.model.BaseDependencyDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Descriptor for relation between POM and a managed dependency.
 * 
 * @see DependencyManagement
 * @see Dependency
 * @author ronald.kunzmann@buschmais.com
 */
@Relation("MANAGES_DEPENDENCY")
public interface PomManagesDependencyDescriptor extends BaseDependencyDescriptor {

    /**
     * Get the dependent POM.
     * 
     * @return The dependent POM.
     */
    @Outgoing
    MavenPomXmlDescriptor getDependent();

    /**
     * Get the artifact dependency.
     * 
     * @return The artifact dependency.
     */
    @Incoming
    MavenArtifactDescriptor getDependency();
}
