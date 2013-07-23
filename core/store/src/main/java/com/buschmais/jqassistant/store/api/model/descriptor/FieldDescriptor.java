package com.buschmais.jqassistant.store.api.model.descriptor;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes a field (i.e. static or instance variable) of a Java class.
 */
public class FieldDescriptor extends ParentDescriptor implements DependentDescriptor, AnnotatedDescriptor {

    /**
     * The classes the field depends on.
     */
    private Set<ClassDescriptor> dependencies = new HashSet<ClassDescriptor>();

    /**
     * The classes this class is annotated by.
     */
    private Set<ClassDescriptor> annotations = new HashSet<ClassDescriptor>();

    @Override
    public Set<ClassDescriptor> getDependencies() {
        return dependencies;
    }

    @Override
    public void setDependencies(Set<ClassDescriptor> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public Set<ClassDescriptor> getAnnotatedBy() {
        return annotations;
    }

    @Override
    public void setAnnotatedBy(Set<ClassDescriptor> annotations) {
        this.annotations = annotations;
    }

}
