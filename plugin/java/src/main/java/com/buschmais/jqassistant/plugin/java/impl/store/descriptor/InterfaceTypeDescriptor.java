package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import static com.buschmais.jqassistant.plugin.java.impl.store.descriptor.Java.JavaLanguageElement.Type;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Denotes an interface type.
 */
@Java(Type)
@Label("INTERFACE")
public interface InterfaceTypeDescriptor extends TypeDescriptor {
}
