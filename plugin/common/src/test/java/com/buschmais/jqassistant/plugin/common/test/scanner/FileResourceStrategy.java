package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.File;
import java.net.MalformedURLException;

/**
 * The strategies for referencing the archive to scan.
 */
enum FileResourceStrategy {
    /**
     * As URL.
     */
    Url {
        Object get(File file) throws MalformedURLException {
            return file.toURI().toURL();
        }
    },
    /**
     * As file.
     */
    File {
        Object get(File file) {
            return file;
        }
    };

    abstract <T> T get(File file) throws Exception;
}
