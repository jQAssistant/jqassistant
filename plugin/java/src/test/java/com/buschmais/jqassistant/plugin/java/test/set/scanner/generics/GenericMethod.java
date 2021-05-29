package com.buschmais.jqassistant.plugin.java.test.set.scanner.generics;

import java.util.List;

public class GenericMethod<X, Y> {

    public <X extends List<String>> X getList(X x, Y y) {
        return null;
    }

}
