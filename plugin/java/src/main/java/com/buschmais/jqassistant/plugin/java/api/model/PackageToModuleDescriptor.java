package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Abstract
public interface PackageToModuleDescriptor extends AccessModifierDescriptor, Descriptor {

    PackageDescriptor getPackage();

    void setPackage(PackageDescriptor target);

    @Relation("TO_MODULE")
    List<ModuleDescriptor> getToModules();

}
