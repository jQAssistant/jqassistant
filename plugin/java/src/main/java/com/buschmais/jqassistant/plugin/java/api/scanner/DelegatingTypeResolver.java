package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * A type resolver which only delegates to another instance.
 */
public class DelegatingTypeResolver implements TypeResolver {

    private TypeResolver delegate;

    /**
     * Constructor.
     * 
     * @param delegate
     *            The delegate.
     */
    public DelegatingTypeResolver(TypeResolver delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T extends ClassFileDescriptor> TypeCache.CachedType<T> create(String fullQualifiedName, FileDescriptor fileDescriptor, Class<T> descriptorType,
            ScannerContext scannerContext) {
        return delegate.create(fullQualifiedName, fileDescriptor, descriptorType, scannerContext);
    }

    @Override
    public TypeCache.CachedType<TypeDescriptor> resolve(String fullQualifiedName, ScannerContext context) {
        return delegate.resolve(fullQualifiedName, context);
    }
}
