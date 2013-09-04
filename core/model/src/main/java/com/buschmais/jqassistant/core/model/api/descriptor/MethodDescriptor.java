package com.buschmais.jqassistant.core.model.api.descriptor;

import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;

import java.util.HashSet;
import java.util.Set;

/**
 * Describes a method of a Java class.
 */
public class MethodDescriptor extends AbstractDescriptor implements NamedDescriptor, DependentDescriptor, AnnotatedDescriptor, AccessModifierDescriptor {

    /**
     * The name of the method
    */
    private String name;

    /**
     * The parameters of this this method.
     */
    private Set<ParameterDescriptor> parameters = new HashSet<>();

    /**
     * The declared throwables.
     */
    private Set<TypeDescriptor> declaredThrowables = new HashSet<>();

    /**
     * The classes the method depends on.
     */
    private Set<TypeDescriptor> dependencies = new HashSet<>();

    /**
     * The annotations this method is annotated by.
     */
    private Set<AnnotationValueDescriptor> annotations = new HashSet<>();

    /**
     * The fields read by this method.
     */
    private Set<FieldDescriptor> reads = new HashSet<>();

    /**
     * The fields written by this method.
     */
    private Set<FieldDescriptor> writes = new HashSet<>();

    /**
     * The methods invoked by this method.
     */
    private Set<MethodDescriptor> invokes = new HashSet<>();

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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Set<ParameterDescriptor> getParameters() {
        return parameters;
    }

    public void setParameters(Set<ParameterDescriptor> parameters) {
        this.parameters = parameters;
    }

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
    public Set<AnnotationValueDescriptor> getAnnotatedBy() {
        return annotations;
    }

    @Override
    public void setAnnotatedBy(Set<AnnotationValueDescriptor> annotations) {
        this.annotations = annotations;
    }

    public Set<FieldDescriptor> getReads() {
        return reads;
    }

    public void setReads(Set<FieldDescriptor> reads) {
        this.reads = reads;
    }

    public Set<FieldDescriptor> getWrites() {
        return writes;
    }

    public void setWrites(Set<FieldDescriptor> writes) {
        this.writes = writes;
    }

    public Set<MethodDescriptor> getInvokes() {
        return invokes;
    }

    public void setInvokes(Set<MethodDescriptor> invokes) {
        this.invokes = invokes;
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
