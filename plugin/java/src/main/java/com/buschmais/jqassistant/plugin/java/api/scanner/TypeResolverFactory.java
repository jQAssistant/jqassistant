package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;

/**
 * Factory to provide {@link TypeResolver} instances.
 */
public class TypeResolverFactory {

    /**
     * Private constructor.
     */
    private TypeResolverFactory() {
    }

    /**
     * Get a type resolver.
     * 
     * @param context
     *            The current scanner context.
     * @return The type resolver.
     */
    public static TypeResolver createTypeResolver(ScannerContext context) {
        TypeResolver typeResolver = context.peek(TypeResolver.class);
        if (typeResolver != null) {
            return typeResolver;
        }
        JavaArtifactFileDescriptor artifactDescriptor = context.peek(JavaArtifactFileDescriptor.class);
        if (artifactDescriptor != null) {
            return new ArtifactBasedTypeResolver(artifactDescriptor);
        }
        return new DefaultTypeResolver();
    }

}
