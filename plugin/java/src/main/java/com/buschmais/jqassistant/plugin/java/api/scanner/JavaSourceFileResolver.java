package com.buschmais.jqassistant.plugin.java.api.scanner;

import java.util.Optional;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

/**
 * Defines a resolver for Java source files.
 */
public interface JavaSourceFileResolver {

    /**
     * Resolve a {@link FileDescriptor} representing a Java source file for the given relativeSourcePath.
     *
     * @param relativeSourcePath
     *     The relative source path.
     * @param context
     *     The {@link ScannerContext}.
     * @return The optional {@link FileDescriptor}.
     */
    Optional<FileDescriptor> resolveSourceFile(String relativeSourcePath, ScannerContext context);
}
