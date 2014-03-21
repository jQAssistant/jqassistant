package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.jqassistant.core.scanner.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.core.scanner.api.descriptor.NamedDescriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;

/**
 * Defines a common base descriptor for all members of a {@link PackageDescriptor}s.
 */
public interface PackageMemberDescriptor extends FullQualifiedNameDescriptor, NamedDescriptor, FileDescriptor {
}
