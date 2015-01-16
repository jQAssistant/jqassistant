package com.buschmais.jqassistant.plugin.maven3.api.model;

import org.apache.maven.model.Activation;

import com.buschmais.jqassistant.plugin.common.api.model.PropertyDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Descriptor containing conditions within the build runtime environment which
 * will trigger the automatic inclusion of the build profile.
 * 
 * @author ronald.kunzmann@buschmais.com
 * @see Activation
 */
@Label("ProfileActivation")
public interface MavenProfileActivationDescriptor extends MavenDescriptor {

    /**
     * Get if set to true, this profile will be active unless another profile in
     * this pom is activated using the command line -P option or by one of that
     * profile's activators.
     * 
     * @return boolean
     */
    @Property("activeByDefault")
    boolean isActiveByDefault();

    void setActiveByDefault(boolean activeByDefault);

    /**
     * Get specifies that this profile will be activated when a matching JDK is
     * detected. For example, <code>1.4</code> only activates on JDKs versioned
     * 1.4, while <code>!1.4</code> matches any JDK that is not version 1.4.
     * 
     * @return String
     */
    @Property("jdk")
    String getJdk();

    void setJdk(String jdk);

    /**
     * Get specifies that this profile will be activated based on existence of a
     * file.
     * 
     * @return MavenActivationFileDescriptor
     */
    @Relation("ACTIVATED_BY_FILE")
    MavenActivationFileDescriptor getActivationFile();

    void setActivationFile(MavenActivationFileDescriptor activationFile);

    /**
     * Get specifies that this profile will be activated when matching operating
     * system attributes are detected.
     * 
     * @return MavenActivationOSDescriptor
     */
    @Relation("ACTIVATED_BY_OS")
    MavenActivationOSDescriptor getActivationOS();

    void setActivationOS(MavenActivationOSDescriptor activationOS);

    /**
     * Get specifies that this profile will be activated when this system
     * property is specified.
     * 
     * @return PropertyDescriptor
     */
    @Relation("HAS_PROPERTY")
    PropertyDescriptor getProperty();

    void setProperty(PropertyDescriptor propertyDescriptor);
}
