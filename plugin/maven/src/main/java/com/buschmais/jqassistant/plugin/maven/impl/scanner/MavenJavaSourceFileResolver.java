package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaSourceFileResolver;
import com.buschmais.xo.api.Query;

import lombok.RequiredArgsConstructor;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * {@link JavaSourceFileResolver} implementation for Maven projects.
 * <p>
 * The relative source paths are resolved against the provided {@link DirectoryDescriptor}s representing the source roots provided by the Maven project
 */
@RequiredArgsConstructor
public class MavenJavaSourceFileResolver implements JavaSourceFileResolver {

    private final List<DirectoryDescriptor> sourceDirectoryDescriptors;

    @Override
    public Optional<FileDescriptor> resolveSourceFile(String relativeSourcePath, ScannerContext context) {
        try (Query.Result<Query.Result.CompositeRowObject> result = context.getStore()
            .executeQuery("MATCH" //
                + "  shortestPath((sourceDirectory:File:Directory)-[:CONTAINS*]->(sourceFile:File{fileName:$fileName})) " //
                + "WHERE" //
                + "  sourceDirectory <> sourceFile " //
                + "  and id(sourceDirectory) in $sourceDirectories " //
                + "RETURN" //
                + "  sourceFile " //
                + "LIMIT " //
                + "  1", Map.of("sourceDirectories", sourceDirectoryDescriptors, "fileName", "/" + relativeSourcePath))) {
            return result.hasResult() ?
                of(result.getSingleResult()
                    .get("sourceFile", FileDescriptor.class)) :
                empty();
        }
    }
}
