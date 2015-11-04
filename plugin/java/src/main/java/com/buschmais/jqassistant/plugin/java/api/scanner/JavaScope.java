package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResolverStrategy;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * Defines the scopes for java.
 */
public enum JavaScope implements Scope {

    CLASSPATH {
        @Override
        public void onEnter(ScannerContext context) {
            TypeResolver typeResolver = getTypeResolver(context);
            context.peek(FileResolver.class).push(new ClassFileResolverStrategy());
            context.push(TypeResolver.class, typeResolver);
        }

        @Override
        public void onLeave(ScannerContext context) {
            context.peek(FileResolver.class).pop();
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

    /**
     * A file resolver that matches on .class files and returns the
     * corresponding type.
     */
    private static class ClassFileResolverStrategy extends AbstractFileResolverStrategy {

        private static final String CLASS_SUFFIX = ".class";

        @Override
        public <D extends FileDescriptor> D require(String path, Class<D> type, ScannerContext context) {
            return null;
        }

        @Override
        public <D extends FileDescriptor> D match(String path, Class<D> type, ScannerContext context) {
            if (path.toLowerCase().endsWith(CLASS_SUFFIX)) {
                String typeName = path.substring(1, path.length() - CLASS_SUFFIX.length()).replaceAll("/", ".");
                TypeResolver typeResolver = context.peek(TypeResolver.class);
                TypeDescriptor typeDescriptor = typeResolver.resolve(typeName, context).getTypeDescriptor();
                return toFileDescriptor(typeDescriptor, type, path, context);
            }
            return null;
        }
    }
}
