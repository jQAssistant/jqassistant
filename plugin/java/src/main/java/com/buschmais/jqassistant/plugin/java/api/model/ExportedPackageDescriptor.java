package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("ExportedPackage")
public interface ExportedPackageDescriptor extends JavaDescriptor, Descriptor {

    @Relation("EXPORTED_PACKAGE")
    PackageDescriptor getPackage();

    void setPackage(PackageDescriptor packageDescriptor);

    @Relation("TO_MODULE")
    List<ModuleDescriptor> getToModules();

}
