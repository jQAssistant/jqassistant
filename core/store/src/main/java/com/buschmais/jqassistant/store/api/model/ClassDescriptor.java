package com.buschmais.jqassistant.store.api.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes a Java class.
 */
public class ClassDescriptor extends ParentDescriptor implements
		DependentDescriptor {

	/**
	 * The super class.
	 */
	private ClassDescriptor superClass;

	/**
	 * The implemented interfaces.
	 */
	private Set<ClassDescriptor> interfaces = new HashSet<ClassDescriptor>();

	/**
	 * The classes this class depends on.
	 */
	private Set<ClassDescriptor> dependencies = new HashSet<ClassDescriptor>();

	/**
	 * Return the super class.
	 * 
	 * @return The super class.
	 */
	public ClassDescriptor getSuperClass() {
		return superClass;
	}

	/**
	 * Set the super class.
	 * 
	 * @param superClass
	 *            The super class.
	 */
	public void setSuperClass(ClassDescriptor superClass) {
		this.superClass = superClass;
	}

	/**
	 * Return the implemented interfaces.
	 * 
	 * @return The implemented interfaces.
	 */
	public Set<ClassDescriptor> getInterfaces() {
		return interfaces;
	}

	/**
	 * Set the implemented interfaces.
	 * 
	 * @param interfaces
	 *            The implemented interfaces.
	 */
	public void setInterfaces(Set<ClassDescriptor> interfaces) {
		this.interfaces = interfaces;
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
