package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes a parameter of a method.
 */
@Label(value = "Parameter")
public interface ParameterDescriptor extends JavaByteCodeDescriptor, TypedDescriptor, AnnotatedDescriptor, IndexTemplate {

}
