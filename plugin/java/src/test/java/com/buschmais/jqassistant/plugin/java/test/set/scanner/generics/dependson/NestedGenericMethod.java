package com.buschmais.jqassistant.plugin.java.test.set.scanner.generics.dependson;

public class NestedGenericMethod {

    <X, Y extends GenericType<X>> X get(Y value) {
        return null;
    }

}
