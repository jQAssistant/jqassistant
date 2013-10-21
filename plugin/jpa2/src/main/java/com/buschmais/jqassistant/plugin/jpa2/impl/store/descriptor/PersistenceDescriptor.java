package com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor;

import java.util.HashSet;
import java.util.Set;

import com.buschmais.jqassistant.core.store.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;

/**
 * A descriptor for JPA persistence descriptors.
 */
public class PersistenceDescriptor extends AbstractDescriptor implements NamedDescriptor {

	private String name;
	private String version;
	/**
	 * The persistence units referenced by this persistence unit.
	 */
	private Set<PersistenceUnitDescriptor> contains = new HashSet<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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
