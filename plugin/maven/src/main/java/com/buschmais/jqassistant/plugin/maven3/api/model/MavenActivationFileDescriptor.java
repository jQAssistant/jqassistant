package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

import org.apache.maven.model.ActivationFile;

/**
 * Descriptor for the file specification used to activate a profile.
 * 
 * @author ronald.kunzmann@buschmais.com
 * @see ActivationFile
 */
@Label("ActivationFile")
public interface MavenActivationFileDescriptor extends MavenDescriptor {

    /**
     * Get the name of the file that should exist to activate a profile.
     * 
     * @return String
     */
    @Property("exists")
    String getExists();

    /**
     * Set the name of the file that should exist to activate a profile.
     * 
     * @param exists
     */
    void setExists(String exists);

    /**
     * Get the name of the file that should be missing to activate a profile.
     * 
     * @return String
     */
    @Property("missing")
    String getMissing();

    /**
     * Set the name of the file that should be missing to activate a profile.
     * 
     * @param missing
     */
    void setMissing(String missing);
}
