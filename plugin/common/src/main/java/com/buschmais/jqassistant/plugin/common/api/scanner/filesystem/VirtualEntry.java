package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import java.io.IOException;

/**
 * Represents a file system entry.
 */
public interface VirtualEntry extends AutoCloseable {

    @Override
    void close() throws IOException;
}
