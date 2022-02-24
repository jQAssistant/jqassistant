package com.buschmais.jqassistant.core.configuration.impl;

import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationLoader;
import com.buschmais.jqassistant.core.plugin.api.configuration.Plugin;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link ConfigurationLoaderImpl}.
 */
class ConfigurationLoaderImplTest {

    private ConfigurationLoader configurationLoader = new ConfigurationLoaderImpl();

    @Test
    void load() {
        File workingDirectory = new File("src/test/resources/working directory");
        File configurationDirectory = configurationLoader.getDefaultConfigurationDirectory(workingDirectory);
        assertThat(configurationDirectory).isEqualTo(new File(workingDirectory, ".jqassistant"));
        Configuration configuration = configurationLoader.load(configurationDirectory);

        assertThat(configuration).isNotNull();
        List<Plugin> plugins = configuration.plugins();
        assertThat(plugins).isNotNull().hasSize(2);

    }
}
