package com.buschmais.jqassistant.core.runtime.impl.configuration;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationMappingLoader;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.shared.configuration.Plugin;

import io.smallrye.config.SysPropConfigSource;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link ConfigurationMappingLoader}.
 */
class ConfigurationMappingLoaderTest {

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
        assertThat(scan.continueOnError()).isTrue();
        assertThat(scan.properties()).containsEntry("user-value", "default");
        assertThat(scan.properties()).containsEntry("overwritten-user-value", "overwritten");
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
        assertThat(scan.continueOnError()).isTrue();
        assertThat(scan.reset()).isPresent()
            .hasValue(true);
        assertThat(scan.properties()).containsEntry("user-value", "default");
        assertThat(scan.properties()).containsEntry("overwritten-user-value", "overwritten");
    }

    @Test
    void profile() {
        TestConfiguration configuration = getConfiguration(singletonList(".jqassistant/profile.yml"), List.of("test-profile"));

        assertThat(configuration.scan()
            .properties()).containsEntry("profile-user-value", "test-value");
    }

    @Test
    @SetEnvironmentVariable(key = "jqassistant_scan_continue_on_error", value = "false")
    void overrideFromEnvVariable() {
        TestConfiguration configuration = getConfiguration(emptyList());
        assertThat(configuration.scan()
            .continueOnError()).isFalse();
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
                .continueOnError()).isFalse();
        } finally {
            System.clearProperty(continueOnError);
        }
    }

    private TestConfiguration getConfiguration(List<String> configLocations) {
        return getConfiguration(configLocations, emptyList());
    }

    private TestConfiguration getConfiguration(List<String> configLocations, List<String> profiles) {
        return ConfigurationMappingLoader.builder(TestConfiguration.class, configLocations)
            .withUserHome(USER_HOME)
            .withWorkingDirectory(WORKING_DIRECTORY)
            .withClasspath()
            .withEnvVariables()
            .withProfiles(profiles)
            .load(new SysPropConfigSource());
    }
}
