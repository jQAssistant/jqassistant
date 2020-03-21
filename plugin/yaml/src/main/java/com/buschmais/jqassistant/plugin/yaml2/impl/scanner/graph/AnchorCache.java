package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import java.util.Hashtable;
import java.util.Map;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;

import static java.lang.String.format;

class AnchorCache {
    private Map<String, YMLDescriptor> alias = new Hashtable<>();

    void addAlias(String aliasName, YMLDescriptor target) {
        alias.put(aliasName, target);
    }

    YMLDescriptor getTarget(String aliasName) {
        if (!alias.containsKey(aliasName)) {
            String message = format("No anchor for alias '%s' found", aliasName);
            throw new GraphGenerationFailedException(message);
        }

        return alias.get(aliasName);
    }
}
