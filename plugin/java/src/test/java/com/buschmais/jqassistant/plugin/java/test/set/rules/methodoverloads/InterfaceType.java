package com.buschmais.jqassistant.plugin.java.test.set.rules.methodoverloads;

import com.buschmais.jqassistant.plugin.java.test.set.rules.virtualdependson.ClassType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.virtualdependson.SubClassType;

/**
 * An interface type.
 */
public interface InterfaceType {

    void doSomething(String value);

    void doSomething(int value);

    /**
     * Only declared in {@link InterfaceType} and {@link ClassType}, not overridden
     * by {@link SubClassType}.
     */
    void doSomething(boolean value);
}
