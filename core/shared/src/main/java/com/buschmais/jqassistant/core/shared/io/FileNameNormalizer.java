package com.buschmais.jqassistant.core.shared.io;

import java.io.File;

/**
 * Provides functionality for normalizing file names, e.g. replacing backslashes
 * by dashes (Unix style).
 */
public final class FileNameNormalizer {

    private FileNameNormalizer() {
    }

    /**
     * @param path
     *            The path.
     * @return The slashified path.
     */
    public static String normalize(String path) {
        return path.replace('\\', '/');
    }

    /**
     * @param file
     *            The {@link File}.
     * @return The slashified path.
     */
    public static String normalize(File file) {
        return normalize(file.getAbsolutePath());
    }
}
