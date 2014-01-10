package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.descriptor.NamedDescriptor;

/**
 * Defines a common base descriptor for all members of a {@link com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageDescriptor}s.
 */
public interface PackageMemberDescriptor extends FullQualifiedNameDescriptor, NamedDescriptor {
}
