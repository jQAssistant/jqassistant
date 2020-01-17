package com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance;

/**
 * A client invoking implemented or overridden methods.
 */
public class ClassTypeClient {

    public void invokeClassTypeMethod(ClassType classType) {
        classType.doSomething(0);
        classType.doSomething(1);
    }

}
