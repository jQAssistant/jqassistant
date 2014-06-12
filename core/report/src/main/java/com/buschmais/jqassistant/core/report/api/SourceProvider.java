package com.buschmais.jqassistant.core.report.api;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

/**
 * Defines a provider which allows looking up source code information from
 * {@link com.buschmais.jqassistant.core.store.api.descriptor.Descriptor}s.
 */
public interface SourceProvider<D extends Descriptor> {

    /**
     * Return the name representing the language element.
     * 
     * @return The name.
     */
    String getName(D descriptor);

    /**
     * Return the name of the resource providing the source code, e.g. a file
     * name.
     * 
     * @param descriptor
     *            The descriptor.
     * @return The name of the resource.
     */
    FileDescriptor getSourceFile(D descriptor);

    /**
     * Return the line number where the descriptor is represented in the source
     * code.
     * 
     * @param descriptor
     *            The descriptor.
     * @return The line numbers.
     */
    Integer getLineNumber(D descriptor);

}
