package com.buschmais.jqassistant.core.configuration.impl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationLoader;
import com.buschmais.jqassistant.core.plugin.api.configuration.Plugin;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.store.api.configuration.Store;

import org.junit.jupiter.api.Test;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link ConfigurationLoaderImpl}.
 */
class ConfigurationLoaderImplTest {

    public static final File WORKING_DIRECTORY = new File("src/test/resources/working directory");

    private File configurationDirectory = ConfigurationLoader.getDefaultConfigurationDirectory(WORKING_DIRECTORY);

    private ConfigurationLoader configurationLoader = new ConfigurationLoaderImpl(configurationDirectory);

    @Test
    void defaultConfigurationDirectory() {
        assertThat(configurationDirectory).isEqualTo(new File(WORKING_DIRECTORY, ".jqassistant"));
    }

    @Test
    void loadFromFiles() throws URISyntaxException {
        Configuration configuration = configurationLoader.load(Configuration.class);

        assertThat(configuration).isNotNull();
        List<Plugin> plugins = configuration.plugins();
        assertThat(plugins).isNotNull()
            .hasSize(2);

        Scan scan = configuration.scan();
        assertThat(scan).isNotNull();
        assertThat(scan.continueOnError()).isEqualTo(true);

        Store store = configuration.store();
        assertThat(store).isNotNull();
        assertThat(store.uri()).isEqualTo(of(new URI("bolt://localhost:7687")));
    }

    @Test
    void overrideFromSystemProperty() {
        System.setProperty("jqassistant.scan.continue-on-error", "false");
        try {
            Configuration configuration = configurationLoader.load(Configuration.class);
            assertThat(configuration.scan()
                .continueOnError()).isEqualTo(false);
        } finally {
            System.clearProperty("jqassistant.scan.continue-on-error");
        }
    }
}
