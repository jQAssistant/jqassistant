package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import java.io.IOException;

/**
 * Represents a local directory resource.
 */
public class LocalDirectoryResource implements DirectoryResource, LocalResource {

    @Override
    public void close() throws IOException {
    }

}
