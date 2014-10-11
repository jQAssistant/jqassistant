package com.buschmais.jqassistant.plugin.cdi.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Beans")
public interface BeansDescriptor extends CdiDescriptor, FileDescriptor {

    String getVersion();

    void setVersion(String version);

    String getBeanDiscoveryMode();

    void setBeanDiscoveryMode(String beanDiscoveryMode);

    @Relation("HAS_INTERCEPTOR")
    List<TypeDescriptor> getInterceptors();

    @Relation("HAS_DECORATOR")
    List<TypeDescriptor> getDecorators();

    @Relation("HAS_ALTERNATIVE")
    List<TypeDescriptor> getAlternatives();

}
