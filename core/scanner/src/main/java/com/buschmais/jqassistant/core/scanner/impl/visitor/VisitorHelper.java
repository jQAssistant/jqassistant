package com.buschmais.jqassistant.core.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.*;
import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;
import com.buschmais.jqassistant.core.scanner.impl.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.core.store.api.Store;
import org.objectweb.asm.Type;

/**
 * Class containing helper methods for ASM visitors.
 */
public class VisitorHelper {

    private DescriptorResolverFactory resolverFactory;

    private Store store;

    /**
     * Constructor.
     *
     * @param store           The store.
     * @param resolverFactory The resolver factory used for looking up descriptors.
     */
    public VisitorHelper(Store store, DescriptorResolverFactory resolverFactory) {
        this.store = store;
        this.resolverFactory = resolverFactory;
    }

    /*
     * Return the type descriptor for the given type name.
     * @param typeName The full qualified name of the type (e.g. java.lang.Object).
     */
    TypeDescriptor getTypeDescriptor(String typeName) {
        String fullQualifiedName = getType(Type.getObjectType(typeName));
        return resolverFactory.getTypeDescriptorResolver().resolve(fullQualifiedName);
    }

    /**
     * Return the method descriptor for the given type and method signature.
     *
     * @param type      The containing type.
     * @param signature The method signature.
     * @return The method descriptor.
     */
    MethodDescriptor getMethodDescriptor(TypeDescriptor type, String signature) {
        return resolverFactory.getMethodDescriptorResolver().resolve(type, signature);
    }


    /**
     * Return the field descriptor for the given type and field signature.
     *
     * @param type      The containing type.
     * @param signature The field signature.
     * @return The field descriptor.
     */
    FieldDescriptor getFieldDescriptor(TypeDescriptor type, String signature) {
        return resolverFactory.getFieldDescriptorResolver().resolve(type, signature);
    }

    <T extends ValueDescriptor> T getValueDescriptor(Class<T> type, String name) {
        return store.create(type, name);
    }

    /**
     * Add an annotation descriptor of the given type name to an annotated descriptor.
     *
     * @param annotatedDescriptor The annotated descriptor.
     * @param typeName            The type name of the annotation.
     * @return The annotation descriptor.
     */
    AnnotationValueDescriptor addAnnotation(AnnotatedDescriptor annotatedDescriptor, String typeName) {
        if (typeName != null) {
            TypeDescriptor type = getTypeDescriptor(typeName);
            AnnotationValueDescriptor annotationDescriptor = store.create(AnnotationValueDescriptor.class, annotatedDescriptor.getFullQualifiedName() + "@" + typeName);
            annotationDescriptor.setType(type);
            annotatedDescriptor.getAnnotatedBy().add(annotationDescriptor);
            return annotationDescriptor;
        }
        return null;
    }

    /**
     * Return the parameter descriptor for the given methodDescriptor and parameter index.
     *
     * @param methodDescriptor The declaring methodDescriptor.
     * @param index  The parameter index.
     * @return The parameter descriptor.
     */
    ParameterDescriptor getParameterDescriptor(MethodDescriptor methodDescriptor, int index) {
        String fullQualifiedName = methodDescriptor.getFullQualifiedName() + "(" + index + ")";
        ParameterDescriptor parameterDescriptor = store.find(ParameterDescriptor.class, fullQualifiedName);
        if (parameterDescriptor == null) {
            parameterDescriptor = store.create(ParameterDescriptor.class, fullQualifiedName);
            methodDescriptor.getParameters().add(parameterDescriptor);
        }
        return parameterDescriptor;
    }

    /**
     * Adds a dependency to the given type name to a dependent descriptor.
     *
     * @param dependentDescriptor The dependent descriptor.
     * @param typeName            The type name of the dependency.
     */
    void addDependency(DependentDescriptor dependentDescriptor, String typeName) {
        if (typeName != null) {
            TypeDescriptor dependency = getTypeDescriptor(typeName);
            dependentDescriptor.getDependencies().add(dependency);
        }
    }

    /**
     * Return the type name for the given native name (as provided by ASM).
     *
     * @param desc The native name.
     * @return The type name.
     */
    String getType(final String desc) {
        return getType(Type.getType(desc));
    }

    /**
     * Return the type name of the given ASM type.
     *
     * @param t The ASM type.
     * @return The type name.
     */
    String getType(final Type t) {
        switch (t.getSort()) {
            case Type.ARRAY:
                return getType(t.getElementType());
            default:
                return t.getClassName();
        }
    }

    /**
     * Return a method signature.
     *
     * @param name The method name.
     * @param desc The signature containing parameter, return and exception values.
     * @return The method signature.
     */
    String getMethodSignature(String name, String desc) {
        StringBuffer signature = new StringBuffer();
        String returnType = org.objectweb.asm.Type.getReturnType(desc).getClassName();
        if (returnType != null) {
            signature.append(returnType);
            signature.append(' ');
        }
        signature.append(name);
        signature.append('(');
        org.objectweb.asm.Type[] types = org.objectweb.asm.Type.getArgumentTypes(desc);
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                signature.append(',');
            }
            signature.append(types[i].getClassName());
        }
        signature.append(')');
        return signature.toString();
    }

    /**
     * Return a field signature.
     *
     * @param name The field name.
     * @param desc The signature containing the type value.
     * @return The field signature.
     */
    String getFieldSignature(String name, String desc) {
        StringBuffer signature = new StringBuffer();
        String returnType = org.objectweb.asm.Type.getReturnType(desc).getClassName();
        signature.append(returnType);
        signature.append(' ');
        signature.append(name);
        return signature.toString();
    }
}