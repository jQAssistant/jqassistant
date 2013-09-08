package com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;

import java.util.HashSet;
import java.util.Set;

/**
 * A descriptor for JPA persistence descriptors.
 */
public class PersistenceDescriptor extends AbstractDescriptor {

    private String version;

    /**
     * The persistence units referenced by this persistence unit.
     */
    private Set<PersistenceUnitDescriptor> contains = new HashSet<>();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Set<PersistenceUnitDescriptor> getContains() {
        return contains;
    }

    public void setContains(Set<PersistenceUnitDescriptor> contains) {
        this.contains = contains;
    }
}
