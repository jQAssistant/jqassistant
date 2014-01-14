package com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.methodinvocations;

import java.util.TreeSet;

/**
 * A class containing a method with external dependencies to methods.
 */
public class MethodInvocation {

    public void methodInvocation() {
        MethodDependency methodDependency = new MethodDependency();
        methodDependency.getMap(new TreeSet<Number>());
    }
}
