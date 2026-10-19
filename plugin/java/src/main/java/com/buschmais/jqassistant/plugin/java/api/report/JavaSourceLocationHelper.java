package com.buschmais.jqassistant.plugin.java.api.report;

import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.report.FileSourceLocationHelper;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.SourceFileTemplate;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

import lombok.NoArgsConstructor;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

/**
 * Provides utility functions for resolving source locations of Java
 * {@link FileDescriptor}s.
 */
@NoArgsConstructor(access = PRIVATE)
public class JavaSourceLocationHelper {

    static Optional<FileLocation> getSourceLocation(TypeDescriptor typeDescriptor) {
        return getSourceLocation(typeDescriptor, empty(), empty());
    }

    static Optional<FileLocation> getSourceLocation(TypeDescriptor typeDescriptor, Integer lineNumber) {
        return getSourceLocation(typeDescriptor, ofNullable(lineNumber), ofNullable(lineNumber));
    }

    static Optional<FileLocation> getSourceLocation(TypeDescriptor typeDescriptor, Optional<Integer> startLine, Optional<Integer> endLine) {
        if (typeDescriptor instanceof ClassFileDescriptor) {
            ClassFileDescriptor classFileDescriptor = (ClassFileDescriptor) typeDescriptor;
            return getSourceLocation(classFileDescriptor, startLine, endLine);
        }
        return empty();
    }

    static Optional<FileLocation> getSourceLocation(FileDescriptor fileDescriptor, Optional<Integer> startLine, Optional<Integer> endLine) {
        if (fileDescriptor instanceof SourceFileTemplate) {
            SourceFileTemplate sourceFileTemplate = (SourceFileTemplate) fileDescriptor;
            FileDescriptor sourceFile = sourceFileTemplate.getHasSourceFile();
            if (sourceFile != null) {
                return FileSourceLocationHelper.getSourceLocation(fileDescriptor, sourceFile, startLine, endLine);
            }
        }
        return FileSourceLocationHelper.getSourceLocation(fileDescriptor, startLine, endLine);
    }
}
