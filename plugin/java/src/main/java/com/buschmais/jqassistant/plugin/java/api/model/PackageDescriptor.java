package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.core.store.api.type.FileContainerDescriptor;
import com.buschmais.jqassistant.core.store.api.type.FullQualifiedNameDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes a Java package.
 */
@Label(value = "Package", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface PackageDescriptor extends PackageMemberDescriptor, FileContainerDescriptor {

}
