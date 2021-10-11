package com.buschmais.jqassistant.core.report.api;

import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.model.source.FileLocation;
import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;
import com.buschmais.xo.api.CompositeObject;

import static java.util.Optional.empty;

/**
 * Defines a provider which allows looking up source code information from
 * {@link CompositeObject}s.
 */
public interface SourceProvider<D extends CompositeObject> {

    /**
     * Return the name representing the language element.
     *
     * @return The name.
     */
    String getName(D descriptor);

    /**
     * Return the name of the resource providing the source code, e.g. a file name.
     *
     * @param descriptor
     *            The descriptor.
     * @return The name of the resource.
     */
    @Deprecated
    @ToBeRemovedInVersion(major = 1, minor = 12)
    default String getSourceFile(D descriptor) {
        return null;
    }

    /**
     * Return the {@link FileLocation} for a {@link CompositeObject}.
     *
     * @param descriptor
     *            The {@link CompositeObject}
     * @return The {@link FileLocation} or <code>null</code> if it cannot be
     *         determined.
     */
    default Optional<FileLocation> getSourceLocation(D descriptor) {
        return empty();
    }

    /**
     * Return the line number where the descriptor is represented in the source
     * code.
     *
     * @param descriptor
     *            The descriptor.
     * @return The line numbers.
     */
    default Integer getLineNumber(D descriptor) {
        return null;
    };

}
