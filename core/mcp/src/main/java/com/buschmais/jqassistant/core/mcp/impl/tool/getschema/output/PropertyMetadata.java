package com.buschmais.jqassistant.core.mcp.impl.tool.getschema.output;

import java.time.*;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class PropertyMetadata {

    private String name;

    private Type type;

    /**
     * Set to <code>true</code> if the property is an array of values of {@link #type}, omitted otherwise.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean array;

    /**
     * Creates the {@link PropertyMetadata} for a property of the given Java type.
     * <p>
     * One-dimensional arrays are supported if their component type is supported.
     *
     * @param name
     *     The name of the property.
     * @param javaType
     *     The Java type of the property.
     * @return The {@link PropertyMetadata} or {@link Optional#empty()} if the Java type is not supported.
     */
    public static Optional<PropertyMetadata> of(String name, Class<?> javaType) {
        boolean isArray = javaType.isArray();
        Class<?> valueType = isArray ? javaType.getComponentType() : javaType;
        return Type.of(valueType)
            .map(type -> PropertyMetadata.builder()
                .name(name)
                .type(type)
                .array(isArray ? true : null)
                .build());
    }

    public enum Type {
        STRING,
        INTEGER,
        FLOAT,
        DATETIME,
        BOOLEAN;

        private static final Set<Class<?>> STRING_TYPES = Set.of(String.class, char.class, Character.class);

        private static final Set<Class<?>> INTEGER_TYPES = Set.of(byte.class, Byte.class, short.class, Short.class, int.class, Integer.class, long.class,
            Long.class);

        private static final Set<Class<?>> FLOAT_TYPES = Set.of(float.class, Float.class, double.class, Double.class, Number.class);

        /**
         * The temporal types supported natively by Neo4j.
         */
        private static final Set<Class<?>> DATETIME_TYPES = Set.of(LocalDate.class, LocalTime.class, LocalDateTime.class, OffsetTime.class,
            OffsetDateTime.class, ZonedDateTime.class);

        private static final Set<Class<?>> BOOLEAN_TYPES = Set.of(boolean.class, Boolean.class);

        public static Optional<Type> of(Class<?> type) {
            if (STRING_TYPES.contains(type) || Enum.class.isAssignableFrom(type)) {
                return Optional.of(STRING);
            } else if (INTEGER_TYPES.contains(type)) {
                return Optional.of(INTEGER);
            } else if (FLOAT_TYPES.contains(type)) {
                return Optional.of(FLOAT);
            } else if (DATETIME_TYPES.contains(type)) {
                return Optional.of(DATETIME);
            } else if (BOOLEAN_TYPES.contains(type)) {
                return Optional.of(BOOLEAN);
            }
            return Optional.empty();
        }
    }

}
