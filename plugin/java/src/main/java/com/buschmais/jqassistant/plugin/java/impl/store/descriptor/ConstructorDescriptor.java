package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import static com.buschmais.jqassistant.plugin.java.impl.store.descriptor.Java.JavaLanguageElement.Constructor;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Java(Constructor)
@Label(value = "Constructor")
public interface ConstructorDescriptor extends MethodDescriptor {
}
