package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.File;
import java.nio.file.Path;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;

import org.jspecify.annotations.NonNull;

/**
 * Provides functionality for normalizing file names, e.g. replacing backslashes
 * by dashes (Unix style).
 */
public final class PathNormalizer {

    private PathNormalizer() {
    }

    /**
     * Determines the relative name of a file to the project root (including a leading "/").
     *
     * @param file
     *     The {@link File}.
     * @return The slashified path.
     */
    public static String normalizeFileName(File file, ScannerContext context) {
        return "/" + normalizePath(file, context);
    }

    /**
     * Determines the relative path of a file to the project root.
     *
     * @param file
     *     The {@link File}.
     * @return The slashified path.
     */
    public static String normalizePath(File file, ScannerContext context) {
        String path = relativize(context.getProjectDirectory(), file);
        return path.replace('\\', '/');
    }

    /**
     * Determine the relative file name of a child against a parent
     *
     * @param parent
     *     The parent.
     * @param child
     *     The child.
     * @return The relative file name.
     */
    public static @NonNull String relativize(File parent, File child) {
        Path projectPath = parent.toPath()
            .toAbsolutePath()
            .normalize();
        Path filePath = child.toPath()
            .toAbsolutePath()
            .normalize();
        return projectPath.relativize(filePath)
            .toString();
    }
}
