package com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance;

public class SubClassType extends AbstractClassType {

    @Override
    public void method() {
        super.method(); // must not create a VIRTUAL_INVOKES on this method
    }

    @Override
    public final void subClassMethod() {
    }

}
