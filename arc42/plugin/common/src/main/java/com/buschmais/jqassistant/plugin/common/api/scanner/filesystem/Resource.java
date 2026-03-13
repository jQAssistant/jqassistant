package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import java.io.IOException;

/**
 * Represents a file system resource.
 */
public interface Resource extends AutoCloseable {

    @Override
    void close() throws IOException;
}
