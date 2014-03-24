package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.xo.neo4j.api.annotation.Label;

import static com.buschmais.jqassistant.plugin.java.impl.store.descriptor.Java.JavaLanguageElement.Type;

/**
 * Denotes an interface type.
 */
@Java(Type)
@Label("INTERFACE")
public interface InterfaceTypeDescriptor extends TypeDescriptor {
}
