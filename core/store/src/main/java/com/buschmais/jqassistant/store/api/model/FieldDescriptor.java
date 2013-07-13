package com.buschmais.jqassistant.store.api.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes a field (i.e. static or instance variable) of a Java class.
 */
public class FieldDescriptor extends ParentDescriptor implements DependentDescriptor {

    /**
     * The classes the field depends on.
     */
    private Set<ClassDescriptor> dependencies = new HashSet<ClassDescriptor>();

    @Override
    public Set<ClassDescriptor> getDependencies() {
        return dependencies;
    }

    @Override
    public void setDependencies(Set<ClassDescriptor> dependencies) {
        this.dependencies = dependencies;
    }

}
