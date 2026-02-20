package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Descriptor for a source definition in a Maven 4.1.0 build.
 */
@Label("Source")
public interface MavenSourceDescriptor extends MavenDescriptor {

    @Property("glob")
    String getGlob();

    void setGlob(String glob);

    @Property("directory")
    String getDirectory();

    void setDirectory(String directory);

    @Property("enabled")
    Boolean isEnabled();

    void setEnabled(Boolean enabled);
}
