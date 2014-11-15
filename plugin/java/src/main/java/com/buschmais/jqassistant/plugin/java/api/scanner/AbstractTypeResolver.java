package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

public abstract class AbstractTypeResolver implements TypeResolver {

    private TypeCache typeCache;

    protected AbstractTypeResolver() {
        this.typeCache = new TypeCache();
    }

    @Override
    public TypeCache.CachedType<TypeDescriptor> resolve(String fullQualifiedName, ScannerContext context) {
        TypeCache.CachedType cachedType = typeCache.get(fullQualifiedName);
        if (cachedType == null) {
            TypeDescriptor typeDescriptor = findType(fullQualifiedName, context);
            if (typeDescriptor == null) {
                typeDescriptor = createType(fullQualifiedName, TypeDescriptor.class, context);
                addRequiredType(typeDescriptor);
            }
            cachedType = toCachedType(typeDescriptor);
            typeCache.put(fullQualifiedName, cachedType);
        }
        return cachedType;
    }

    @Override
    public <T extends TypeDescriptor> TypeCache.CachedType<T> create(String fullQualifiedName, Class<T> descriptorType, ScannerContext context) {
        TypeCache.CachedType cachedType = typeCache.get(fullQualifiedName);
        if (cachedType == null) {
            T typeDescriptor;
            TypeDescriptor resolvedType = findType(fullQualifiedName, context);
            if (resolvedType == null) {
                typeDescriptor = createType(fullQualifiedName, descriptorType, context);
            } else if (!(descriptorType.isAssignableFrom(resolvedType.getClass()))) {
                typeDescriptor = migrateType(descriptorType, resolvedType, context);
            } else {
                typeDescriptor = descriptorType.cast(resolvedType);
            }
            cachedType = toCachedType(typeDescriptor);
            typeCache.put(fullQualifiedName, cachedType);
        } else {
            T typeDescriptor;
            TypeDescriptor resolvedType = cachedType.getTypeDescriptor();
            if (!descriptorType.isAssignableFrom(resolvedType.getClass())) {
                typeDescriptor = migrateType(descriptorType, resolvedType, context);
                cachedType.migrate(typeDescriptor);
            }
        }
        return cachedType;
    }

    private <T extends TypeDescriptor> T createType(String fullQualifiedName, Class<T> descriptorType, ScannerContext scannerContext) {
        T typeDescriptor = scannerContext.getStore().create(descriptorType);
        String name;
        int separatorIndex = fullQualifiedName.lastIndexOf('.');
        if (separatorIndex != -1) {
            name = fullQualifiedName.substring(separatorIndex + 1);
        } else {
            name = fullQualifiedName;
        }
        typeDescriptor.setName(name);
        typeDescriptor.setFullQualifiedName(fullQualifiedName);
        return typeDescriptor;
    }

    private <T extends TypeDescriptor> T migrateType(Class<T> descriptorType, TypeDescriptor resolvedType, ScannerContext context) {
        T typeDescriptor = context.getStore().migrate(resolvedType, descriptorType);
        removeRequiredType(typeDescriptor);
        return typeDescriptor;
    }

    private TypeCache.CachedType toCachedType(TypeDescriptor typeDescriptor) {
        TypeCache.CachedType cachedType;
        cachedType = new TypeCache.CachedType(typeDescriptor);
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
        for (TypeDescriptor descriptor : typeDescriptor.getDependencies()) {
            cachedType.addDependency(descriptor.getFullQualifiedName(), typeDescriptor);
        }
        return cachedType;
    }

    protected abstract TypeDescriptor findType(String fullQualifiedName, ScannerContext context);

    protected abstract <T extends TypeDescriptor> void removeRequiredType(T typeDescriptor);

    protected abstract void addRequiredType(TypeDescriptor typeDescriptor);
}
