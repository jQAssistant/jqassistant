package com.buschmais.jqassistant.plugin.java8.test.set.rules;

/**
 * An interface defining a default method.
 */
public interface DefaultMethod {

    default int add(int a, int b) {
        return a + b;
    }

    int sub(int a, int b);
}
