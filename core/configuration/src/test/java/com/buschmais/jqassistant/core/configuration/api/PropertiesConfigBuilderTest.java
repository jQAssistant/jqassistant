package com.buschmais.jqassistant.core.configuration.api;

import java.util.Map;

import com.buschmais.jqassistant.core.shared.map.MapBuilder;

import io.smallrye.config.PropertiesConfigSource;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PropertiesConfigBuilder}.
 */
class PropertiesConfigBuilderTest {

    public static final String PREFIX = "jqassistant.test";
    private PropertiesConfigBuilder propertiesConfigBuilder = new PropertiesConfigBuilder("Test", 110);

    @Test
    void properties() {
        propertiesConfigBuilder.with(PREFIX, "boolean-value", true);
        Map<String, Object> map = MapBuilder.<String, Object>builder()
            .entry("key.1", "value1")
            .entry("key.2", "value2")
            .build();
        propertiesConfigBuilder.with(PREFIX, "map-value", map);
        propertiesConfigBuilder.with(PREFIX, "list-value", asList("element0", "element1"));

        PropertiesConfigSource propertiesConfigSource = propertiesConfigBuilder.build();

        assertThat(propertiesConfigSource.getValue("jqassistant.test.boolean-value")).isEqualTo("true");
        assertThat(propertiesConfigSource.getValue("jqassistant.test.map-value.\"key.1\"")).isEqualTo("value1");
        assertThat(propertiesConfigSource.getValue("jqassistant.test.map-value.\"key.2\"")).isEqualTo("value2");
        assertThat(propertiesConfigSource.getValue("jqassistant.test.list-value[0]")).isEqualTo("element0");
        assertThat(propertiesConfigSource.getValue("jqassistant.test.list-value[1]")).isEqualTo("element1");
    }

}
