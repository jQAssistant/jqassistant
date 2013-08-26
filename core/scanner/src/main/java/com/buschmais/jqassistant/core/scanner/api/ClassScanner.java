package com.buschmais.jqassistant.core.scanner.api;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

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
    Collection<TypeDescriptor> scanClasses(Class<?>... classTypes) throws IOException;

    /**
     * Scans an inputstream representing a class.
     *
     * @param inputStream The input stream.
     * @param name        The name (e.g. file name).
     * @throws IOException If scanning fails.
     */
    TypeDescriptor scanInputStream(InputStream inputStream, String name) throws IOException;


}
