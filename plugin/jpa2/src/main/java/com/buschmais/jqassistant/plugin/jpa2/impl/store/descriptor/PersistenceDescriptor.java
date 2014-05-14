package com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor;

import java.util.Set;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * A descriptor for JPA model descriptors.
 */
@Label("Persistence")
public interface PersistenceDescriptor extends FileDescriptor, NamedDescriptor, JpaDescriptor {

    @Property("version")
    public String getVersion();

    public void setVersion(String version);

    @Property("contains")
    public Set<PersistenceUnitDescriptor> getContains();

}
