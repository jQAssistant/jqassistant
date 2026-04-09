package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Label("Module")
public interface ModuleClassFileDescriptor extends ClassFileDescriptor, ModuleDescriptor, AccessModifierDescriptor {

    String getVersion();

    void setVersion(String version);

    Boolean isOpen();

    void setOpen(Boolean open);

    @Relation("DECLARES_MAIN_CLASS")
    TypeDescriptor getMainClass();

    void setMainClass(TypeDescriptor mainClassType);

    @Outgoing
    List<RequiresDescriptor> getRequiredModules();

    @Relation("EXPORTS")
    List<ExportedPackageDescriptor> getExportedPackages();

    @Relation("USES")
    List<TypeDescriptor> getUsesServices();

    @Relation("PROVIDES")
    List<ProvidedServiceDescriptor> getProvidesServices();

    @Relation("OPENS")
    List<OpenPackageDescriptor> getOpenPackages();
}
