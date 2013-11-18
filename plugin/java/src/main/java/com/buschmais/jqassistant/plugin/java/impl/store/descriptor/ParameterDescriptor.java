package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;

/**
 * Describes a parameter of a method.
 */
@Label(value = "PARAMETER")
public interface ParameterDescriptor extends FullQualifiedNameDescriptor, DependentDescriptor, AnnotatedDescriptor {
}
