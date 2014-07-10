package com.buschmais.jqassistant.plugin.java.impl.scanner.resolver;

import java.util.Hashtable;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

public class TypeDescriptorResolver extends AbstractPackageMemberDescriptorResolver<PackageDescriptor, TypeDescriptor> {

    Map<String, TypeDescriptor> types = new Hashtable<>();

    public TypeDescriptorResolver(Store store, PackageDescriptorResolver parentResolver) {
        super(store, parentResolver);
    }

    @Override
    protected Class<TypeDescriptor> getBaseType() {
        return TypeDescriptor.class;
    }

    @Override
    protected char getSeparator() {
        return '.';
    }

    @Override
    public <R extends TypeDescriptor> R resolve(String fullQualifiedName, Class<R> concreteType) {
        TypeDescriptor descriptor = types.get(fullQualifiedName);
        if (descriptor != null) {
            if (getBaseType().equals(concreteType)) {
                return concreteType.cast(descriptor);
            } else {
                return store.migrate(descriptor, concreteType);
            }
        } else {
            PackageDescriptor parent = null;
            String name;
            if (!EMPTY_NAME.equals(fullQualifiedName)) {
                int separatorIndex = fullQualifiedName.lastIndexOf(getSeparator());
                String parentName;
                if (separatorIndex != -1) {
                    name = fullQualifiedName.substring(separatorIndex + 1, fullQualifiedName.length());
                    parentName = fullQualifiedName.substring(0, separatorIndex);
                } else {
                    name = fullQualifiedName;
                    parentName = EMPTY_NAME;
                }
                parent = parentResolver.resolve(parentName);
            } else {
                name = EMPTY_NAME;
            }
            descriptor = store.create(concreteType, fullQualifiedName);
            descriptor.setName(name);
            if (parent != null) {
                parent.addContains(descriptor);
            }
            types.put(fullQualifiedName, descriptor);
            return concreteType.cast(descriptor);
        }
    }
}
