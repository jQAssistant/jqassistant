package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.core.store.api.type.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.core.store.api.type.NamedDescriptor;

/**
 * Defines a common base descriptor for all members of a
 * {@link PackageDescriptor}s.
 */
public interface PackageMemberDescriptor extends FullQualifiedNameDescriptor, NamedDescriptor {
}
