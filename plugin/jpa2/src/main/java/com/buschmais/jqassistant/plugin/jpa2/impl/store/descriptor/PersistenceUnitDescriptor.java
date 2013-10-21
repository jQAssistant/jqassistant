package com.buschmais.jqassistant.plugin.jpa2.impl.store.descriptor;

import java.util.HashSet;
import java.util.Set;

import com.buschmais.jqassistant.core.store.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;

/**
 * A descriptor for JPA persistence units.
 */
public class PersistenceUnitDescriptor extends AbstractDescriptor implements NamedDescriptor {

	private String name;
	private String description;
	private String provider;
	private String jtaDataSource;
	private String nonJtaDataSource;
	private String validationMode;
	private String sharedCacheMode;
	private Set<PropertyDescriptor> properties = new HashSet<>();

	/**
	 * The classes referenced by this persistence unit.
	 */
	private Set<TypeDescriptor> contains = new HashSet<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getJtaDataSource() {
		return jtaDataSource;
	}

	public void setJtaDataSource(String jtaDataSource) {
		this.jtaDataSource = jtaDataSource;
	}

	public String getNonJtaDataSource() {
		return nonJtaDataSource;
	}

	public void setNonJtaDataSource(String nonJtaDataSource) {
		this.nonJtaDataSource = nonJtaDataSource;
	}

	public String getValidationMode() {
		return validationMode;
	}

	public void setValidationMode(String validationMode) {
		this.validationMode = validationMode;
	}

	public Set<TypeDescriptor> getContains() {
		return contains;
	}

	public void setContains(Set<TypeDescriptor> contains) {
		this.contains = contains;
	}

	public String getSharedCacheMode() {
		return sharedCacheMode;
	}

	public void setSharedCacheMode(String sharedCacheMode) {
		this.sharedCacheMode = sharedCacheMode;
	}

	public Set<PropertyDescriptor> getProperties() {
		return properties;
	}

	public void setProperties(Set<PropertyDescriptor> properties) {
		this.properties = properties;
	}
}
