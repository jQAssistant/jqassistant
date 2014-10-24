package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.jqassistant.plugin.java.impl.scanner.resolver.DescriptorResolverFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Class containing helper methods for ASM visitors.
 */
public class VisitorHelper {

    /**
     * The name of constructor methods.
     */
    private static final String CONSTRUCTOR_METHOD = "void <init>";

    private DescriptorResolverFactory resolverFactory;
    private ScannerContext scannerContext;

    /**
     * Represents a type and all of its declared members.
     * 
     * @param <T>
     *            The descriptor type.
     */
    public static class CachedType<T extends TypeDescriptor> {
        private T typeDescriptor;
        private Map<String, MethodDescriptor> methods = new HashMap<>();
        private Map<String, FieldDescriptor> fields = new HashMap<>();

        /**
         * Constructor.
         * 
         * @param typeDescriptor
         *            The type descriptor.
         */
        public CachedType(T typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
        }

        public T getTypeDescriptor() {
            return typeDescriptor;
        }

        public void migrate(T typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
        }

        FieldDescriptor getField(String signature) {
            return fields.get(signature);
        }

        void addField(String signature, FieldDescriptor field) {
            fields.put(signature, field);
        }

        MethodDescriptor getMethod(String signature) {
            return methods.get(signature);
        }

        void addMethod(String signature, MethodDescriptor method) {
            methods.put(signature, method);
        }

    }

    private Cache<String, CachedType> typeCache;

    /**
     * Constructor.
     * 
     * @param scannerContext
     *            The scanner context
     * @param resolverFactory
     *            The resolver factory used for looking up descriptors.
     */
    public VisitorHelper(ScannerContext scannerContext, DescriptorResolverFactory resolverFactory) {
        this.typeCache = CacheBuilder.newBuilder().softValues().build();
        this.scannerContext = scannerContext;
        this.resolverFactory = resolverFactory;
    }

    /*
     * Return the type descriptor for the given type name.
     * 
     * @param typeName The full qualified name of the type (e.g.
     * java.lang.Object).
     */
    CachedType getType(String fullQualifiedName) {
        return getType(fullQualifiedName, TypeDescriptor.class);
    }

    /*
     * Return the type descriptor for the given type name.
     * 
     * @param typeName The full qualified name of the type (e.g.
     * java.lang.Object).
     * 
     * @param type The expected type.
     */
    <T extends TypeDescriptor> CachedType getType(String fullQualifiedName, Class<T> expectedType) {
        CachedType cachedType = typeCache.getIfPresent(fullQualifiedName);
        TypeDescriptor typeDescriptor;
        if (cachedType != null && !expectedType.isAssignableFrom(cachedType.getTypeDescriptor().getClass())) {
            typeDescriptor = scannerContext.getStore().migrate(cachedType.getTypeDescriptor(), expectedType);
            cachedType.migrate(typeDescriptor);
        } else if (cachedType == null) {
            typeDescriptor = resolverFactory.getTypeDescriptorResolver().resolve(fullQualifiedName, expectedType, scannerContext);
            cachedType = new CachedType(typeDescriptor);
            for (Descriptor descriptor : typeDescriptor.getDeclaredFields()) {
                if (descriptor instanceof FieldDescriptor) {
                    FieldDescriptor fieldDescriptor = (FieldDescriptor) descriptor;
                    cachedType.addField(fieldDescriptor.getSignature(), fieldDescriptor);
                }
            }
            for (Descriptor descriptor : typeDescriptor.getDeclaredMethods()) {
                if (descriptor instanceof MethodDescriptor) {
                    MethodDescriptor methodDescriptor = (MethodDescriptor) descriptor;
                    cachedType.addMethod(methodDescriptor.getSignature(), methodDescriptor);
                }
            }
            typeCache.put(fullQualifiedName, cachedType);
        }
        return cachedType;
    }

    /**
     * Return the method descriptor for the given type and method signature.
     * 
     * @param cachedType
     *            The containing type.
     * @param signature
     *            The method signature.
     * @return The method descriptor.
     */
    MethodDescriptor getMethodDescriptor(CachedType<?> cachedType, String signature) {
        MethodDescriptor methodDescriptor = cachedType.getMethod(signature);
        if (methodDescriptor == null) {
            if (signature.startsWith(CONSTRUCTOR_METHOD)) {
                methodDescriptor = scannerContext.getStore().create(ConstructorDescriptor.class);
            } else {
                methodDescriptor = scannerContext.getStore().create(MethodDescriptor.class);
            }
            methodDescriptor.setSignature(signature);
            cachedType.getTypeDescriptor().getDeclaredMethods().add(methodDescriptor);
            cachedType.addMethod(signature, methodDescriptor);
        }
        return methodDescriptor;
    }

    /**
     * Return the field descriptor for the given type and field signature.
     * 
     * @param cachedType
     *            The containing type.
     * @param signature
     *            The field signature.
     * @return The field descriptor.
     */
    FieldDescriptor getFieldDescriptor(CachedType<?> cachedType, String signature) {
        FieldDescriptor fieldDescriptor = cachedType.getField(signature);
        if (fieldDescriptor == null) {
            fieldDescriptor = scannerContext.getStore().create(FieldDescriptor.class);
            fieldDescriptor.setSignature(signature);
            cachedType.getTypeDescriptor().getDeclaredFields().add(fieldDescriptor);
            cachedType.addField(signature, fieldDescriptor);
        }
        return fieldDescriptor;
    }

    /**
     * Creates a {@link ValueDescriptor}.
     * 
     * @param valueDescriptorType
     *            The type of the value descriptor.
     * @param <T>
     *            The type of the value descriptor.
     * @return The value descriptor.
     */
    <T extends ValueDescriptor<?>> T getValueDescriptor(Class<T> valueDescriptorType) {
        return scannerContext.getStore().create(valueDescriptorType);
    }

    /**
     * Add an annotation descriptor of the given type name to an annotated
     * descriptor.
     * 
     * @param annotatedDescriptor
     *            The annotated descriptor.
     * @param typeName
     *            The type name of the annotation.
     * @return The annotation descriptor.
     */
    AnnotationValueDescriptor addAnnotation(AnnotatedDescriptor annotatedDescriptor, String typeName) {
        if (annotatedDescriptor == null) {
            throw new RuntimeException();
        }
        if (typeName != null) {
            TypeDescriptor type = getType(typeName).getTypeDescriptor();
            AnnotationValueDescriptor annotationDescriptor = scannerContext.getStore().create(AnnotationValueDescriptor.class);
            annotationDescriptor.setType(type);
            annotatedDescriptor.addAnnotatedBy(annotationDescriptor);
            return annotationDescriptor;
        }
        return null;
    }

    /**
     * Adds a dependency to the given type name to a dependent descriptor.
     * 
     * @param containingTypeDescriptor
     *            The type containing the dependency.
     * @param dependentDescriptor
     *            The dependent descriptor.
     * @param dependencyTypeName
     *            The type name of the dependency.
     */
    void addDependency(TypeDescriptor containingTypeDescriptor, DependentDescriptor dependentDescriptor, String dependencyTypeName) {
        if (dependencyTypeName != null) {
            TypeDescriptor dependency = getType(dependencyTypeName).getTypeDescriptor();
            if (!containingTypeDescriptor.equals(dependency)) {
                dependentDescriptor.addDependency(dependency);
            }
        }
    }
}
