package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.jqassistant.plugin.java.api.model.Java.JavaLanguageElement.Package;

import com.buschmais.jqassistant.core.store.api.model.DirectoryDescriptor;

@Java(Package)
public interface PackageDirectoryDescriptor extends PackageDescriptor, DirectoryDescriptor {
}
