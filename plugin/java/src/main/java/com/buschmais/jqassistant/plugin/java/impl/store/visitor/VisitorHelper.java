package com.buschmais.jqassistant.plugin.java.impl.store.visitor;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.*;
import com.buschmais.jqassistant.plugin.java.impl.store.query.FindParameterQuery;
import com.buschmais.jqassistant.plugin.java.impl.store.query.GetOrCreateFieldQuery;
import com.buschmais.jqassistant.plugin.java.impl.store.query.GetOrCreateMethodQuery;
import com.buschmais.jqassistant.plugin.java.impl.store.resolver.DescriptorResolverFactory;
import org.apache.commons.collections.map.LRUMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Class containing helper methods for ASM visitors.
 */
public class VisitorHelper {

    /**
     * The name of constructor methods.
     */
    private static final String CONSTRUCTOR_METHOD = "void <init>";
    private static final int TYPE_CACHE_SIZE = 16384;
    private static final int MEMBER_CACHE_SIZE = 256;

    private DescriptorResolverFactory resolverFactory;
    private Store store;

    private Map<String, TypeDescriptor> typeCache = new LRUMap(TYPE_CACHE_SIZE);
    private Map<TypeDescriptor, Map<String, MethodDescriptor>> methodCache = new LRUMap(TYPE_CACHE_SIZE);
    private Map<TypeDescriptor, Map<String, FieldDescriptor>> fieldCache = new LRUMap(TYPE_CACHE_SIZE);

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
     *
     * @param typeName The full qualified name of the type (e.g. java.lang.Object).
     */
    TypeDescriptor getTypeDescriptor(String fullQualifiedName) {
        return getTypeDescriptor(fullQualifiedName, TypeDescriptor.class);
    }

    /*
     * Return the type descriptor for the given type name.
     *
     * @param typeName The full qualified name of the type (e.g. java.lang.Object).
     *
     * @param type The expected type.
     */
    TypeDescriptor getTypeDescriptor(String fullQualifiedName, Class<? extends TypeDescriptor> type) {
        TypeDescriptor typeDescriptor = typeCache.get(fullQualifiedName);
        if (typeDescriptor != null && !type.isAssignableFrom(typeDescriptor.getClass())) {
            typeCache.remove(typeDescriptor);
            methodCache.remove(typeDescriptor);
            fieldCache.remove(typeDescriptor);
            typeDescriptor = null;
        }
        if (typeDescriptor == null) {
            typeDescriptor = resolverFactory.getTypeDescriptorResolver().resolve(fullQualifiedName, type);
            typeCache.put(fullQualifiedName, typeDescriptor);
        }
        return typeDescriptor;
    }

    /**
     * Return the method descriptor for the given type and method signature.
     *
     * @param type      The containing type.
     * @param signature The method signature.
     * @return The method descriptor.
     */
    MethodDescriptor getMethodDescriptor(TypeDescriptor type, String signature) {
        Map<String, MethodDescriptor> methodsOfType = getMemberCache(type, methodCache);
        MethodDescriptor methodDescriptor = methodsOfType.get(signature);
        if (methodDescriptor == null) {
            Map<String, Object> params = new HashMap<>();
            params.put("type", type);
            params.put("signature", signature);
            methodDescriptor = store.executeQuery(GetOrCreateMethodQuery.class, params).getSingleResult().getMethod();
            if (signature.startsWith(CONSTRUCTOR_METHOD) && !ConstructorDescriptor.class.isAssignableFrom(methodDescriptor.getClass())) {
                methodDescriptor = store.migrate(methodDescriptor, ConstructorDescriptor.class);
            }
            methodsOfType.put(signature, methodDescriptor);
        }
        return methodDescriptor;
    }

    /**
     * Get the member cache for a type descriptor.
     *
     * @param type        The type descriptor.
     * @param memberCache The cache holding members by their type.
     * @param <T>         The member type.
     * @return The member cache.
     */
    private <T extends Descriptor> Map<String, T> getMemberCache(TypeDescriptor type, Map<TypeDescriptor, Map<String, T>> memberCache) {
        Map<String, T> membersOfType = memberCache.get(type);
        if (membersOfType == null) {
            membersOfType = new LRUMap(MEMBER_CACHE_SIZE);
            memberCache.put(type, membersOfType);
        }
        return membersOfType;
    }

    /**
     * Return the field descriptor for the given type and field signature.
     *
     * @param type      The containing type.
     * @param signature The field signature.
     * @return The field descriptor.
     */
    FieldDescriptor getFieldDescriptor(TypeDescriptor type, String signature) {
        Map<String, FieldDescriptor> fieldsOfType = getMemberCache(type, fieldCache);
        FieldDescriptor fieldDescriptor = fieldsOfType.get(signature);
        if (fieldDescriptor == null) {
            Map<String, Object> params = new HashMap<>();
            params.put("type", type);
            params.put("signature", signature);
            fieldDescriptor = store.executeQuery(GetOrCreateFieldQuery.class, params).getSingleResult().getField();
            fieldsOfType.put(signature, fieldDescriptor);
        }
        return fieldDescriptor;
    }

    /**
     * Creates a {@link ValueDescriptor}.
     *
     * @param valueDescriptorType The type of the value descriptor.
     * @param <T>                 The type of the value descriptor.
     * @return The value descriptor.
     */
    <T extends ValueDescriptor> T getValueDescriptor(Class<T> valueDescriptorType) {
        return store.create(valueDescriptorType);
    }

    /**
     * Add an annotation descriptor of the given type name to an annotated
     * descriptor.
     *
     * @param annotatedDescriptor The annotated descriptor.
     * @param typeName            The type name of the annotation.
     * @return The annotation descriptor.
     */
    AnnotationValueDescriptor addAnnotation(AnnotatedDescriptor annotatedDescriptor, String typeName) {
        if (typeName != null) {
            TypeDescriptor type = getTypeDescriptor(typeName);
            AnnotationValueDescriptor annotationDescriptor = store.create(AnnotationValueDescriptor.class);
            annotationDescriptor.setType(type);
            annotatedDescriptor.getAnnotatedBy().add(annotationDescriptor);
            return annotationDescriptor;
        }
        return null;
    }

    /**
     * Create and return the parameter descriptor for the given methodDescriptor
     * and parameter index.
     *
     * @param methodDescriptor The declaring methodDescriptor.
     * @param index            The parameter index.
     * @return The parameter descriptor.
     */
    ParameterDescriptor addParameterDescriptor(MethodDescriptor methodDescriptor, int index) {
        ParameterDescriptor parameterDescriptor = store.create(ParameterDescriptor.class);
        parameterDescriptor.setIndex(index);
        methodDescriptor.getParameters().add(parameterDescriptor);
        return parameterDescriptor;
    }

    /**
     * Return the parameter descriptor for the given methodDescriptor and
     * parameter index.
     *
     * @param methodDescriptor The declaring methodDescriptor.
     * @param index            The parameter index.
     * @return The parameter descriptor.
     */
    ParameterDescriptor getParameterDescriptor(MethodDescriptor methodDescriptor, int index) {
        Map<String, Object> params = new HashMap<>();
        params.put("method", methodDescriptor);
        params.put("index", index);
        return store.executeQuery(FindParameterQuery.class, params).getSingleResult().getParameter();
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
}
