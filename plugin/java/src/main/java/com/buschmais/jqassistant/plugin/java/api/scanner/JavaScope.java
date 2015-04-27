package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;

/**
 * Defines the scopes for java.
 */
public enum JavaScope implements Scope {

    CLASSPATH {
        @Override
        public void create(ScannerContext context) {
            context.push(TypeResolver.class, getTypeResolver(context));
        }

        @Override
        public void destroy(ScannerContext context) {
            context.pop(TypeResolver.class);
        }

        private TypeResolver getTypeResolver(ScannerContext context) {
            TypeResolver typeResolver = context.peek(TypeResolver.class);
            if (typeResolver != null) {
                return new DelegatingTypeResolver(typeResolver);
            }
            JavaArtifactFileDescriptor artifactDescriptor = context.peek(JavaArtifactFileDescriptor.class);
            if (artifactDescriptor != null) {
                return new ClasspathScopedTypeResolver(artifactDescriptor);
            }
            return new DefaultTypeResolver();
        }
    };

    @Override
    public String getPrefix() {
        return "java";
    }

    @Override
    public String getName() {
        return name();
    }

}
