package com.buschmais.jqassistant.core.runtime.api.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VersionProvider {

    private static final String PROPERTIES_FILE = "/META-INF/jqassistant.properties";
    private static final String VERSION_KEY = "jqassistant.version";

    public static String getVersion() {
        Properties props = new Properties();
        InputStream inputStream = VersionProvider.class.getResourceAsStream(PROPERTIES_FILE);
        if (inputStream == null) {
            log.warn("Properties file not found: " + PROPERTIES_FILE);
        }
        try {
            props.load(inputStream);
        } catch (IOException e) {
            log.warn("Failed to load inout stream from " + PROPERTIES_FILE);
        }
        return props.getProperty(VERSION_KEY);
    }
}

