package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Property;
import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;

/**
 * Describes a field (i.e. static or instance variable) of a Java class.
 */
@Label(value = "FIELD")
public interface FieldDescriptor extends SignatureDescriptor, NamedDescriptor, TypedDescriptor, DependentDescriptor, AnnotatedDescriptor,
		AccessModifierDescriptor {

	/**
	 * @return the transientField
	 */
	@Property("TRANSIENT")
	public Boolean isTransient();

	/**
	 * @param transientField
	 *            the transientField to set
	 */
	public void setTransient(Boolean transientField);

	/**
	 * @return the volatileField
	 */
	@Property("VOLATILE")
	public Boolean isVolatile();

	/**
	 * @param volatileField
	 *            the volatileField to set
	 */
	public void setVolatile(Boolean volatileField);

}
