package com.buschmais.jqassistant.scanner.api;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class scanner interface.
 * <p>Provides various methods for scanning class files.</p>
 */
public interface ClassScanner {

    /**
     * Scans an archive,e .g. JAR file.
     *
     * @param archive The archive.
     * @throws IOException If scanning fails.
     */
    void scanArchive(File archive) throws IOException;

    /**
     * Scans an archive,e .g. JAR file.
     *
     * @param artifactDescriptor The {@link ArtifactDescriptor} describing the archive to be scanned.
     * @param archive            The archive.
     * @throws IOException If scanning fails.
     */
    void scanArchive(ArtifactDescriptor artifactDescriptor, File archive) throws IOException;

    /**
     * Scans a directory containing classes.
     *
     * @param directory The directory.
     * @throws IOException If Scanning fails.
     */
    void scanClassDirectory(File directory) throws IOException;

    /**
     * Scans a directory containing classes.
     *
     * @param artifactDescriptor The {@link ArtifactDescriptor} describing the directory to be scanned.
     * @param directory          The directory.
     * @throws IOException If Scanning fails.
     */
    void scanClassDirectory(ArtifactDescriptor artifactDescriptor, File directory) throws IOException;

    /**
     * Scans a class file.
     *
     * @param artifactDescriptor The {@link ArtifactDescriptor} describing the artifact which contains the class file.
     * @param file               The class file.
     * @throws IOException If scanning fails.
     */
    void scanClassFile(ArtifactDescriptor artifactDescriptor, File file) throws IOException;

    /**
     * Scans the given classes by resolving their files as resources from the class path.
     *
     * @param classTypes The classes.
     * @throws IOException If scanning fails.
     */
    void scanClasses(Class<?>... classTypes) throws IOException;

    /**
     * Scans an input stream representing a class.
     *
     * @param inputStream The input stream.
     * @param name        The name (e.g. file name).
     * @throws IOException If scanning fails.
     */
    void scanInputStream(InputStream inputStream, String name) throws IOException;

    /**
     * Scans an inputstream representing a class.
     *
     * @param artifactDescriptor The {@link ArtifactDescriptor} describing the artifact which provides the class input stream.
     * @param inputStream        The input stream.
     * @param name               The name (e.g. file name).
     * @throws IOException If scanning fails.
     */
    void scanInputStream(ArtifactDescriptor artifactDescriptor, InputStream inputStream, String name) throws IOException;

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
}
