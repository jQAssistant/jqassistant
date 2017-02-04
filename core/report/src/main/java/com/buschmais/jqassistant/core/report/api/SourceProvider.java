package com.buschmais.jqassistant.core.report.api;

import com.buschmais.xo.api.CompositeObject;

/**
 * Defines a provider which allows looking up source code information from {@link CompositeObject}s.
 */
public interface SourceProvider<D extends CompositeObject> {

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
    String getSourceFile(D descriptor);

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
