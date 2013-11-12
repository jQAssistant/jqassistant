package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Relation;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;

import java.util.Set;

/**
 * A descriptor representing a property file.
 */
@Label(value = "PROPERTYFILE", usingIndexOf = FullQualifiedNameDescriptor.class)
public interface PropertyFileDescriptor extends FullQualifiedNameDescriptor {

    @Relation("HAS")
    public Set<PropertyDescriptor> getProperties();

}
