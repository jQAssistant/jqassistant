package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Interface that describes java elements with access modifiers.
 * 
 * @author Herklotz
 */
public interface AccessModifierDescriptor {

    @Property("visibility")
    String getVisibility();

    void setVisibility(String visibility);

    @Property("static")
    Boolean isStatic();

    void setStatic(Boolean s);

    @Property("final")
    Boolean isFinal();

    void setFinal(Boolean f);

    @Property("synthetic")
    Boolean isSynthetic();

    void setSynthetic(Boolean s);
}
