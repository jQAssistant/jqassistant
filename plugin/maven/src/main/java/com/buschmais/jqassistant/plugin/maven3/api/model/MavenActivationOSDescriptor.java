package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

import org.apache.maven.model.ActivationOS;

/**
 * Descriptor for an activator which will detect an operating system's
 * attributes in order to activate its profile.
 * 
 * @author ronald.kunzmann@buschmais.com
 * @see ActivationOS
 */
@Label("ActivationOS")
public interface MavenActivationOSDescriptor extends MavenDescriptor {

    /**
     * Get the architecture of the OS to be used to activate a profile.
     * 
     * @return String
     */
    @Property("arch")
    String getArch();

    /**
     * Set the architecture of the OS to be used to activate a profile.
     * 
     * @param arch
     */
    void setArch(String arch);

    /**
     * Get the general family of the OS to be used to activate a profile (e.g.
     * 'windows').
     * 
     * @return String
     */
    @Property("family")
    String getFamily();

    /**
     * Set the general family of the OS to be used to activate a profile (e.g.
     * 'windows').
     * 
     * @param family
     */
    void setFamily(String family);

    /**
     * Get the name of the OS to be used to activate a profile.
     * 
     * @return String
     */
    @Property("name")
    String getName();

    /**
     * Set the name of the OS to be used to activate a profile.
     * 
     * @param name
     */
    void setName(String name);

    /**
     * Get the version of the OS to be used to activate a profile.
     * 
     * @return String
     */
    @Property("version")
    String getVersion();

    /**
     * Set the version of the OS to be used to activate a profile.
     * 
     * @param version
     */
    void setVersion(String version);

}
