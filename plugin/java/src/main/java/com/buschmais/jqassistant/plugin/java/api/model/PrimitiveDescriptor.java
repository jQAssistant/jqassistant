package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Represents a primitive value.
 */
@Label("Primitive")
@Abstract
public interface PrimitiveDescriptor extends JavaByteCodeDescriptor {
}
