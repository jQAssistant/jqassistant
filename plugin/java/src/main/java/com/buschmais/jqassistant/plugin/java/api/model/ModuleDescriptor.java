package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Label("Module")
public interface ModuleDescriptor extends ClassFileDescriptor {

    String getVersion();

    void setVersion(String version);

    Boolean isOpen();

    void setOpen(Boolean open);

    @Relation("DECLARES_MAIN_CLASS")
    TypeDescriptor getMainClass();

    void setMainClass(TypeDescriptor mainClassType);

    /**
     * Find a contained or required {@link ModuleDescriptor} for the given module name and version in the dependencies of the current artifact
     *
     * @param artifact
     *     The artifact.
     * @param moduleName
     *     the module name.
     * @param version
     *     the module version.
     * @return The {@link ModuleDescriptor} or <code>null</code>
     */
    @ResultOf
    @Cypher("MATCH (artifact:Artifact)-[:DEPENDS_ON*..]->(:Artifact)-[:CONTAINS]->(:Module)-[:REQUIRES_MODULE*0..1]->(module:Java:Module{moduleName:$moduleName}) WHERE id(artifact)=$artifact and ($version is null or module.version=$version) RETURN module")
    ModuleDescriptor findModuleInDependencies(@Parameter("artifact") JavaArtifactFileDescriptor artifact, @Parameter("moduleName") String moduleName,
        @Parameter("version") String version);

    @Outgoing
    List<RequiresDescriptor> getRequiredModules();

    @Incoming
    List<RequiresDescriptor> getRequiringModules();

    @Relation("EXPORTS")
    List<ExportedPackageDescriptor> getExportedPackages();

    @Relation("USES")
    List<TypeDescriptor> getUsesServices();

    @Relation("PROVIDES")
    List<ProvidedServiceDescriptor> getProvidesServices();

    @Relation("OPENS")
    List<OpenPackageDescriptor> getOpenPackages();
}
