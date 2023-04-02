package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.util.Map;

import com.buschmais.jqassistant.core.shared.map.MapBuilder;

import io.smallrye.config.ConfigMapping;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigurationBuilder}.
 */
class ConfigurationBuilderTest {

    @ConfigMapping(prefix = "jqassistant.test")
    static class TestMapping {
    }

    private ConfigurationBuilder configurationBuilder = new ConfigurationBuilder("Test", 110);

    @Test
    void properties() {
        configurationBuilder.with(TestMapping.class, "boolean-value", true);
        // null values must not override existing configuration properties
        configurationBuilder.with(TestMapping.class, "boolean-value", (Boolean) null);
        Map<String, Object> map = MapBuilder.<String, Object>builder()
            .entry("key.1", "value1")
            .entry("key.2", "value2")
            .build();
        configurationBuilder.with(TestMapping.class, "map-value", map);
        configurationBuilder.with(TestMapping.class, "list-value", asList("element0", "element1"));

        ConfigSource propertiesConfigSource = configurationBuilder.build();

        assertThat(propertiesConfigSource.getValue("jqassistant.test.boolean-value")).isEqualTo("true");
        assertThat(propertiesConfigSource.getValue("jqassistant.test.map-value.\"key.1\"")).isEqualTo("value1");
        assertThat(propertiesConfigSource.getValue("jqassistant.test.map-value.\"key.2\"")).isEqualTo("value2");
        assertThat(propertiesConfigSource.getValue("jqassistant.test.list-value[0]")).isEqualTo("element0");
        assertThat(propertiesConfigSource.getValue("jqassistant.test.list-value[1]")).isEqualTo("element1");
    }

}
