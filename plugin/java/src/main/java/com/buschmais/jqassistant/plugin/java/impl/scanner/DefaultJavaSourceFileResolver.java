package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaSourceFileResolver;
import com.buschmais.xo.api.Query;

import lombok.RequiredArgsConstructor;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Default implementation of the {@link JavaSourceFileResolver}.
 * Works on a list of source directory paths, source file names are resolved against these directories.
 * If no source directory path is given then a global search for the source file is perforned.
 */
@RequiredArgsConstructor
public class DefaultJavaSourceFileResolver implements JavaSourceFileResolver {

    private final List<String> sourceDirectoryPaths;

    @Override
    public Optional<FileDescriptor> resolveSourceFile(String relativeSourcePath, ScannerContext context) {
        Map<String, Object> params = new HashMap<>();
        params.put("sourceDirectoryPaths", sourceDirectoryPaths);
        params.put("sourceFileName", "/" + relativeSourcePath);
        try (Query.Result<Query.Result.CompositeRowObject> result = context.getStore()
            .executeQuery("MATCH" //
                + "  shortestPath((directory:Directory)-[:CONTAINS*]->(sourceFile:File{fileName:$sourceFileName})) " //
                + "WHERE" //
                + "  $sourceDirectoryPaths is null" //
                + "  or any(sourceDirectory in $sourceDirectoryPaths WHERE directory.path ends with sourceDirectory) " //
                + "RETURN"  //
                + "  sourceFile " //
                + "ORDER BY "  //
                + "  id(sourceFile) desc " //
                + "LIMIT" //
                + "  1", params)) {
            return result.hasResult() ?
                of(result.getSingleResult()
                    .get("sourceFile", FileDescriptor.class)) :
                empty();
        }
    }
}
