package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.api.model.AnnotatedDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.AnnotationValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ConstructorDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.DependentDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ParameterDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ValueDescriptor;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(VisitorHelper.class);

    private DescriptorResolverFactory resolverFactory;
    private Store store;

    private Cache<String, TypeDescriptor> typeCache;
    private Cache<String, MethodDescriptor> methodCache;
    private Cache<String, FieldDescriptor> fieldCache;

    /**
     * Constructor.
     * 
     * @param store
     *            The store.
     * @param resolverFactory
     *            The resolver factory used for looking up descriptors.
     */
    public VisitorHelper(Store store, DescriptorResolverFactory resolverFactory) {
        this.typeCache = CacheBuilder.newBuilder().softValues().build();
        this.methodCache = CacheBuilder.newBuilder().softValues().build();
        this.fieldCache = CacheBuilder.newBuilder().softValues().build();
        this.store = store;
        this.resolverFactory = resolverFactory;
    }

    /*
     * Return the type descriptor for the given type name.
     * 
     * @param typeName The full qualified name of the type (e.g.
     * java.lang.Object).
     */
    TypeDescriptor getTypeDescriptor(String fullQualifiedName) {
        return getTypeDescriptor(fullQualifiedName, TypeDescriptor.class);
    }

    /*
     * Return the type descriptor for the given type name.
     * 
     * @param typeName The full qualified name of the type (e.g.
     * java.lang.Object).
     * 
     * @param type The expected type.
     */
    <T extends TypeDescriptor> T getTypeDescriptor(String fullQualifiedName, Class<T> type) {
        TypeDescriptor typeDescriptor = typeCache.getIfPresent(fullQualifiedName);
        if (typeDescriptor != null && !type.isAssignableFrom(typeDescriptor.getClass())) {
            typeCache.invalidate(typeDescriptor);
            typeDescriptor = null;
        }
        if (typeDescriptor == null) {
            typeDescriptor = resolverFactory.getTypeDescriptorResolver().resolve(fullQualifiedName, type);
            typeCache.put(fullQualifiedName, typeDescriptor);
        }
        return type.cast(typeDescriptor);
    }

    /**
     * Return the method descriptor for the given type and method signature.
     * 
     * @param type
     *            The containing type.
     * @param signature
     *            The method signature.
     * @return The method descriptor.
     */
    MethodDescriptor getMethodDescriptor(TypeDescriptor type, String signature) {
        String memberKey = getMemberKey(type, signature);
        MethodDescriptor methodDescriptor = MethodDescriptor.class.cast(methodCache.getIfPresent(memberKey));
        if (methodDescriptor == null) {
            methodDescriptor = type.getOrCreateMethod(signature);
            if (signature.startsWith(CONSTRUCTOR_METHOD) && !ConstructorDescriptor.class.isAssignableFrom(methodDescriptor.getClass())) {
                methodDescriptor = store.migrate(methodDescriptor, ConstructorDescriptor.class);
            }
            methodCache.put(memberKey, methodDescriptor);
        }
        return methodDescriptor;
    }

    /**
     * Return the field descriptor for the given type and field signature.
     * 
     * @param type
     *            The containing type.
     * @param signature
     *            The field signature.
     * @return The field descriptor.
     */
    FieldDescriptor getFieldDescriptor(TypeDescriptor type, String signature) {
        String memberKey = getMemberKey(type, signature);
        FieldDescriptor fieldDescriptor = fieldCache.getIfPresent(memberKey);
        if (fieldDescriptor == null) {
            fieldDescriptor = type.getOrCreateField(signature);
            fieldCache.put(signature, fieldDescriptor);
        }
        return fieldDescriptor;
    }

    /**
     * Creates a unique key for a type member.
     * 
     * @param type
     *            The type.
     * @param signature
     *            The field signature.
     * @return The key.
     */
    private String getMemberKey(TypeDescriptor type, String signature) {
        return type.getFullQualifiedName() + "#" + signature;
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
        return store.create(valueDescriptorType);
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
        if (typeName != null) {
            TypeDescriptor type = getTypeDescriptor(typeName);
            AnnotationValueDescriptor annotationDescriptor = store.create(AnnotationValueDescriptor.class);
            annotationDescriptor.setType(type);
            annotatedDescriptor.addAnnotatedBy(annotationDescriptor);
            return annotationDescriptor;
        }
        return null;
    }

    /**
     * Create and return the parameter descriptor for the given methodDescriptor
     * and parameter index.
     * 
     * @param methodDescriptor
     *            The declaring methodDescriptor.
     * @param index
     *            The parameter index.
     * @return The parameter descriptor.
     */
    ParameterDescriptor addParameterDescriptor(MethodDescriptor methodDescriptor, int index) {
        ParameterDescriptor parameterDescriptor = store.create(ParameterDescriptor.class);
        parameterDescriptor.setIndex(index);
        methodDescriptor.addParameter(parameterDescriptor);
        return parameterDescriptor;
    }

    /**
     * Adds a dependency to the given type name to a dependent descriptor.
     * 
     * @param dependentDescriptor
     *            The dependent descriptor.
     * @param typeName
     *            The type name of the dependency.
     */
    void addDependency(DependentDescriptor dependentDescriptor, String typeName) {
        if (typeName != null) {
            TypeDescriptor dependency = getTypeDescriptor(typeName);
            dependentDescriptor.addDependency(dependency);
        }
    }
}
