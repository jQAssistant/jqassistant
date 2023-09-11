package com.buschmais.jqassistant.core.runtime.impl.configuration;

import java.util.Properties;

import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationLoader;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationSerializer;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;

import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.source.yaml.YamlConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.runtime.impl.configuration.ConfigurationLoaderImplTest.USER_HOME;
import static com.buschmais.jqassistant.core.runtime.impl.configuration.ConfigurationLoaderImplTest.WORKING_DIRECTORY;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link ConfigurationSerializerImpl}.
 */
@Slf4j
class ConfigurationSerializerImplTest {

    private final ConfigurationSerializer<TestConfiguration> configurationSerializer = new ConfigurationSerializerImpl<>();

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
        TestConfiguration configuration = new ConfigurationLoaderImpl<>(TestConfiguration.class).load(configSource);

        String yaml = toYaml(configuration);

        assertThat(yaml).contains("group-id: org.jqassistant.plugin");
        assertThat(yaml).contains("version: 1.0.0");
        assertThat(yaml).contains("uri: bolt://localhost:7687");
        assertThat(yaml).contains("directory: target/rules");
        assertThat(yaml).contains("fail-on-severity: BLOCKER");
    }

    @Test
    void serializeAndRestoreConfiguration() {
        ConfigurationLoader<TestConfiguration> configurationLoader = new ConfigurationLoaderImpl(TestConfiguration.class, USER_HOME, WORKING_DIRECTORY,
            emptyList());
        TestConfiguration configuration = configurationLoader.load();

        String yaml = toYaml(configuration);

        YamlConfigSource yamlConfigSource = new YamlConfigSource("yaml", yaml);
        assertThat(yamlConfigSource.getValue("jqassistant.analyze.execute-applied-concepts")).isEqualTo("false");
        TestConfiguration restoredConfiguration = new ConfigurationLoaderImpl<>(TestConfiguration.class).load(yamlConfigSource);
        Scan scan = restoredConfiguration.scan();
        assertThat(scan.properties()
            .get("user-value")).isEqualTo("default");
        assertThat(scan.properties()
            .get("overwritten-user-value")).isEqualTo("overwritten");
    }

    private String toYaml(TestConfiguration configuration) {
        String yaml = configurationSerializer.toYaml(configuration);
        log.info("\n{}", yaml);
        return yaml;
    }

}
