package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.jqassistant.plugin.java.api.report.Java;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

/**
 * Describes a field (i.e. static or instance variable) of a Java class.
 */
@Java(Java.JavaLanguageElement.Field)
@Label(value = "Variable")
public interface VariableDescriptor extends JavaByteCodeDescriptor, SignatureDescriptor, NamedDescriptor, TypedDescriptor {

    @Declares
    @Incoming
    MethodDescriptor getMethod();
}
