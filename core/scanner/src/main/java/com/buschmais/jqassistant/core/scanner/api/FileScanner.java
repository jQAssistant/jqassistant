package com.buschmais.jqassistant.core.scanner.api;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Defines the interface for an artifact scanner.
 */
public interface FileScanner {

    /**
     * Scans an archive,e .g. JAR file.
     *
     * @param archive The archive.
     * @throws IOException If scanning fails.
     */
    Iterable<FileDescriptor> scanArchive(File archive) throws IOException;

    /**
     * Scans a directory recursively.
     *
     * @param directory The directory.
     * @throws IOException If Scanning fails.
     */
    Iterable<FileDescriptor> scanDirectory(File directory) throws IOException;

    /**
     * Scans a directory.
     *
     * @param directory The directory.
     * @param recursive if <code>true</code> sub directories will be scanned recursively.
     * @throws IOException If Scanning fails.
     */
    Iterable<FileDescriptor> scanDirectory(File directory, boolean recursive) throws IOException;

    /**
     * Scans the given classes.
     *
     * @param classes The classes.
     * @throws IOException If Scanning fails.
     */
    Iterable<FileDescriptor> scanClasses(Class<?>... classes) throws IOException;

    /**
     * Scans the given URLs.
     *
     * @param urls The URLs.
     * @throws IOException If Scanning fails.
     */
    Iterable<FileDescriptor> scanURLs(URL... urls) throws IOException;

    /**
     * Scan the given files.
     *
     * @param directory reference directory for scanners
     * @param files     given files to scan
     * @throws IOException If Scanning fails.
     */
    Iterable<FileDescriptor> scanFiles(File directory, List<File> files) throws IOException;
}
