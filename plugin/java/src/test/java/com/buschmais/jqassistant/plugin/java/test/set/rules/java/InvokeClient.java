package com.buschmais.jqassistant.plugin.java.test.set.rules.java;

/**
 * A client invoking implemented or overridden methods.
 */
public class InvokeClient {

    public void invokeInterfaceTypeMethod(InterfaceType interfaceType) {
        interfaceType.doSomething(0);
        interfaceType.doSomething(1);
    }

    public void invokeClassTypeMethod(ClassType classType) {
        classType.doSomething(0);
        classType.doSomething(1);
    }

}
