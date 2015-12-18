package com.buschmais.jqassistant.plugin.graphml.report.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;

public class VirtualPropertyContainer implements PropertyContainer {

    private final Map<String, Object> props = new LinkedHashMap<>();

    public VirtualPropertyContainer(Map<String, Object> m) {
        if (m.containsKey("properties")) {
            this.props.putAll((Map<String, Object>) m.get("properties"));
        }
    }

    @Override
    public GraphDatabaseService getGraphDatabase() {
        return null;
    }

    @Override
    public boolean hasProperty(String key) {
        return props.containsKey(key);
    }

    @Override
    public Object getProperty(String key) {
        return props.get(key);
    }

    @Override
    public Object getProperty(String key, Object defaultValue) {
        if (hasProperty(key)) {
            return getProperty(key);
        }
        return defaultValue;
    }

    @Override
    public void setProperty(String key, Object value) {
        props.put(key, value);
    }

    @Override
    public Object removeProperty(String key) {
        return props.remove(key);
    }

    @Override
    public Iterable<String> getPropertyKeys() {
        return props.keySet();
    }

    @Override
    public Map<String, Object> getProperties(String... keys) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : keys) {
            result.put(key, props.get(key));
        }

        return result;
    }

    @Override
    public Map<String, Object> getAllProperties() {
        return props;
    }

}
