package com.buschmais.jqassistant.scm.maven.configuration.source;

import org.apache.maven.settings.Settings;

import static java.util.Arrays.asList;

/**
 * Config source for Maven {@link Settings}.
 */
public class SettingsConfigSource extends AbstractObjectValueConfigSource<Settings> {

    public SettingsConfigSource(Settings settings) {
        super("Maven Settings", settings, "settings", asList("settings.localRepository"));
    }

}
