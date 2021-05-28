package com.buschmais.jqassistant.plugin.java.test.set.scanner.generics;

import java.util.List;

public class GenericMethod<X> {

    public <X extends List<String>> X getList(X x) {
        return null;
    }

}
