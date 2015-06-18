package com.buschmais.jqassistant.plugin.yaml.impl.scanner.util;

import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLKeyDescriptor;

import java.util.Collection;

/**
 * <p>This class provides helper methods to find elements based
 *    on various properties in a given collection.</p>
 *
 * <p>This class is only needed for Java 7. With Java 8 as
 *    code basis the Stream API could be used. Actually
 *    this class is only needed as Neo4J returns sometimes
 *    lists in a different ordering therefor accessing them
 *    by index might lead to a failing build because
 *    of test failures.</p>
 */
public class Finder {

    public YAMLKeyDescriptor findKeyByName(Collection<? extends YAMLKeyDescriptor> keys, String name) {
        YAMLKeyDescriptor result = null;

        for (YAMLKeyDescriptor key : keys) {
            if (key.getName().equals(name)) {
                result = key;
            }
        }

        return result;
    }
}
