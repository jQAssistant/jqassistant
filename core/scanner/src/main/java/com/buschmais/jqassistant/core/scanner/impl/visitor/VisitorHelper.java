package com.buschmais.jqassistant.core.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.*;
import com.buschmais.jqassistant.core.scanner.impl.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.core.store.api.Store;
import org.objectweb.asm.Type;

/**
 * Abstract implementation of an ASM visitor.
 */
public class VisitorHelper {

    private DescriptorResolverFactory resolverFactory;

    private Store store;

    public VisitorHelper(Store store, DescriptorResolverFactory resolverFactory) {
        this.store = store;
        this.resolverFactory = resolverFactory;
    }

    protected Store getStore() {
        return store;
    }

    protected TypeDescriptor getTypeDescriptor(String typeName) {
        String fullQualifiedName = getType(Type.getObjectType(typeName));
        return resolverFactory.getTypeDescriptorResolver().resolve(fullQualifiedName);
    }

    protected MethodDescriptor getMethodDescriptor(TypeDescriptor type, String signature) {
        return resolverFactory.getMethodDescriptorResolver().resolve(type, signature);
    }

    protected FieldDescriptor getFieldDescriptor(TypeDescriptor type, String signature) {
        return resolverFactory.getFieldDescriptorResolver().resolve(type, signature);
    }

    protected void addAnnotation(AnnotatedDescriptor annotatedDescriptor, String typeName) {
        if (typeName != null) {
            TypeDescriptor dependency = getTypeDescriptor(typeName);
            annotatedDescriptor.getAnnotatedBy().add(dependency);
        }
    }

    protected void addDependency(DependentDescriptor dependentDescriptor, String typeName) {
        if (typeName != null) {
            TypeDescriptor dependency = getTypeDescriptor(typeName);
            dependentDescriptor.getDependencies().add(dependency);
        }
    }

    protected String getType(final String desc) {
        return getType(Type.getType(desc));
    }

    protected String getType(final Type t) {
        switch (t.getSort()) {
            case Type.ARRAY:
                return getType(t.getElementType());
            default:
                return t.getClassName();
        }
    }
}