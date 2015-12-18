package com.buschmais.jqassistant.plugin.cdi.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Defines the label "Beans" representing a beans.xml descriptor.
 */
@Label("Beans")
public interface BeansXmlDescriptor extends CdiDescriptor, XmlFileDescriptor {

    /**
     * Return the version of the CDI specification.
     * 
     * @return The version of the CDI specification.
     */
    String getVersion();

    void setVersion(String version);

    /**
     * Return the bean discovery mode.
     * 
     * @return The bean discovery mode.
     */
    String getBeanDiscoveryMode();

    void setBeanDiscoveryMode(String beanDiscoveryMode);

    /**
     * Return all Java types which are declared as interceptor.
     * 
     * @return All Java types which are declared as interceptor.
     */
    @Relation("HAS_INTERCEPTOR")
    List<TypeDescriptor> getInterceptors();

    /**
     * Return all Java types which are declared as decorator.
     *
     * @return All Java types which are declared as decorator.
     */
    @Relation("HAS_DECORATOR")
    List<TypeDescriptor> getDecorators();

    /**
     * Return all Java types which are declared as alternative.
     *
     * @return All Java types which are declared as alternative.
     */
    @Relation("HAS_ALTERNATIVE")
    List<TypeDescriptor> getAlternatives();

}
