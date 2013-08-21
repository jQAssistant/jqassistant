package com.buschmais.jqassistant.core.scanner.api;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;

import java.io.File;
import java.io.IOException;

/**
 * Defines the interface for an artifact scanner.
 */
public interface ArtifactScanner {

    /**
     * Defines a listener which is notified on each package of class scanned.
     */
    public abstract static class ScanListener {

        /**
         * This method is called before a package is scanned.
         */
        public void beforePackage() {
        }

        /**
         * This method is called after a package is scanned.
         */
        public void afterPackage() {
        }

        /**
         * This method is called before a class is scanned.
         */
        public void beforeClass() {
        }

        /**
         * This method is called after a class is scanned.
         */
        public void afterClass() {
        }
    }
    /**
     * Scans an archive,e .g. JAR file.
     *
     * @param archive The archive.
     * @throws java.io.IOException If scanning fails.
     */
    void scanArchive(File archive) throws IOException;

    /**
     * Scans an archive,e .g. JAR file.
     *
     * @param artifactDescriptor The {@link com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor} describing the archive to be scanned.
     * @param archive            The archive.
     * @throws IOException If scanning fails.
     */
    void scanArchive(ArtifactDescriptor artifactDescriptor, File archive) throws IOException;

    /**
     * Scans a directory containing classes.
     *
     * @param artifactDescriptor The {@link ArtifactDescriptor} describing the directory to be scanned.
     * @param directory          The directory.
     * @throws IOException If Scanning fails.
     */
    void scanClassDirectory(ArtifactDescriptor artifactDescriptor, File directory) throws IOException;

}
