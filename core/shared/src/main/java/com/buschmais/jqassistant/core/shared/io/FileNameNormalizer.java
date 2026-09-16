package com.buschmais.jqassistant.core.shared.io;

import java.io.File;

import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

/**
 * Provides functionality for normalizing file names, e.g. replacing backslashes
 * by dashes (Unix style).
 */
@Deprecated
@ToBeRemovedInVersion(major = 3, minor = 0)
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
