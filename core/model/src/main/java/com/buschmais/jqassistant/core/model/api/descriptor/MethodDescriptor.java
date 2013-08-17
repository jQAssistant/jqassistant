package com.buschmais.jqassistant.core.model.api.descriptor;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes a method of a Java class.
 */
public class MethodDescriptor extends AbstractDescriptor implements DependentDescriptor, AnnotatedDescriptor, AccessModifierDescriptor {

    /**
     * The declared throwables.
     */
    private Set<TypeDescriptor> declaredThrowables = new HashSet<TypeDescriptor>();

    /**
     * The classes the method depends on.
     */
    private Set<TypeDescriptor> dependencies = new HashSet<TypeDescriptor>();

    /**
     * The classes this class is annotated by.
     */
    private Set<TypeDescriptor> annotations = new HashSet<TypeDescriptor>();

    /**
     * <code>true</code> if this method is a constructorMethod.
     */
    private Boolean constructorMethod;

    /**
     * <code>true</code> if this method is native.
     */
    private Boolean nativeMethod;

    /**
     * <code>true</code> if this method is abstract.
     */
    private Boolean abstractMethod;

    /**
     * Visibility of this method.
     */
    private VisibilityModifier visbility;

    /**
     * <code>true</code> if this method is static, otherwise <code>false</code>.
     */
    private Boolean staticMethod;

    /**
     * <code>true</code> if this method is final, otherwise <code>false</code>.
     */
    private Boolean finalMethod;

    /**
     * <code>true</code> if this method is synthetic, otherwise <code>false</code>.
     */
    private Boolean syntheticMethod;

    public Set<TypeDescriptor> getDeclaredThrowables() {
        return declaredThrowables;
    }

    public void setDeclaredThrowables(Set<TypeDescriptor> declaredThrowables) {
        this.declaredThrowables = declaredThrowables;
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
    public Set<TypeDescriptor> getAnnotatedBy() {
        return annotations;
    }

    @Override
    public void setAnnotatedBy(Set<TypeDescriptor> annotations) {
        this.annotations = annotations;
    }

    public Boolean isAbstract() {
        return abstractMethod;
    }

    public void setAbstract(Boolean abstractClass) {
        this.abstractMethod = abstractClass;
    }

    @Override
    public VisibilityModifier getVisibility() {
        return visbility;
    }

    @Override
    public void setVisibility(VisibilityModifier visibilityModifier) {
        visbility = visibilityModifier;
    }

    @Override
    public Boolean isStatic() {
        return staticMethod;
    }

    @Override
    public void setStatic(Boolean s) {
        staticMethod = s;
    }

    @Override
    public Boolean isFinal() {
        return finalMethod;
    }

    public void setFinal(Boolean f) {
        finalMethod = f;
    }

    public Boolean isNative() {
        return nativeMethod;
    }

    public void setNative(Boolean nativeMethod) {
        this.nativeMethod = nativeMethod;
    }

    public Boolean isConstructor() {
        return constructorMethod;
    }

    public void setConstructor(Boolean constructor) {
        this.constructorMethod = constructor;
    }

    public Boolean isSynthetic() {
        return syntheticMethod;
    }

    public void setSynthetic(Boolean syntheticMethod) {
        this.syntheticMethod = syntheticMethod;
    }
}
