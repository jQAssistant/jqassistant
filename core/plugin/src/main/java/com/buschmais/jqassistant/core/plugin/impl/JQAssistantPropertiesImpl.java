package com.buschmais.jqassistant.core.plugin.impl;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.plugin.api.JQAssistantProperties;

/**
 * JQAssistant properties implementation.
 */
public class JQAssistantPropertiesImpl implements JQAssistantProperties {

    /** Property key for `{@value}`. */
    private static final String PROPERTY_KEY_VERSION = "project.version";

    /** The properties. */
    private final Properties properties;

    /**
     * Create a new instance.
     */
    private JQAssistantPropertiesImpl() {

        properties = new Properties();
        try {
            properties.load(JQAssistantPropertiesImpl.class.getResourceAsStream("/META-INF/jqassistant.properties"));
        } catch (IOException e) {
            LoggerFactory.getLogger(JQAssistantPropertiesImpl.class).error("Unable to read jqassistant.properties.", e);
        }
    }

    /**
     * Get the singleton instance of the properties.
     *
     * @return the instance
     */
    public static JQAssistantPropertiesImpl getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public String getVersion() {
        return (String) properties.get(PROPERTY_KEY_VERSION);
    }

    /** The instance holder. */
    private static class LazyHolder {
        private static final JQAssistantPropertiesImpl INSTANCE = new JQAssistantPropertiesImpl();
    }
}
