package com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance;

public class SubClassType extends AbstractClassType {

    private int overriddenAbstractClassField;

    private int subClassField;

    @Override
    public void method() {
        super.method(); // must not create a VIRTUAL_INVOKES on this method
        abstractClassField = 0;
        overriddenAbstractClassField = 0;
        subClassField = 0;
    }

    @Override
    public final void subClassMethod() {
    }

}
