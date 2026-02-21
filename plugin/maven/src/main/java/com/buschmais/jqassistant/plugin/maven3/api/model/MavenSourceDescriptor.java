package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Descriptor for a source definition in a Maven 4.1.0 build.
 */
@Label("Source")
public interface MavenSourceDescriptor extends MavenDescriptor {

    @Property("scope")
    String getScope();

    void setScope(String scope);

    @Property("lang")
    String getLang();

    void setLang(String lang);

    @Property("module")
    String getModule();

    void setModule(String module);

    @Property("directory")
    String getDirectory();

    void setDirectory(String directory);

    @Property("targetVersion")
    String getTargetVersion();

    void setTargetVersion(String targetVersion);

    @Property("targetPath")
    String getTargetPath();

    void setTargetPath(String targetPath);

    @Property("stringFiltering")
    Boolean isStringFiltering();

    void setStringFiltering(Boolean stringFiltering);

    @Property("enabled")
    Boolean isEnabled();

    void setEnabled(Boolean enabled);
}
