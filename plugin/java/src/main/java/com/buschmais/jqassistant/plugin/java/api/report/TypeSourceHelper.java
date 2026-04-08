package com.buschmais.jqassistant.plugin.java.api.report;

import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.report.FileSourceHelper;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaByteCodeFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import static java.util.Optional.*;
import static lombok.AccessLevel.PRIVATE;

/**
 * Provides utility functions for resolving source locations of Java
 * {@link TypeDescriptor}s.
 */
@NoArgsConstructor(access = PRIVATE)
public class TypeSourceHelper {

    static String getSourceFile(TypeDescriptor typeDescriptor) {
        return (typeDescriptor instanceof ClassFileDescriptor) ? ((ClassFileDescriptor) typeDescriptor).getFileName() : null;
    }

    static Optional<FileLocation> getSourceLocation(TypeDescriptor typeDescriptor) {
        return getSourceLocation(typeDescriptor, empty(), empty());
    }

    static Optional<FileLocation> getSourceLocation(TypeDescriptor typeDescriptor, Integer lineNumber) {
        return getSourceLocation(typeDescriptor, ofNullable(lineNumber), ofNullable(lineNumber));
    }

    static Optional<FileLocation> getSourceLocation(TypeDescriptor typeDescriptor, Optional<Integer> startLine, Optional<Integer> endLine) {
        if (typeDescriptor instanceof JavaByteCodeFileDescriptor) {
            JavaByteCodeFileDescriptor javaByteCodeFileDescriptor = (JavaByteCodeFileDescriptor) typeDescriptor;
            Optional<FileLocation> fileLocationBuilder = getSourceLocation(javaByteCodeFileDescriptor, startLine, endLine);
            if (fileLocationBuilder != null)
                return fileLocationBuilder;
        }
        return empty();
    }

    public static @Nullable Optional<FileLocation> getSourceLocation(JavaByteCodeFileDescriptor javaByteCodeFileDescriptor, Optional<Integer> startLine,
        Optional<Integer> endLine) {
        for (FileDescriptor parent : javaByteCodeFileDescriptor.getParents()) {
            if (parent instanceof PackageDescriptor) {
                // File location can only safely built if a parent package exists.
                PackageDescriptor packageDescriptor = (PackageDescriptor) parent;
                FileLocation.FileLocationBuilder fileLocationBuilder = FileLocation.builder();
                fileLocationBuilder.parent(FileSourceHelper.getParentLocation(javaByteCodeFileDescriptor));
                fileLocationBuilder.fileName(packageDescriptor.getFileName() + "/" + javaByteCodeFileDescriptor.getSourceFileName());
                fileLocationBuilder.startLine(startLine);
                fileLocationBuilder.endLine(endLine);
                return of(fileLocationBuilder.build());
            }
        }
        return empty();
    }
}
