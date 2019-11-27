package com.buschmais.jqassistant.plugin.java.test.set.rules.java;

/**
 * A client invoking implemented or overridden methods.
 */
public class InterfaceTypeClient {

    public void invokeInterfaceTypeMethod(InterfaceType interfaceType) {
        interfaceType.doSomething(0);
        interfaceType.doSomething(1);
    }

}
