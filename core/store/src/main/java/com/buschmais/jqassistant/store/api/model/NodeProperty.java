package com.buschmais.jqassistant.store.api.model;

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
     * Artifact type
     */
    TYPE;

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
