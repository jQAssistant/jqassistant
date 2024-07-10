package com.buschmais.jqassistant.scm.maven.configuration.source;

import java.util.Properties;
import java.util.Set;

import io.smallrye.config.common.AbstractConfigSource;

/**
 * Config source for properties provided by Maven.
 */
public class MavenPropertiesConfigSource extends AbstractConfigSource {

    public static final int CONFIGURATION_ORDINAL_MAVEN_PROPERTIES = 90;

    private Properties properties;

    public MavenPropertiesConfigSource(Properties properties, String name) {
        super(name, CONFIGURATION_ORDINAL_MAVEN_PROPERTIES);
        this.properties = properties;
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.stringPropertyNames();
    }

    @Override
    public String getValue(String key) {
        return properties.getProperty(key);
    }
}
