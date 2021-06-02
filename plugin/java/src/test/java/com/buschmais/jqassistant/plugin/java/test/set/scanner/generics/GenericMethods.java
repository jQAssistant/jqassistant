package com.buschmais.jqassistant.plugin.java.test.set.scanner.generics;

import java.io.IOException;
import java.util.List;

public class GenericMethods<X> {

    void genericParameter(X x, List<String> listOfString) {
    }

    X genericReturnType() {
        return null;
    }

    <E extends IOException> void genericException() throws E {

    }

    <X> void overwriteGenericDeclaration(X x) {
    }

}
