package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a file entry.
 */
public interface File extends Entry {

    InputStream createStream() throws IOException;

}
