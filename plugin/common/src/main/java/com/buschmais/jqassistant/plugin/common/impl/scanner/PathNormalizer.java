package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.File;
import java.nio.file.Path;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;

/**
 * Provides functionality for normalizing file names, e.g. replacing backslashes
 * by dashes (Unix style).
 */
public final class PathNormalizer {

    private PathNormalizer() {
    }

    /**
     * @param file
     *     The {@link File}.
     * @return The slashified path.
     */
    public static String normalize(File file, ScannerContext context) {
        String path = getRelativePath(file, context);
        return path.replace('\\', '/');
    }

    private static String getRelativePath(File file, ScannerContext context) {
        File projectDirectory = context.getProjectDirectory();
        Path projectPath = projectDirectory.toPath()
            .toAbsolutePath()
            .normalize();
        Path filePath = file.toPath()
            .toAbsolutePath()
            .normalize();
        return "/" + projectPath.relativize(filePath);
    }
}
