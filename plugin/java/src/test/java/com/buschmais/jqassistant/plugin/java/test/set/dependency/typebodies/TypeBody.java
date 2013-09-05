package com.buschmais.jqassistant.plugin.java.test.set.dependency.typebodies;

import java.util.Iterator;
import java.util.List;

/**
 * A class containing dependencies on field and method level
 */
public class TypeBody {

    @FieldAnnotation
    private List<String> values;

    @MethodAnnotation
    public Iterator<Integer> iterator(Number n) throws Exception {
        Double doubleValue = Double.valueOf(0d);
        Boolean.valueOf(true);
        return null;
    }
}
