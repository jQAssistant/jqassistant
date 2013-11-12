package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Relation;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;

import java.util.Set;

/**
 * Interface describing a {@link Descriptor} which depends on other
 * {@link TypeDescriptor}s.
 */
public interface DependentDescriptor extends Descriptor {

	/**
	 * Return the classes this descriptor depends on.
	 *
	 * @return The classes this descriptor depends on.
	 */
    @Relation("DEPENDS_ON")
	Set<TypeDescriptor> getDependencies();

}
