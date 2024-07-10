package com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance;

public class ClientType {

    private SubClassType subClassType = new SubClassType();

    private AbstractClassType abstractClassType = subClassType;

    private InterfaceType interfaceType = subClassType;

    public void methodOnInterfaceType() {
        interfaceType.method();
    }

    public void methodOnAbstractClassType() {
        abstractClassType.method();
    }

    public void methodOnSubClassType() {
        subClassType.method();
    }

    public void abstractClassMethodOnInterfaceType() {
        interfaceType.abstractClassMethod();
    }

    public void abstractClassMethodOnAbstractClassType() {
        abstractClassType.abstractClassMethod();
    }

    public void abstractClassMethodOnSubType() {
        subClassType.abstractClassMethod();
    }

    public void subClassMethodOnInterfaceType() {
        interfaceType.subClassMethod();
    }

    public void subClassMethodOnAbstractClassType() {
        abstractClassType.subClassMethod();
    }

    public void subClassMethodOnSubType() {
        subClassType.subClassMethod();
    }
}
