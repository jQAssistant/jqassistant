package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("OpenPackage")
public interface OpenPackageDescriptor extends JavaDescriptor, PackageToModuleDescriptor {

    @Override
    @Relation("OPEN_PACKAGE")
    PackageDescriptor getPackage();

}
