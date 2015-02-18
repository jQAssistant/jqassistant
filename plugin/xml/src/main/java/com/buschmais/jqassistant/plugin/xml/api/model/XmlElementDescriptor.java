package com.buschmais.jqassistant.plugin.xml.api.model;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Label("Element")
public interface XmlElementDescriptor extends XmlDescriptor, OfNamespaceDescriptor, SiblingDescriptor {

    @Outgoing
    @HasElement
    List<XmlElementDescriptor> getElements();

    @Incoming
    @HasElement
    XmlElementDescriptor getParent();

    @Relation("HAS_ATTRIBUTE")
    List<XmlAttributeDescriptor> getAttributes();

    @Relation("HAS_TEXT")
    List<XmlTextDescriptor> getCharacters();

    @Relation("DECLARES_NAMESPACE")
    List<XmlNamespaceDescriptor> getDeclaredNamespaces();

    @Relation("HAS_FIRST_CHILD")
    XmlDescriptor getFirstChild();

    void setFirstChild(XmlDescriptor firstChild);

    @Relation("HAS_LAST_CHILD")
    XmlDescriptor getLastChild();

    void setLastChild(XmlDescriptor lastChild);

    @Relation("HAS_ELEMENT")
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface HasElement {
    }
}
