package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.ParentDescriptor;

/**
 * Describes a Java package.
 */
@Label(value = "PACKAGE", usingIndexOf = FullQualifiedNameDescriptor.class)
public interface PackageDescriptor extends ParentDescriptor, SignatureDescriptor {
}
