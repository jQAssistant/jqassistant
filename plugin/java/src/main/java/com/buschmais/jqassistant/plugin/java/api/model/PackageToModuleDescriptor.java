package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface PackageToModuleDescriptor extends Descriptor, AccessModifierDescriptor {

    @Relation("OF_PACKAGE")
    PackageDescriptor getPackage();

    void setPackage(PackageDescriptor target);

    @Relation("TO_MODULE")
    List<ModuleDescriptor> getToModules();

}
