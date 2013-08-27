package com.buschmais.jqassistant.core.scanner.api;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Defines the interface for an artifact scanner.
 */
public interface ArtifactScanner {

    /**
     * Scans an archive,e .g. JAR file.
     *
     * @param artifactDescriptor The {@link ArtifactDescriptor} describing the archive to be scanned.
     * @param archive            The archive.
     * @throws IOException If scanning fails.
     */
    void scanArchive(ArtifactDescriptor artifactDescriptor, File archive) throws IOException;

    /**
     * Scans a directory.
     *
     * @param artifactDescriptor The {@link ArtifactDescriptor} describing the directory to be scanned.
     * @param directory          The directory.
     * @throws IOException If Scanning fails.
     */
    void scanDirectory(ArtifactDescriptor artifactDescriptor, File directory) throws IOException;

    /**
     * Scans the given classes.
     *
     * @param artifactDescriptor The {@link ArtifactDescriptor} describing the directory to be scanned.
     * @param classes            The classes.
     * @throws IOException If Scanning fails.
     */
    void scanClasses(ArtifactDescriptor artifactDescriptor, Class<?>... classes) throws IOException;

    /**
     * Scans the given URLs.
     *
     * @param artifactDescriptor The {@link ArtifactDescriptor} describing the directory to be scanned.
     * @param urls               The URLs.
     * @throws IOException If Scanning fails.
     */
    void scanURLs(ArtifactDescriptor artifactDescriptor, URL... urls) throws IOException;
}
