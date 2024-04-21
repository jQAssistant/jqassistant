package com.buschmais.jqassistant.core.runtime.impl.configuration;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationLoader;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.shared.configuration.Plugin;
import com.buschmais.jqassistant.core.store.api.configuration.Store;

import io.smallrye.config.SysPropConfigSource;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link ConfigurationLoader}.
 */
class ConfigurationLoaderTest {

    public static final File USER_HOME = new File("src/test/resources/userhome");

    public static final File WORKING_DIRECTORY = new File("src/test/resources/working directory");

    /**
     * Load all yaml/yml config files from the working directory.
     */
    @Test
    void loadFromDefaultConfigLocations() throws URISyntaxException {
        TestConfiguration configuration = getConfiguration(emptyList());

        assertThat(configuration).isNotNull();

        List<Plugin> defaultPlugins = configuration.defaultPlugins();
        assertThat(defaultPlugins).isNotNull()
            .hasSize(1);

        List<Plugin> plugins = configuration.plugins();
        assertThat(plugins).isNotNull()
            .hasSize(2);

        Scan scan = configuration.scan();
        assertThat(scan).isNotNull();
        assertThat(scan.continueOnError()).isEqualTo(true);
        assertThat(scan.properties()
            .get("user-value")).isEqualTo("default");
        assertThat(scan.properties()
            .get("overwritten-user-value")).isEqualTo("overwritten");

        Store store = configuration.store();
        assertThat(store).isNotNull();
        assertThat(store.uri()).isEqualTo(of(new URI("bolt://localhost:7687")));

    }

    /**
     * Load only a specific config file from the working directory.
     */
    @Test
    void loadFromConfigLocation() {
        TestConfiguration configuration = getConfiguration(singletonList(".jqassistant/scan/scan.yaml"));

        assertThat(configuration).isNotNull();

        List<Plugin> plugins = configuration.plugins();
        assertThat(plugins).isEmpty();

        Scan scan = configuration.scan();
        assertThat(scan).isNotNull();
        assertThat(scan.continueOnError()).isEqualTo(true);
        assertThat(scan.properties()
            .get("user-value")).isEqualTo("default");
        assertThat(scan.properties()
            .get("overwritten-user-value")).isEqualTo("overwritten");

        Store store = configuration.store();
        assertThat(store).isNotNull();
        assertThat(store.uri()).isEmpty();
    }

    @Test
    @SetEnvironmentVariable(key = "jqassistant_scan_continue_on_error", value = "false")
    void overrideFromEnvVariable() {
        TestConfiguration configuration = getConfiguration(emptyList());
        assertThat(configuration.scan()
            .continueOnError()).isEqualTo(false);
    }

    @Test
    void overrideFromSystemProperty() {
        overrideFromSystemProperty("jqassistant.scan.continue-on-error");
    }

    @Test
    void useExpressionFromSystemProperty() {
        overrideFromSystemProperty("continueOnError");
    }

    private void overrideFromSystemProperty(String continueOnError) {
        System.setProperty(continueOnError, "false");
        try {
            TestConfiguration configuration = getConfiguration(emptyList());
            assertThat(configuration.scan()
                .continueOnError()).isEqualTo(false);
        } finally {
            System.clearProperty(continueOnError);
        }
    }

    private TestConfiguration getConfiguration(List<String> configLocations) {
        return ConfigurationLoader.builder(TestConfiguration.class, configLocations)
            .withUserHome(USER_HOME)
            .withWorkingDirectory(WORKING_DIRECTORY)
            .load(new SysPropConfigSource());
    }
}
