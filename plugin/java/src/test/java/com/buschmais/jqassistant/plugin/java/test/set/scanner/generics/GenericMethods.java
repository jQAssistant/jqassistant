package com.buschmais.jqassistant.plugin.java.test.set.scanner.generics;

import java.io.IOException;

public class GenericMethods<X> {

    void genericParameter(X x) {
    }

    X genericReturnType() {
        return null;
    }

    <E extends IOException> void genericException() throws E {
    }

    <X> X overwriteTypeParameter() {
        return null;
    }

    void genericVariable() {
        X x = null;
    }

}
