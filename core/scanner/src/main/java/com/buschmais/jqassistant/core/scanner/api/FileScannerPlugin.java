package com.buschmais.jqassistant.core.scanner.api;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * Defines the interface for plugins for scanning files.
 */
public interface FileScannerPlugin extends ScannerPlugin {

    /**
     * Match given file name.
     *
     * @param file        The file or directory name.
     * @param isDirectory <code>true</code> if the file is a directory.
     * @return <code>true</code> If the file shall be scanned.
     */
    boolean matches(String file, boolean isDirectory);

    /**
     * Perform scanning of a file.
     *
     * @param streamSource The {@StreamSource}.
     * @return The descriptor representing the file.
     * @throws IOException If scanning fails.
     */
    Descriptor scanFile(StreamSource streamSource) throws IOException;

    /**
     * Perform scanning of a file.
     *
     * @return The descriptor representing the file.
     * @throws IOException If scanning fails.
     */
    Descriptor scanDirectory(String name) throws IOException;
}
