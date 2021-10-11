package com.buschmais.jqassistant.plugin.java.api.report;

import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.report.FileSourceHelper;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

import lombok.NoArgsConstructor;

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
        return TypeSourceHelper.getSourceLocation(typeDescriptor, empty(), empty());
    }

    static Optional<FileLocation> getSourceLocation(TypeDescriptor typeDescriptor, Integer lineNumber) {
        return getSourceLocation(typeDescriptor, ofNullable(lineNumber), ofNullable(lineNumber));
    }

    static Optional<FileLocation> getSourceLocation(TypeDescriptor typeDescriptor, Optional<Integer> startLine, Optional<Integer> endLine) {
        if (typeDescriptor instanceof ClassFileDescriptor) {
            ClassFileDescriptor classFileDescriptor = (ClassFileDescriptor) typeDescriptor;
            FileLocation.FileLocationBuilder fileLocationBuilder = FileLocation.builder();
            for (FileDescriptor parent : classFileDescriptor.getParents()) {
                if (parent instanceof PackageDescriptor) {
                    PackageDescriptor packageDescriptor = (PackageDescriptor) parent;
                    fileLocationBuilder.fileName(packageDescriptor.getFileName() + "/" + classFileDescriptor.getSourceFileName());
                }
            }
            fileLocationBuilder.parent(FileSourceHelper.getParentLocation(classFileDescriptor));
            fileLocationBuilder.startLine(startLine);
            fileLocationBuilder.endLine(endLine);
            return of(fileLocationBuilder.build());
        }
        return empty();
    }
}
