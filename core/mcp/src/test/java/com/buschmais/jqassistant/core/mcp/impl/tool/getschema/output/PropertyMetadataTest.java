package com.buschmais.jqassistant.core.mcp.impl.tool.getschema.output;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static com.buschmais.jqassistant.core.mcp.impl.tool.getschema.output.PropertyMetadata.Type.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the creation of {@link PropertyMetadata} from Java types.
 */
class PropertyMetadataTest {

    @Test
    void singleValue() {
        assertThat(PropertyMetadata.of("name", String.class)).hasValueSatisfying(propertyMetadata -> {
            assertThat(propertyMetadata.getName()).isEqualTo("name");
            assertThat(propertyMetadata.getType()).isEqualTo(STRING);
            assertThat(propertyMetadata.getArray()).isNull();
        });
    }

    @ParameterizedTest
    @MethodSource("arrayTypes")
    void array(Class<?> javaType, PropertyMetadata.Type expectedType) {
        assertThat(PropertyMetadata.of("values", javaType)).hasValueSatisfying(propertyMetadata -> {
            assertThat(propertyMetadata.getName()).isEqualTo("values");
            assertThat(propertyMetadata.getType()).isEqualTo(expectedType);
            assertThat(propertyMetadata.getArray()).isTrue();
        });
    }

    static Stream<Arguments> arrayTypes() {
        return Stream.of( //
            Arguments.of(String[].class, STRING), //
            Arguments.of(char[].class, STRING), //
            Arguments.of(int[].class, INTEGER), //
            Arguments.of(Long[].class, INTEGER), //
            Arguments.of(double[].class, FLOAT), //
            Arguments.of(LocalDate[].class, DATETIME), //
            Arguments.of(boolean[].class, BOOLEAN));
    }

    @ParameterizedTest
    @ValueSource(classes = { Object.class, Object[].class, String[][].class, int[][].class, List.class })
    void unsupportedType(Class<?> javaType) {
        assertThat(PropertyMetadata.of("value", javaType)).isEmpty();
    }

    @Test
    void arrayFlagIsOmittedInJsonForSingleValues() {
        PropertyMetadata propertyMetadata = PropertyMetadata.of("name", String.class)
            .orElseThrow();

        assertThat(toJson(propertyMetadata)).isEqualTo(JsonMapper.shared()
            .readTree("{\"name\":\"name\",\"type\":\"STRING\"}"));
    }

    @Test
    void arrayFlagIsIncludedInJsonForArrays() {
        PropertyMetadata propertyMetadata = PropertyMetadata.of("names", String[].class)
            .orElseThrow();

        assertThat(toJson(propertyMetadata)).isEqualTo(JsonMapper.shared()
            .readTree("{\"name\":\"names\",\"type\":\"STRING\",\"array\":true}"));
    }

    private static JsonNode toJson(PropertyMetadata propertyMetadata) {
        JsonMapper jsonMapper = JsonMapper.shared();
        return jsonMapper.readTree(jsonMapper.writeValueAsString(propertyMetadata));
    }
}
