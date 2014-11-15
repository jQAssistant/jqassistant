package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactDescriptor;

public class TypeResolverBuilder {

    private TypeResolverBuilder() {
    }

    public static TypeResolver createTypeResolver(ScannerContext context) {
        JavaArtifactDescriptor artifactDescriptor = context.peek(JavaArtifactDescriptor.class);
        if (artifactDescriptor != null) {
            return new ArtifactBasedTypeResolver(artifactDescriptor);
        } else {
            return new DefaultTypeResolver();
        }
    }

}
