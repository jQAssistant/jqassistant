package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import java.util.HashSet;
import java.util.Set;

import com.buschmais.jqassistant.core.store.api.descriptor.AbstractFullQualifiedNameDescriptor;

/**
 * Describes a parameter of a method.
 */
public class ParameterDescriptor extends AbstractFullQualifiedNameDescriptor implements DependentDescriptor, AnnotatedDescriptor {

	/**
	 * The classes the method depends on.
	 */
	private Set<TypeDescriptor> dependencies = new HashSet<>();

	/**
	 * The annotations this method is annotated by.
	 */
	private Set<AnnotationValueDescriptor> annotations = new HashSet<>();

	/**
	 * <code>true</code> if this method is final, otherwise <code>false</code>.
	 */
	private Boolean finalParameter;

	@Override
	public Set<TypeDescriptor> getDependencies() {
		return dependencies;
	}

	@Override
	public void setDependencies(Set<TypeDescriptor> dependencies) {
		this.dependencies = dependencies;
	}

	@Override
	public Set<AnnotationValueDescriptor> getAnnotatedBy() {
		return annotations;
	}

	@Override
	public void setAnnotatedBy(Set<AnnotationValueDescriptor> annotations) {
		this.annotations = annotations;
	}
}
