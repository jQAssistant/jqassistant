package com.buschmais.jqassistant.core.model.api.descriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * The supported java types.
 */
public enum JavaType {
    /**
     * Class
     */
    CLASS,
    /**
     * Interface
     */
    INTERFACE,
    /**
     * Enumeration
     */
    ENUM,
    /**
     * Annotation
     */
    ANNOTATION;

    private static Map<String, JavaType> javaTypes;

    static {
        javaTypes = new HashMap<String, JavaType>();
        for (JavaType javaType : JavaType.values()) {
            javaTypes.put(javaType.name(), javaType);
        }
    }

    public static JavaType getJavaType(String name) {
        return javaTypes.get(name);
    }
}
