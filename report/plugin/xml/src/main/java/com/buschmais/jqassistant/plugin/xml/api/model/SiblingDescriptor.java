package com.buschmais.jqassistant.plugin.xml.api.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

public interface SiblingDescriptor {

    @Outgoing
    @HasSibling
    XmlDescriptor getNextSibling();
    void setNextSibling(XmlDescriptor nextSibling);

    @Incoming
    @HasSibling
    XmlDescriptor getPreviousSibling();
    void setPreviousSibling(XmlDescriptor nextSibling);

    @Relation("HAS_SIBLING")
    @Retention(RetentionPolicy.RUNTIME)
    public @interface HasSibling {
    }

}
