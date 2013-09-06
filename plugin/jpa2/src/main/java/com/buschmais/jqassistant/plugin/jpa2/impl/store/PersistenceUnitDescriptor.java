package com.buschmais.jqassistant.plugin.jpa2.impl.store;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;

import java.util.HashSet;
import java.util.Set;

/**
 * A descriptor for JPA persistence units.
 */
public class PersistenceUnitDescriptor extends AbstractDescriptor {

    /**
     * The classes referenced by this persistence unit.
     */
    private Set<TypeDescriptor> contains = new HashSet<>();

    public Set<TypeDescriptor> getContains() {
        return contains;
    }

    public void setContains(Set<TypeDescriptor> contains) {
        this.contains = contains;
    }
}
