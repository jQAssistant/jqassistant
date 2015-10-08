package com.buschmais.jqassistant.plugin.maven3.api.artifact;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;

public interface ArtifactResolver {

    /**
     * Resolves an artifact descriptor for the given coordinates, i.e. by
     * looking up an existing one and creating new one on demand.
     *
     * @param coordinates
     *            The artifact coordinates.
     * @param descriptorType
     *            The required descriptor type.
     * @param scannerContext
     *            The scanner context.
     * @param <A>
     *            The required descriptor type.
     * @return The resolved artifact descriptor.
     */
    <A extends ArtifactDescriptor> A resolve(Coordinates coordinates, Class<A> descriptorType, ScannerContext scannerContext);

    /**
     * Find an artifact identified by the given coordinates.
     *
     * @param coordinates
     *            The coordinates.
     * @param scannerContext
     *            The scanner context.
     * @return The artifact descriptor or <code>null</code>.
     */
    ArtifactDescriptor find(Coordinates coordinates, ScannerContext scannerContext);
}
