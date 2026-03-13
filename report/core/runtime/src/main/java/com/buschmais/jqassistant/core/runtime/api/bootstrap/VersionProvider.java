package com.buschmais.jqassistant.core.runtime.api.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class VersionProvider {

    private static final String PROPERTIES_FILE = "/META-INF/jqassistant.properties";
    private static final String MINOR_VERSION = "jqassistant.version.minor";
    private static final String MAJOR_VERSION = "jqassistant.version.major";
    private static final String VERSION = "jqassistant.version";

    private static final VersionProvider INSTANCE = new VersionProvider();

    private final String minorVersion;
    private final String majorVersion;
    private final String version;

    public static VersionProvider getVersionProvider() {
        return INSTANCE;
    }

    private VersionProvider() {
        Properties props = new Properties();
        InputStream inputStream = VersionProvider.class.getResourceAsStream(PROPERTIES_FILE);
        if (inputStream == null) {
            log.warn("Properties file not found: " + PROPERTIES_FILE);
        }
        try {
            props.load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load input stream from " + PROPERTIES_FILE, e);
        }
        this.minorVersion = props.getProperty(MINOR_VERSION);
        this.majorVersion = props.getProperty(MAJOR_VERSION);
        this.version = props.getProperty(VERSION);
    }
}

