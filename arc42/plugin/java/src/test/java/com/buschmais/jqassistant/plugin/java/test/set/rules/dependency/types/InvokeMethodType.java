package com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types;

public class InvokeMethodType {

    public InvokeMethodReturnType<InvokeMethodReturnTypeParameter> invoke(InvokeMethodParameterType<InvokeMethodParameterTypeTypeParameter> parameter)
            throws InvokeMethodException {
        return null;
    }

    public class InvokeMethodReturnType<T> {
    }

    public class InvokeMethodReturnTypeParameter {
    }

    public class InvokeMethodParameterType<T> {
    }

    public class InvokeMethodParameterTypeTypeParameter<T> {
    }

    public class InvokeMethodException extends Exception {

    }
}
