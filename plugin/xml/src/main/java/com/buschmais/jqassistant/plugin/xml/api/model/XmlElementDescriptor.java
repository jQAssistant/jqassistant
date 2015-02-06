package com.buschmais.jqassistant.plugin.xml.api.model;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.XmlDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Label("Element")
public interface XmlElementDescriptor extends XmlDescriptor, NamespaceDescriptor {

    @Outgoing
    @HasElement
    List<XmlElementDescriptor> getElements();

    @Incoming
    @HasElement
    XmlElementDescriptor getParent();

    @Relation("HAS_ATTRIBUTE")
    List<XmlAttributeDescriptor> getAttributes();

    @Relation("HAS_CHARACTERS")
    List<XmlCharactersDescriptor> getCharacters();

    /**
    * Created by dimahler on 2/6/2015.
    */
    @Relation("HAS_ELEMENT")
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface HasElement {
    }
}
