package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.ValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.report.Java;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import static com.buschmais.jqassistant.plugin.java.api.report.Java.JavaLanguageElement.Method;

/**
 * Describes a method of a Java type.
 */
@Java(Method)
@Label(value = "Method")
public interface MethodDescriptor extends MemberDescriptor, AbstractDescriptor {

    /**
     * Return all declared parameters of this method.
     *
     * @return The declared parameters.
     */
    @Relation("HAS")
    List<ParameterDescriptor> getParameters();

    /**
     * Return the return type of this method.
     *
     * @return The return type.
     */
    @Relation("RETURNS")
    TypeDescriptor getReturns();

    void setReturns(TypeDescriptor returns);

    /**
     * Return the generic return type of this method.
     *
     * @return The generic return type.
     */
    @Relation("RETURNS_GENERIC")
    BoundDescriptor getReturnsGeneric();

    void setReturnsGeneric(BoundDescriptor returnsGeneric);

    @Relation("HAS_DEFAULT")
    ValueDescriptor<?> getHasDefault();

    void setHasDefault(ValueDescriptor<?> hasDefault);

    /**
     * Return all declared generic throwables of this method.
     *
     * @return The declared generic throwables.
     */
    @Relation("THROWS_GENERIC")
    List<BoundDescriptor> getThrowsGeneric();

    /**
     * Return all read accesses to fields this method performs.
     *
     * @return All read accesses to fields this method performs.
     */
    List<ReadsDescriptor> getReads();

    /**
     * Return all write accesses to fields this method performs.
     *
     * @return All write accesses to fields this method performs.
     */
    List<WritesDescriptor> getWrites();

    /**
     * Return all invocations this method performs.
     *
     * @return All invocations this method performs.
     */
    @Outgoing
    List<InvokesDescriptor> getInvokes();

    /**
     * Return all invocations of this method by other methods.
     *
     * @return The invocations of this method by other methods.
     */
    @Incoming
    List<InvokesDescriptor> getInvokedBy();

    /**
     * Return all thrown types (declared and actually thrown) of this method.
     *
     * @return The thrown types throwables.
     */
    List<ThrowsDescriptor> getThrows();

    /**
     * Return all caught exception types.
     *
     * @return The caught exception types.
     */
    @Relation("CATCHES")
    List<CatchesDescriptor> getCatches();

    /**
     * Return <code>true</code> if this method is native.
     *
     * @return <code>true</code> if this method is native.
     */
    Boolean isNative();

    void setNative(Boolean nativeMethod);

    /**
     * Return the cyclomatic complexity of the method.
     *
     * @return The cyclomatic complexity.
     */
    int getCyclomaticComplexity();

    void setCyclomaticComplexity(int cyclomaticComplexity);

    @Declares
    List<TypeDescriptor> getDeclaredInnerClasses();

    @Declares
    List<VariableDescriptor> getVariables();

    /**
     * Return the first line number of the method.
     *
     * @return The first line number of the method.
     */
    Integer getFirstLineNumber();

    void setFirstLineNumber(Integer firstLineNumber);

    /**
     * Return the last line number of the method.
     *
     * @return The last line number of the method.
     */
    Integer getLastLineNumber();

    void setLastLineNumber(Integer lastLineNumber);

    /**
     * Return the number of source code lines containing code.
     *
     * @return The number of source code lines containing code.
     */
    int getEffectiveLineCount();

    void setEffectiveLineCount(int effectiveLineCount);

}
