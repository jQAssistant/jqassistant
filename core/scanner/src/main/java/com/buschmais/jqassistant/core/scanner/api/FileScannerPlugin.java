package com.buschmais.jqassistant.core.scanner.api;

import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;

/**
 * Defines the interface for plugins for scanning files.
 * 
 * @param <T>
 *            The type of descriptors generated by this plugin.
 */
public interface FileScannerPlugin<T extends Descriptor> extends ScannerPlugin {

	/**
	 * Match given file name.
	 * 
	 * @param file
	 *            The file or directory name.
	 * @param isDirectory
	 *            <code>true</code> if the file is a directory.
	 * @return <code>true</code> If the file shall be scanned.
	 */
	boolean matches(String file, boolean isDirectory);

	/**
	 * Perform scanning of a file.
	 * 
	 * @param store
	 *            The {@Store}.
	 * @param streamSource
	 *            The {@StreamSource}.
	 * @return The descriptor representing the file.
	 * @throws IOException
	 *             If scanning fails.
	 */
	T scanFile(Store store, StreamSource streamSource) throws IOException;

	/**
	 * Perform scanning of a file.
	 * 
	 * @param store
	 *            The {@Store}.
	 * @return The descriptor representing the file.
	 * @throws IOException
	 *             If scanning fails.
	 */
	T scanDirectory(Store store, String name) throws IOException;
}
