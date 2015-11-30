package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Describes a maven project as it has been executed during a maven build.
 */
public interface MavenProjectDirectoryDescriptor extends MavenProjectDescriptor, DirectoryDescriptor {

    /**
     * Return the artifacts created by this project.
     * 
     * @return The artifacts.
     */
    @Relation("CREATES")
    List<ArtifactFileDescriptor> getCreatesArtifacts();

    /**
     * Return the parent of this project.
     * 
     * @return The parent.
     */
    @Relation("HAS_PARENT")
    MavenProjectDescriptor getParent();

    void setParent(MavenProjectDescriptor parentDescriptor);

    /**
     * Return the modules of this project.
     * 
     * @return The modules.
     */
    @Relation("HAS_MODULE")
    List<MavenProjectDescriptor> getModules();

    /**
     * Return the pom.xml file that defines this project.
     * 
     * @return The pom.xml file.
     */
    @Relation("HAS_MODEL")
    FileDescriptor getModel();

    void setModel(FileDescriptor model);
}
