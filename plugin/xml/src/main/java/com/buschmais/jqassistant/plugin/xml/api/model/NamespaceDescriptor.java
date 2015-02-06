package com.buschmais.jqassistant.plugin.xml.api.model;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;

public interface NamespaceDescriptor extends NamedDescriptor {

    String getNamespaceUri();
    void setNamespaceUri(String uri);

    String getNamespacePrefix();
    void setNamespacePrefix(String prefix);

}
