package com.buschmais.jqassistant.plugin.graphml.report.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

class GraphMLNamespaceContext implements NamespaceContext {

    private static final String XSI_NS_PREFIX = "xsi";

    private Map<String, String> namespaceByPrefix;

    private Map<String, String> prefixByNameSpace;

    private Map<String, String> schemaLocations;

    public GraphMLNamespaceContext(Map<String, String> additionalNamespaces, Map<String, String> schemaLocations) {
        this.namespaceByPrefix = createDefaultNamespaces();
        this.namespaceByPrefix.putAll(additionalNamespaces);
        this.schemaLocations = schemaLocations;
        this.prefixByNameSpace = new HashMap<>();
        for (Map.Entry<String, String> entry : this.namespaceByPrefix.entrySet()) {
            this.prefixByNameSpace.put(entry.getValue(), entry.getKey());
        }
    }

    private Map<String, String> createDefaultNamespaces() {
        LinkedHashMap<String, String> defaultNS = new LinkedHashMap<>();
        defaultNS.put(XSI_NS_PREFIX, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        return defaultNS;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return getOrDefault(namespaceByPrefix, prefix, XMLConstants.NULL_NS_URI);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return getOrDefault(prefixByNameSpace, namespaceURI, XMLConstants.DEFAULT_NS_PREFIX);
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        return Collections.singletonList(getPrefix(namespaceURI)).iterator();
    }

    /**
     * Return all registered namespaces and their prefixes.
     *
     * @return The registered namespaces.
     */
    Map<String, String> getNamespaces() {
        return namespaceByPrefix;
    }

    /**
     * Return namespaces and their schema locatons.
     *
     * @return The schema locations.
     */
    public Map<String, String> getSchemaLocations() {
        return schemaLocations;
    }

    private <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return defaultValue;
    }

}
