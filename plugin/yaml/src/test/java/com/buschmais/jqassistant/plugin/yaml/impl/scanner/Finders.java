package com.buschmais.jqassistant.plugin.yaml.impl.scanner;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLValueDescriptor;

import java.util.Collection;
import java.util.NoSuchElementException;

public class Finders {
    /**
     * Finds a value by its value. This method helps
     * to make the tests more stable as the order of
     * search result collection elements might vary.
     */
    static <T extends YAMLValueDescriptor> T findValueByValue(Collection<T> in, String value) {
        for (T element : in) {
            if (value.equals(element.getValue())) {
                return element;
            }
        }

        throw new NoSuchElementException("No entry with value '" + value + "' found.");
    }

    /**
     * Finds a descriptor by the name of the node. This method
     * helps to make the tests more stable as the order of the
     * search result collection elements might vary.
     */
    static <T extends NamedDescriptor> T findKeyByName(Collection<T> in, String name) {
        for (T element : in) {
            if (name.equals(element.getName())) {
                return element;
            }
        }

        throw new NoSuchElementException("No entry with name '" + name + "' found.");
    }

}
