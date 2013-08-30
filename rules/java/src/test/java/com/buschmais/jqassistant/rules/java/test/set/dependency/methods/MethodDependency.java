package com.buschmais.jqassistant.rules.java.test.set.dependency.methods;

import java.util.TreeSet;

/**
 * A class containing a method with external dependencies to fields and methods.
 */
public class MethodDependency {

    public void externalDependencies() {
        Dependency dependency = new Dependency();
        if (dependency.set != null) {
            dependency.set = null;
        }
        dependency.getMap(new TreeSet<Number>());
    }
}
