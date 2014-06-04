package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Defines a descriptor containing line number information.
 */
public interface LineNumberDescriptor {

    @Property("lineNumber")
    Integer getLineNumber();

    void setLineNumber(Integer lineNumber);

}
