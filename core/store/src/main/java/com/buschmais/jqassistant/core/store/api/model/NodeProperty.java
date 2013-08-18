package com.buschmais.jqassistant.core.store.api.model;

import java.util.HashMap;
import java.util.Map;

/**
 * The supported node properties.
 */
public enum NodeProperty {

    /**
     * Full Qualified Name
     */
    FQN,

    /**
     * Artifact group
     */
    GROUP,

    /**
     * Artifact name
     */
    NAME,

    /**
     * Artifact version
     */
    VERSION,

    /**
     * Artifact classifier
     */
    CLASSIFIER,

    /**
     * Artifact types
     */
    TYPE,

    /**
     * <code>true</code> if the class or method is abstract, otherwise
     * <code>false</code>.
     */
    ABSTRACT,

    /**
     * Access modifier of a class/method/field.
     */
    VISIBILITY,

    /**
     * <code>true</code> if the class, method or field is static, otherwise
     * <code>false</code>.
     */
    STATIC,

    /**
     * <code>true</code> if the class, method or field is final, otherwise
     * <code>false</code>.
     */
    FINAL,

    /**
     * <code>true</code> if the class, field or method is synthetic, otherwise <code>false</code>.
     */
    SYNTHETIC,

    /**
     * <code>true</code> if the method is native, otherwise <code>false</code>.
     */
    NATIVE,

    /**
     * <code>true</code> if the field is transient, otherwise <code>false</code>
     * .
     */
    TRANSIENT,

    /**
     * <code>true</code> if the field is volatile, otherwise <code>false</code>.
     */
    VOLATILE;

    private static Map<String, NodeProperty> nodeProperties;

    static {
        nodeProperties = new HashMap<String, NodeProperty>();
        for (NodeProperty property : NodeProperty.values()) {
            nodeProperties.put(property.name(), property);
        }
    }

    public static NodeProperty getProperty(String name) {
        return nodeProperties.get(name);
    }
}
