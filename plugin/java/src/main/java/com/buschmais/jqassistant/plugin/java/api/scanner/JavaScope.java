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
        public void onEnter(ScannerContext context) {
            TypeResolver typeResolver = getTypeResolver(context);
            context.push(TypeResolver.class, typeResolver);
        }

        @Override
        public void onLeave(ScannerContext context) {
            context.pop(TypeResolver.class);
        }

        private TypeResolver getTypeResolver(ScannerContext context) {
            TypeResolver typeResolver = context.peekOrDefault(TypeResolver.class, null);
            if (typeResolver != null) {
                return new DelegatingTypeResolver(typeResolver);
            } else {
                JavaArtifactFileDescriptor artifactDescriptor = context.peekOrDefault(JavaArtifactFileDescriptor.class, null);
                if (artifactDescriptor != null) {
                    return new ClasspathScopedTypeResolver(artifactDescriptor);
                }
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
