package com.buschmais.jqassistant.rules.java.test.set.dependency.methods;

import java.util.*;

/**
 * A class containing dependencies on field an method signature level.
 */
public class Dependency {

    public Set<?> set;

    public Map<?,?> getMap(SortedSet<Number> set) throws RuntimeException {
        return null;
    };
}
