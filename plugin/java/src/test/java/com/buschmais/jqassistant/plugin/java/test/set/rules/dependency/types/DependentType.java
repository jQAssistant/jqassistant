package com.buschmais.jqassistant.plugin.java.test.set.rules.dependency.types;

@TypeAnnotation(TypeAnnotationValueType.class)
public class DependentType extends SuperClass<SuperClassTypeParameter> implements ImplementedInterface<ImplementedInterfaceTypeParameter> {

    @FieldAnnotation(FieldAnnotationValueType.class)
    private FieldType<FieldTypeParameter> field;

    @MethodAnnotation(MethodAnnotationValueType.class)
    public MethodReturnType<MethodReturnTypeParameter> iterator(MethodParameter<MethodParameterTypeParameter> n) throws MethodException {
        @LocalVariableAnnotation(LocalVariableAnnotationValueType.class)
        LocalVariable localVariable = new LocalVariable();
        LocalVariable.ReadStaticVariable readStaticVariable = LocalVariable.readStaticVariable;
        LocalVariable.ReadVariable readVariable = localVariable.readVariable;
        LocalVariable.writeStaticVariable = null;
        localVariable.writeVariable = null;
        InvokeMethodType invokeMethodType = new InvokeMethodType();
        try {
            invokeMethodType.invoke(null);
        } catch (InvokeMethodType.InvokeMethodException e) {
        }
        return null;
    }
}
