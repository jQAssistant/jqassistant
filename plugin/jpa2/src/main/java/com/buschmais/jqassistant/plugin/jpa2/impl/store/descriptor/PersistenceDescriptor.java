package com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

import java.util.Set;

/**
 * A descriptor for JPA model descriptors.
 */
@Label("Persistence")
public interface PersistenceDescriptor extends FileDescriptor, NamedDescriptor, JpaDescriptor {

    @Property("Version")
    public String getVersion();

    public void setVersion(String version);

    @Property("Contains")
    public Set<PersistenceUnitDescriptor> getContains();

}
