package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolverStrategy;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;

/**
 * Defines the scopes for java.
 */
public enum JavaScope implements Scope {

    CLASSPATH {
        @Override
        public void create(ScannerContext context) {
            TypeResolver typeResolver = getTypeResolver(context);
            context.peek(FileResolver.class).addStrategy(new ClassFileResolverStrategy());
            context.push(TypeResolver.class, typeResolver);
        }

        @Override
        public void destroy(ScannerContext context) {
            context.peek(FileResolver.class).removeStrategy(ClassFileResolverStrategy.class);
            context.pop(TypeResolver.class);
        }

        private TypeResolver getTypeResolver(ScannerContext context) {
            TypeResolver typeResolver = context.peek(TypeResolver.class);
            if (typeResolver != null) {
                return new DelegatingTypeResolver(typeResolver);
            } else {
                JavaArtifactFileDescriptor artifactDescriptor = context.peek(JavaArtifactFileDescriptor.class);
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

    /**
     * A file resolver that matches on .class files and returns the
     * corresponding type.
     */
    private static class ClassFileResolverStrategy implements FileResolverStrategy {

        private static final String CLASS_SUFFIX = ".class";

        @Override
        public Descriptor resolve(String path, ScannerContext context) {
            if (path.toLowerCase().endsWith(CLASS_SUFFIX)) {
                String typeName = path.substring(1, path.length() - CLASS_SUFFIX.length()).replaceAll("/", ".");
                TypeResolver typeResolver = context.peek(TypeResolver.class);
                return typeResolver.resolve(typeName, context).getTypeDescriptor();
            }
            return null;
        }

    }

}
