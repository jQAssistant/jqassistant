package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;

public class TypeResolverBuilder {

    private TypeResolverBuilder() {
    }

    public static TypeResolver createTypeResolver(ScannerContext context) {
        JavaArtifactFileDescriptor artifactDescriptor = context.peek(JavaArtifactFileDescriptor.class);
        if (artifactDescriptor != null) {
            return new ArtifactBasedTypeResolver(artifactDescriptor);
        } else {
            return new DefaultTypeResolver();
        }
    }

}
