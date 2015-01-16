package com.buschmais.jqassistant.plugin.maven3.api.model;

import org.apache.maven.model.License;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Descriptor for a license entry defined in a pom.xml.
 * 
 * @see License
 * @author ronald.kunzmann@buschmais.com
 *
 */
@Label("License")
public interface MavenLicenseDescriptor extends MavenDescriptor {

    /**
     * Get the full legal name of the license.
     * 
     * @return The name.
     */
    @Property("name")
    String getName();

    /**
     * Set the name of the license.
     * 
     * @param name
     *            The name.
     */
    void setName(String name);

    /**
     * Get the official url for the license text.
     * 
     * @return The url.
     */
    @Property("url")
    String getUrl();

    /**
     * Set the url of the license.
     * 
     * @param url
     *            The url.
     */
    void setUrl(String url);

    /**
     * Get addendum information pertaining to this license.
     * 
     * @return The comments.
     */
    @Property("comments")
    String getComments();

    /**
     * Set license comments.
     * 
     * @param comments
     *            The comments.
     */
    void setComments(String comments);

    /**
     * Get the primary method by which this project may be distributed.
     * <dl>
     * <dt>repo</dt>
     * <dd>may be downloaded from the Maven repository</dd>
     * <dt>manual</dt>
     * <dd>user must manually download and install the dependency.</dd>
     * </dl>
     * 
     * @return The distribution information.
     */
    @Property("distribution")
    String getDistribution();

    /**
     * Set distribution information.
     * 
     * @param distribution
     *            The distribution information.
     */
    void setDistribution(String distribution);
}
