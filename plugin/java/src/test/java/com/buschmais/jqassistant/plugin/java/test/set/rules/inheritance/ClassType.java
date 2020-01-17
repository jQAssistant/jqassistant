package com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance;

/**
 * A class type.
 */
public class ClassType implements InterfaceType {

    @Override
    public void doSomething(String value) {
    }

    @Override
    public void doSomething(int value) {
    }

    @SuppressWarnings("unused")
    private void doSomething() {
    }
}
