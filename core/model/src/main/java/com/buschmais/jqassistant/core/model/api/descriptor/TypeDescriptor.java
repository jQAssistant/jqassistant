package com.buschmais.jqassistant.core.model.api.descriptor;

import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes a Java type.
 */
public class TypeDescriptor extends ParentDescriptor implements DependentDescriptor, AnnotatedDescriptor, AccessModifierDescriptor {

    /**
     * The super class.
     */
    private TypeDescriptor superClass;

    /**
     * The implemented interfaces.
     */
    private Set<TypeDescriptor> interfaces = new HashSet<>();

    /**
     * The classes this class depends on.
     */
    private Set<TypeDescriptor> dependencies = new HashSet<>();

    /**
     * The annotations this class is annotated by.
     */
    private Set<AnnotationValueDescriptor> annotations = new HashSet<>();

    /**
     * The java types.
     */
    private JavaType javaType;

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
     * <code>true</code> if this class is synthetic, otherwise <code>false</code>.
     */
    private Boolean syntheticClass;

    /**
     * Return the super class.
     *
     * @return The super class.
     */
    public TypeDescriptor getSuperClass() {
        return superClass;
    }

    /**
     * Set the super class.
     *
     * @param superClass The super class.
     */
    public void setSuperClass(TypeDescriptor superClass) {
        this.superClass = superClass;
    }

    /**
     * Return the implemented interfaces.
     *
     * @return The implemented interfaces.
     */
    public Set<TypeDescriptor> getInterfaces() {
        return interfaces;
    }

    /**
     * Set the implemented interfaces.
     *
     * @param interfaces The implemented interfaces.
     */
    public void setInterfaces(Set<TypeDescriptor> interfaces) {
        this.interfaces = interfaces;
    }

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

    @Override
    public Boolean isSynthetic() {
        return syntheticClass;
    }

    @Override
    public void setSynthetic(Boolean syntheticClass) {
        this.syntheticClass = syntheticClass;
    }

    /**
     * Return the java types.
     *
     * @return The java types.
     */
    public JavaType getJavaType() {
        return javaType;
    }

    /**
     * Set the java types.
     *
     * @param javaType The java types.
     */
    public void setJavaType(JavaType javaType) {
        this.javaType = javaType;
    }
}
