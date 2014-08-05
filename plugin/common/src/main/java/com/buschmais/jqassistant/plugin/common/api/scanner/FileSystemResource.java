package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a file system resource which is created by a scanner.
 */
public interface FileSystemResource {

    InputStream createStream() throws IOException;

    boolean isDirectory();
}
