package com.buschmais.jqassistant.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.AnnotatedDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.DependentDescriptor;
import com.buschmais.jqassistant.scanner.impl.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.store.api.Store;
import org.objectweb.asm.Type;

/**
 * Abstract implementation of an ASM visitor.
 */
public abstract class AbstractVisitor {

    private final DescriptorResolverFactory resolverFactory;

    protected AbstractVisitor(DescriptorResolverFactory resolverFactory) {
        this.resolverFactory = resolverFactory;
    }

    protected DescriptorResolverFactory getResolverFactory() {
        return resolverFactory;
    }

    protected Store getStore() {
        return resolverFactory.getStore();
    }

    protected TypeDescriptor getTypeDescriptor(String typeName) {
        String fullQualifiedName = getType(Type.getObjectType(typeName));
        return resolverFactory.getTypeDescriptorResolver().resolve(fullQualifiedName);
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
