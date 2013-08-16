package com.buschmais.jqassistant.core.scanner.test.set.generics;

public class NestedGenericMethod {

    <X, Y extends GenericType<X>> X get(Y value) {
        return null;
    }

}
