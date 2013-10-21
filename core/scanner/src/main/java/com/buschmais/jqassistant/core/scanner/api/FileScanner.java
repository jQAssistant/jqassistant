package com.buschmais.jqassistant.core.scanner.api;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;

/**
 * Defines the interface for an artifact scanner.
 */
public interface FileScanner {

	/**
	 * Scans an archive,e .g. JAR file.
	 * 
	 * @param archive
	 *            The archive.
	 * @throws IOException
	 *             If scanning fails.
	 */
	Iterable<Descriptor> scanArchive(File archive) throws IOException;

	/**
	 * Scans a directory.
	 * 
	 * @param directory
	 *            The directory.
	 * @throws IOException
	 *             If Scanning fails.
	 */
	Iterable<Descriptor> scanDirectory(File directory) throws IOException;

	/**
	 * Scans the given classes.
	 * 
	 * @param classes
	 *            The classes.
	 * @throws IOException
	 *             If Scanning fails.
	 */
	Iterable<Descriptor> scanClasses(Class<?>... classes) throws IOException;

	/**
	 * Scans the given URLs.
	 * 
	 * @param urls
	 *            The URLs.
	 * @throws IOException
	 *             If Scanning fails.
	 */
	Iterable<Descriptor> scanURLs(URL... urls) throws IOException;
}
