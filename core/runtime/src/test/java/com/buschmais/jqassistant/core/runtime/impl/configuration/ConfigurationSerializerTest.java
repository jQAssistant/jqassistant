package com.buschmais.jqassistant.core.runtime.impl.configuration;

import java.util.Properties;

import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationMappingLoader;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationSerializer;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.source.yaml.YamlConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.runtime.impl.configuration.ConfigurationMappingLoaderTest.USER_HOME;
import static com.buschmais.jqassistant.core.runtime.impl.configuration.ConfigurationMappingLoaderTest.WORKING_DIRECTORY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link ConfigurationSerializer}.
 */
@Slf4j
class ConfigurationSerializerTest {

    private final ConfigurationSerializer<TestConfiguration> configurationSerializer = new ConfigurationSerializer<>();

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
        TestConfiguration configuration = ConfigurationMappingLoader.builder(TestConfiguration.class)
            .load(configSource);

        String yaml = toYaml(configuration);

        assertThat(yaml).contains("group-id: org.jqassistant.plugin")
            .contains("version: 1.0.0")
            .contains("uri: bolt://localhost:7687")
            .contains("directory: target/rules")
            .contains("fail-on-severity: BLOCKER");
    }

    @Test
    void serializeAndRestoreConfiguration() {
        TestConfiguration configuration = ConfigurationMappingLoader.builder(TestConfiguration.class)
            .withUserHome(USER_HOME)
            .withWorkingDirectory(WORKING_DIRECTORY)
            .load();

        String yaml = toYaml(configuration);

        YamlConfigSource yamlConfigSource = new YamlConfigSource("yaml", yaml);
        assertThat(yamlConfigSource.getValue("jqassistant.analyze.execute-applied-concepts")).isEqualTo("false");
        TestConfiguration restoredConfiguration = ConfigurationMappingLoader.builder(TestConfiguration.class)
            .load(yamlConfigSource);
        Scan scan = restoredConfiguration.scan();
        assertThat(scan.properties()).containsEntry("user-value", "default");
        assertThat(scan.properties()).containsEntry("overwritten-user-value", "overwritten");
    }

    private String toYaml(TestConfiguration configuration) {
        String yaml = configurationSerializer.toYaml(configuration);
        log.info("\n{}", yaml);
        return yaml;
    }

}
