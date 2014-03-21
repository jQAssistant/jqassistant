package com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Property;
import com.buschmais.jqassistant.core.scanner.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.core.scanner.api.descriptor.NamedDescriptor;

import java.util.Set;

/**
 * A descriptor for JPA model descriptors.
 */
@Label("PERSISTENCE")
public interface PersistenceDescriptor extends FileDescriptor, NamedDescriptor, JpaDescriptor {

    @Property("VERSION")
    public String getVersion();

    public void setVersion(String version);

    @Property("CONTAINS")
    public Set<PersistenceUnitDescriptor> getContains();

}
