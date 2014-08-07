package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Denotes an interface type.
 */
@Java(Java.JavaLanguageElement.Type)
@Label("Interface")
public interface InterfaceTypeDescriptor extends ClassFileDescriptor {
}
