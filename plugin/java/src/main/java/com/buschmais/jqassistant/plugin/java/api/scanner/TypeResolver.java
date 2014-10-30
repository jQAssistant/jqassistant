package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

public class TypeResolver {

    private TypeResolver() {
    }

    public static <T extends TypeDescriptor> TypeDescriptor resolve(String fullQualifiedName, Class<T> expectedType, ScannerContext scannerContext) {
        TypeDescriptor typeDescriptor = scannerContext.getStore().find(TypeDescriptor.class, fullQualifiedName);
        if (typeDescriptor == null) {
            typeDescriptor = scannerContext.getStore().create(expectedType);
            String name;
            int separatorIndex = fullQualifiedName.lastIndexOf('.');
            if (separatorIndex != -1) {
                name = fullQualifiedName.substring(separatorIndex + 1);
            } else {
                name = fullQualifiedName;
            }
            typeDescriptor.setName(name);
            typeDescriptor.setFullQualifiedName(fullQualifiedName);
        }
        return typeDescriptor;
    }

    public static TypeDescriptor resolve(String fullQualifiedName, ScannerContext context) {
        return resolve(fullQualifiedName, TypeDescriptor.class, context);
    }
}
