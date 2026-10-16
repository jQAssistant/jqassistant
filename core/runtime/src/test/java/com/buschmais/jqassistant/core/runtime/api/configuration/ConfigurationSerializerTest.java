package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.time.ZonedDateTime;
import java.util.Properties;

import com.buschmais.jqassistant.core.report.api.configuration.Build;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationMappingLoader;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationSerializer;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.WithDefault;
import io.smallrye.config.source.yaml.YamlConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationMappingLoaderTest.USER_HOME;
import static com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationMappingLoaderTest.WORKING_DIRECTORY;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.microprofile.config.spi.ConfigSource.DEFAULT_ORDINAL;

/**
 * Tests for the {@link ConfigurationSerializer}.
 */
@Slf4j
class ConfigurationSerializerTest {

    private final ConfigurationSerializer<Configuration> configurationSerializer = new ConfigurationSerializer<>();

    public static final ConfigSource BUILD_CONFIG_SOURCE = new ConfigurationBuilder("Build", DEFAULT_ORDINAL).with(Build.class, Build.NAME, "Test")
        .with(Build.class, Build.TIMESTAMP, ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now()))
        .build();

    @ConfigMapping(prefix = "jqassistant")
    public interface TestConfiguration extends Configuration {

        Test test();

        @ConfigMapping(prefix = "jqassistant.test")
        interface Test {
            @WithDefault("foo")
            String value();
        }
    }

    @Test
    void types() {
        Properties properties = new Properties();
        properties.put("jqassistant.store.uri", "bolt://localhost:7687");
        properties.put("jqassistant.analyze.rule.directory", "target/rules");
        properties.put("jqassistant.analyze.report.fail-on-severity", "BLOCKER");
        properties.put("jqassistant.plugins[0].group-id", "org.jqassistant.plugin");
        properties.put("jqassistant.plugins[0].artifact-id", "test-plugin");
        properties.put("jqassistant.plugins[0].version", "1.0.0");
        PropertiesConfigSource configSource = new PropertiesConfigSource(properties, "test");
        TestConfiguration testConfiguration = ConfigurationMappingLoader.builder(TestConfiguration.class)
            .load(BUILD_CONFIG_SOURCE, configSource);

        String yaml = toYaml(testConfiguration);

        assertThat(yaml).contains("group-id: org.jqassistant.plugin")
            .contains("version: 1.0.0")
            .contains("uri: bolt://localhost:7687")
            .contains("directory: target/rules")
            .contains("fail-on-severity: BLOCKER")
            .contains("value: foo");
    }

    @Test
    void serializeAndRestoreConfiguration() {
        TestConfiguration testConfiguration = ConfigurationMappingLoader.builder(TestConfiguration.class)
            .withUserHome(USER_HOME)
            .withWorkingDirectory(WORKING_DIRECTORY)
            .load(BUILD_CONFIG_SOURCE);

        String yaml = toYaml(testConfiguration);

        YamlConfigSource yamlConfigSource = new YamlConfigSource("yaml", yaml);
        assertThat(yamlConfigSource.getValue("jqassistant.analyze.execute-applied-concepts")).isEqualTo("true");
        TestConfiguration restoredConfiguration = ConfigurationMappingLoader.builder(TestConfiguration.class)
            .load(yamlConfigSource);
        Scan scan = restoredConfiguration.scan();
        assertThat(scan.properties()).containsEntry("user-value", "default");
        assertThat(scan.properties()).containsEntry("overwritten-user-value", "overwritten");
        assertThat(restoredConfiguration.test()
            .value()).isEqualTo("foo");
    }

    private String toYaml(Configuration configuration) {
        String yaml = configurationSerializer.toYaml(configuration);
        log.info("\n{}", yaml);
        return yaml;
    }

}
