package com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance;

public abstract class AbstractClassType implements InterfaceType {

    protected int abstractClassField;

    protected int overriddenAbstractClassField;

    @Override
    public void method() {
        abstractClassField = 0;
        overriddenAbstractClassField = 0;
    }

    @Override
    public final void abstractClassMethod() {
    }
}
