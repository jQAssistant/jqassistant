package com.buschmais.jqassistant.core.mcp.impl.tool.getschema.output;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.buschmais.jqassistant.core.mcp.impl.tool.getschema.output.PropertyMetadata.Type.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the mapping of Java types to {@link PropertyMetadata.Type}s.
 */
class PropertyMetadataTypeTest {

    private enum Visibility {
        PUBLIC
    }

    @ParameterizedTest
    @MethodSource("supportedTypes")
    void supportedType(Class<?> javaType, PropertyMetadata.Type expectedType) {
        assertThat(PropertyMetadata.Type.of(javaType)).contains(expectedType);
    }

    static Stream<Arguments> supportedTypes() {
        return Stream.of( //
            Arguments.of(String.class, STRING), //
            Arguments.of(char.class, STRING), //
            Arguments.of(Character.class, STRING), //
            Arguments.of(Visibility.class, STRING), //
            Arguments.of(byte.class, INTEGER), //
            Arguments.of(Byte.class, INTEGER), //
            Arguments.of(short.class, INTEGER), //
            Arguments.of(Short.class, INTEGER), //
            Arguments.of(int.class, INTEGER), //
            Arguments.of(Integer.class, INTEGER), //
            Arguments.of(long.class, INTEGER), //
            Arguments.of(Long.class, INTEGER), //
            Arguments.of(float.class, FLOAT), //
            Arguments.of(Float.class, FLOAT), //
            Arguments.of(double.class, FLOAT), //
            Arguments.of(Double.class, FLOAT), //
            Arguments.of(Number.class, FLOAT), //
            Arguments.of(LocalDate.class, DATETIME), //
            Arguments.of(LocalTime.class, DATETIME), //
            Arguments.of(LocalDateTime.class, DATETIME), //
            Arguments.of(OffsetTime.class, DATETIME), //
            Arguments.of(OffsetDateTime.class, DATETIME), //
            Arguments.of(ZonedDateTime.class, DATETIME), //
            Arguments.of(boolean.class, BOOLEAN), //
            Arguments.of(Boolean.class, BOOLEAN));
    }

    @ParameterizedTest
    @ValueSource(classes = { Object.class, BigInteger.class, BigDecimal.class, Instant.class, Duration.class, Period.class, Date.class, String[].class,
        int[].class, List.class, Optional.class })
    void unsupportedType(Class<?> javaType) {
        assertThat(PropertyMetadata.Type.of(javaType)).isEmpty();
    }
}
