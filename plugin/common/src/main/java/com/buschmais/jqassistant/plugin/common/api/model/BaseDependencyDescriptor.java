package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;

public interface BaseDependencyDescriptor extends Descriptor {

    /**
     * Get the scope of the dependency - `compile`,
     * `runtime`, `test`, `system`, and
     * `provided`. Used to calculate the various classpaths used for
     * compilation, testing, and so on. It also assists in determining which
     * artifacts to include in a distribution of this project.
     *
     * @see http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html[Introduction to the Dependency Mechanism^]
     * @return The scope.
     */
    String getScope();

    /**
     * Set the scope.
     * 
     * @param scope
     *            The scope.
     */
    void setScope(String scope);

    /**
     * Get indicates the dependency is optional for use of this library. While
     * the version of the dependency will be taken into account for dependency
     * calculation if the library is used elsewhere, it will not be passed on
     * transitively.
     * 
     * @return Is optional?
     */
    boolean isOptional();

    /**
     * Set optional.
     * 
     * @param optional
     *            Is optional?
     */
    void setOptional(boolean optional);

}
