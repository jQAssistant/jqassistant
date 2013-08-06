package com.buschmais.jqassistant.core.model.api.descriptor;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes a Java class.
 */
public class ClassDescriptor extends ParentDescriptor implements DependentDescriptor, AnnotatedDescriptor,
		AccessModifierDescriptor {

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
     * The classes this class is annotated by.
     */
    private Set<ClassDescriptor> annotations = new HashSet<ClassDescriptor>();

    /**
	 * <code>true</code> if this class is abstract.
	 */
	private Boolean abstractClass;

	/**
	 * Visibility of this class.
	 */
	private VisibilityModifier visbility;

	/**
	 * <code>true</code> if this class is static, otherwise <code>false</code>.
	 */
	private Boolean staticClass;

	/**
	 * <code>true</code> if this class is final, otherwise <code>false</code>.
	 */
	private Boolean finalClass;

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
     * @param superClass The super class.
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
     * @param interfaces The implemented interfaces.
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

    @Override
    public Set<ClassDescriptor> getAnnotatedBy() {
        return annotations;
    }

    @Override
    public void setAnnotatedBy(Set<ClassDescriptor> annotations) {
        this.annotations = annotations;
    }

	/**
	 * @return the abstractClass
	 */
	public Boolean isAbstract() {
		return abstractClass;
	}

	/**
	 * @param abstractClass the abstractClass to set
	 */
	public void setAbstract(boolean abstractClass) {
		this.abstractClass = abstractClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VisibilityModifier getVisibility() {
		return visbility;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVisibility(VisibilityModifier visibilityModifier) {
		visbility = visibilityModifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean isStatic() {
		return staticClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStatic(Boolean s) {
		staticClass = s;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean isFinal() {
		return finalClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFinal(Boolean f) {
		finalClass = f;
	}

}
