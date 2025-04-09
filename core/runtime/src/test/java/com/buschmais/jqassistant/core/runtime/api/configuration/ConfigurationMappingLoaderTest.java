package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.report.api.configuration.Build;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.shared.aether.configuration.Plugin;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationMappingLoader;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SysPropConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.microprofile.config.spi.ConfigSource.DEFAULT_ORDINAL;

/**
 * Tests for the {@link ConfigurationMappingLoader}.
 */
class ConfigurationMappingLoaderTest {

    public static final File USER_HOME = new File("src/test/resources/configuration/userhome");

    public static final File WORKING_DIRECTORY = new File("src/test/resources/configuration/working directory");

    public static final ConfigSource BUILD_CONFIG_SOURCE = new ConfigurationBuilder("Build", DEFAULT_ORDINAL).with(Build.class, Build.NAME, "Test")
        .with(Build.class, Build.TIMESTAMP, ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()))
        .build();

    /**
     * Load all yaml/yml config files from the working directory.
     */
    @Test
    void loadFromDefaultConfigLocations() {
        Configuration configuration = getConfiguration(emptyList());

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
        Configuration configuration = getConfiguration(singletonList(".jqassistant/scan/scan.yaml"));

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
        Configuration configuration = getConfiguration(singletonList(".jqassistant/profile.yml"), List.of("test-profile"));

        assertThat(configuration.scan()
            .properties()).containsEntry("profile-user-value", "test-value");
    }

    @Test
    void ignoreProperty() {
        String unknownProperty = "jqassistant.unknown";

        Configuration configuration = ConfigurationMappingLoader.builder(Configuration.class, emptyList())
            .withUserHome(USER_HOME)
            .withWorkingDirectory(WORKING_DIRECTORY)
            .withIgnoreProperties(Set.of(unknownProperty))
            .load(BUILD_CONFIG_SOURCE, new PropertiesConfigSource(Map.of(unknownProperty, "test value"), "Test", DEFAULT_ORDINAL));

        assertThat(configuration).isNotNull();
    }

    @Test
    @SetEnvironmentVariable(key = "jqassistant_scan_continue_on_error", value = "false")
    void overrideFromEnvVariable() {
        Configuration configuration = getConfiguration(emptyList());
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
            Configuration configuration = getConfiguration(emptyList());
            assertThat(configuration.scan()
                .continueOnError()).isFalse();
        } finally {
            System.clearProperty(continueOnError);
        }
    }

    private Configuration getConfiguration(List<String> configLocations) {
        return getConfiguration(configLocations, emptyList());
    }

    private Configuration getConfiguration(List<String> configLocations, List<String> profiles) {
        return ConfigurationMappingLoader.builder(Configuration.class, configLocations)
            .withUserHome(USER_HOME)
            .withWorkingDirectory(WORKING_DIRECTORY)
            .withClasspath()
            .withEnvVariables()
            .withProfiles(profiles)
            .load(BUILD_CONFIG_SOURCE, new SysPropConfigSource());
    }
}
