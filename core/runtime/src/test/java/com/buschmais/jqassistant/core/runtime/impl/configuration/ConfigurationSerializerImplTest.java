package com.buschmais.jqassistant.core.runtime.impl.configuration;

import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationLoader;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationSerializer;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;

import io.smallrye.config.source.yaml.YamlConfigSource;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.runtime.impl.configuration.ConfigurationLoaderImplTest.USER_HOME;
import static com.buschmais.jqassistant.core.runtime.impl.configuration.ConfigurationLoaderImplTest.WORKING_DIRECTORY;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link ConfigurationSerializerImpl}.
 */
class ConfigurationSerializerImplTest {

    private final ConfigurationSerializer<TestConfiguration> configurationSerializer = new ConfigurationSerializerImpl<>();

    @Test
    void serializeAndRestoreConfiguration() {
        ConfigurationLoader<TestConfiguration> configurationLoader = new ConfigurationLoaderImpl(TestConfiguration.class, USER_HOME, WORKING_DIRECTORY,
            emptyList());
        TestConfiguration configuration = configurationLoader.load();

        String yaml = configurationSerializer.toYaml(configuration);
        System.out.println(yaml);

        YamlConfigSource yamlConfigSource = new YamlConfigSource("yaml", yaml);
        assertThat(yamlConfigSource.getValue("jqassistant.analyze.execute-applied-concepts")).isEqualTo("false");
        TestConfiguration restoredConfiguration = new ConfigurationLoaderImpl<>(TestConfiguration.class).load(yamlConfigSource);
        Scan scan = restoredConfiguration.scan();
        assertThat(scan.properties()
            .get("user-value")).isEqualTo("default");
        assertThat(scan.properties()
            .get("overwritten-user-value")).isEqualTo("overwritten");
    }
}
