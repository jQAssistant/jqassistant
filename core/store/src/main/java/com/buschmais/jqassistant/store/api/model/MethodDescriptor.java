package com.buschmais.jqassistant.store.api.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes a method of a Java class.
 */
public class MethodDescriptor extends AbstractDescriptor implements DependentDescriptor {

    /**
     * The declared throwables.
     */
    private Set<ClassDescriptor> declaredThrowables = new HashSet<ClassDescriptor>();

    /**
     * The classes the method depends on.
     */
    private Set<ClassDescriptor> dependencies = new HashSet<ClassDescriptor>();

    /**
     * Return the declared throwables.
     *
     * @return The declared throwables.
     */
    public Set<ClassDescriptor> getDeclaredThrowables() {
        return declaredThrowables;
    }

    /**
     * Set the declared throwables.
     *
     * @param declaredThrowables The declared throwables.
     */
    public void setDeclaredThrowables(Set<ClassDescriptor> declaredThrowables) {
        this.declaredThrowables = declaredThrowables;
    }

    @Override
    public Set<ClassDescriptor> getDependencies() {
        return dependencies;
    }

    @Override
    public void setDependencies(Set<ClassDescriptor> dependencies) {
        this.dependencies = dependencies;
    }
}
