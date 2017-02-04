package com.buschmais.jqassistant.core.report.api;

import com.buschmais.xo.api.CompositeObject;

/**
 * Defines the interface for language elements to be returned by
 * {@link Language} annotations.
 */
public interface LanguageElement {

    String getLanguage();

    /**
     * The name of the language element.
     * 
     * @return The name of the language element.
     */
    String name();

    <D extends CompositeObject> SourceProvider<D> getSourceProvider();

}
