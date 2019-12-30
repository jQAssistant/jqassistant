package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;

import static java.lang.String.format;

class AliasCache {
    private Map<String, YMLDescriptor> alias = new Hashtable<>();

    void addAlias(String aliasName, YMLDescriptor target) {
        alias.put(aliasName, target);
    }

    YMLDescriptor getTarget(String aliasName) {
        if (!alias.containsKey(aliasName)) {
            String message = format("No target for alias '%s' found", aliasName);
            throw new NoSuchElementException(message);
        }

        return alias.get(aliasName);
    }
}
