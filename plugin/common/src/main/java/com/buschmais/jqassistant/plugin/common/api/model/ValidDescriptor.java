package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Template for descriptor which indicate that information is invalid.
 */
public interface ValidDescriptor {

    /* tag::valid-property[]
     | `valid`
     | always
     | Indicates whether or not the item underlying the
       node was successfully scanned.
     end::valid-property[] */
    @Property("valid")
    boolean isValid();

    void setValid(boolean valid);

}
