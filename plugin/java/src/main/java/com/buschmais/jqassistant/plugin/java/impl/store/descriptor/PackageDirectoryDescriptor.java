package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import static com.buschmais.jqassistant.plugin.java.impl.store.descriptor.Java.JavaLanguageElement.Package;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

@Java(Package)
public interface PackageDirectoryDescriptor extends PackageDescriptor, FileDescriptor {
}
