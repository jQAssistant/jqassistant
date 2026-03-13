package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Descriptor for the Software Configuration Management information of a POM.
 *
 * @see http://maven.apache.org/pom.html#SCM[SCM section in the Apache Maven POM Reference]
 */
@Label(value = "Scm")
public interface MavenScmDescriptor extends MavenDescriptor {

    /**
     * Returns the connection of read-only access to the source-control system
     * used by the project.
     *
     * @return the read-only connection to the source-control system or
     *         `null` if this information is not present.
     */
    @Property("connection")
    String getConnection();

    void setConnection(String connection);

    @Property("developerConnection")
    String getDeveloperConnection();

    void setDeveloperConnection(String connection);

    /**
     * Returns the tag that this project lives under.
     *
     * @return the tag that this project lives under or `null` if this
     *         information is not present.
     */
    @Property("tag")
    String getTag();

    void setTag(String tag);

    /**
     * Returns URL of the publicly browsalbe repository.
     *
     * @return the URL of the publicly browsable repository or `null` if this
     *         information is not present.
     */
    @Property("url")
    String getUrl();

    void setUrl(String url);
}
