package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a file entry.
 */
public interface VirtualFile extends VirtualEntry {

    InputStream createStream() throws IOException;

    java.io.File getFile();
}
