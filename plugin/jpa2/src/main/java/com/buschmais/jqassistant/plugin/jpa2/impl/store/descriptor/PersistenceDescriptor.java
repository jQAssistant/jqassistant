package com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Property;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.plugin.common.impl.descriptor.NamedDescriptor;

import java.util.Set;

/**
 * A descriptor for JPA model descriptors.
 */
@Label("PERSISTENCE")
public interface PersistenceDescriptor extends Descriptor, NamedDescriptor, JpaDescriptor {

    @Property("VERSION")
    public String getVersion();

    public void setVersion(String version);

    @Property("CONTAINS")
    public Set<PersistenceUnitDescriptor> getContains();

}
