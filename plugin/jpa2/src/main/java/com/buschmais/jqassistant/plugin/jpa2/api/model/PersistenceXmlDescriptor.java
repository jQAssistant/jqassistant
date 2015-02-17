package com.buschmais.jqassistant.plugin.jpa2.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * A descriptor for JPA model descriptors.
 */
@Label("Persistence")
public interface PersistenceXmlDescriptor extends XmlDescriptor, FileDescriptor, JpaDescriptor {

    @Property("version")
    public String getVersion();

    public void setVersion(String version);

    @Property("contains")
    public List<PersistenceUnitDescriptor> getContains();

}
