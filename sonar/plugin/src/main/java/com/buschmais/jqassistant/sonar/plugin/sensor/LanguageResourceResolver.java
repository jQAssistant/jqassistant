package com.buschmais.jqassistant.sonar.plugin.sensor;

import org.sonar.api.BatchExtension;
import org.sonar.api.resources.Resource;

/**
 * Defines an interface for resolving resources representing language specific
 * elements, e.g. java classes.
 */
public interface LanguageResourceResolver extends BatchExtension {

    /**
     * Return the language this resolver represents.
     * 
     * @return The language.
     */
    String getLanguage();

    /**
     * Resolve the resource for an element of a given type.
     * 
     * @param type
     *            The type.
     * @param name
     *            The name of the element (e.g. the class name).
     * @return The resource.
     */
    Resource resolve(String type, String name);
}
