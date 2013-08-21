package com.buschmais.jqassistant.core.scanner.api;

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
     * Scans the given classes by resolving their files as resources from the class path.
     *
     * @param classTypes The classes.
     * @throws IOException If scanning fails.
     */
    void scanClasses(Class<?>... classTypes) throws IOException;

    /**
     * Scans the given classes by resolving their files as resources from the class path.
     *
     * @param artifact   The containing artifact.
     * @param classTypes The classes.
     * @throws IOException If scanning fails.
     */
    void scanClasses(ArtifactDescriptor artifact, Class<?>... classTypes) throws IOException;

    /**
     * Scans an inputstream representing a class.
     *
     * @param artifactDescriptor The {@link ArtifactDescriptor} describing the artifact which provides the class input stream.
     * @param inputStream        The input stream.
     * @param name               The name (e.g. file name).
     * @throws IOException If scanning fails.
     */
    void scanInputStream(ArtifactDescriptor artifactDescriptor, InputStream inputStream, String name) throws IOException;


}
