package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;

import org.objectweb.asm.Opcodes;

import static com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper.getObjectType;
import static java.lang.Boolean.TRUE;

public class ModuleVisitor extends org.objectweb.asm.ModuleVisitor {

    private final ModuleDescriptor moduleDescriptor;
    private final VisitorHelper visitorHelper;

    private final JavaArtifactFileDescriptor artifactFileDescriptor;

    public ModuleVisitor(ModuleDescriptor moduleDescriptor, VisitorHelper visitorHelper) {
        super(VisitorHelper.ASM_OPCODES);
        this.moduleDescriptor = moduleDescriptor;
        this.visitorHelper = visitorHelper;
        this.artifactFileDescriptor = visitorHelper.getScannerContext()
            .peek(JavaArtifactFileDescriptor.class);
    }

    @Override
    public void visitMainClass(String mainClass) {
        TypeDescriptor mainClassType = visitorHelper.resolveType(mainClass)
            .getTypeDescriptor();
        moduleDescriptor.setMainClass(mainClassType);
    }

    @Override
    public void visitRequire(String module, int access, String version) {
        ModuleDescriptor requiredModule = resolveModule(artifactFileDescriptor, module, version);
        RequiresDescriptor requiresDescriptor = visitorHelper.getStore()
            .create(moduleDescriptor, RequiresDescriptor.class, requiredModule);
        applyFlags(requiresDescriptor, access);
    }

    @Override
    public void visitExport(String packaze, int access, String... modules) {
        // (:Module)-[:EXPORTS]->(pe:ExportedPackage)
        // (pe)-[:OF_PACKAGE]->(:Package);
        // (pe)-[:TO_MODULE]->(:Module);
        ExportedPackageDescriptor exportedPackage = packageToModule(packaze, access, modules, ExportedPackageDescriptor.class);
        moduleDescriptor.getExportedPackages()
            .add(exportedPackage);
    }

    @Override
    public void visitOpen(String packaze, int access, String... modules) {
        // (:Module)-[:OPENS]->(op:OpenPackage)
        // (op)-[:OF_PACKAGE]->(:Package);
        // (op)-[:TO_MODULE]->(:Module);
        OpenPackageDescriptor openPackage = packageToModule(packaze, access, modules, OpenPackageDescriptor.class);
        moduleDescriptor.getOpenPackages()
            .add(openPackage);
    }

    @Override
    public void visitUse(String service) {
        TypeDescriptor serviceType = visitorHelper.resolveType(getObjectType(service))
            .getTypeDescriptor();
        moduleDescriptor.getUsesServices()
            .add(serviceType);
    }

    @Override
    public void visitProvide(String service, String... providers) {
        // (:Module)-[:PROVIDES]->(ps:ProvidedService)
        // (providedService)-[:OF_TYPE]->(:Type)
        // (providedService)-[:WITH_PROVIDER]->(:Type)
        ProvidedServiceDescriptor providesService = visitorHelper.getStore()
            .create(ProvidedServiceDescriptor.class);
        providesService.setService(visitorHelper.resolveType(getObjectType(service))
            .getTypeDescriptor());
        for (String provider : providers) {
            providesService.getProviders()
                .add(visitorHelper.resolveType(getObjectType(provider))
                    .getTypeDescriptor());
        }
        moduleDescriptor.getProvidesServices()
            .add(providesService);
    }

    private ModuleDescriptor resolveModule(JavaArtifactFileDescriptor artifactFileDescriptor, String module, String version) {
        ModuleDescriptor requiredModule = moduleDescriptor.findModuleInDependencies(artifactFileDescriptor, module, version);
        if (requiredModule == null) {
            requiredModule = visitorHelper.getStore()
                .create(ModuleDescriptor.class);
            requiredModule.setName(module);
            requiredModule.setVersion(version);
            artifactFileDescriptor.getRequires().add(requiredModule);
        }
        return requiredModule;
    }

    private <D extends PackageToModuleDescriptor> D packageToModule(String packaze, int access, String[] modules, Class<D> descriptorType) {
        D descriptor = visitorHelper.getStore()
            .create(descriptorType);
        applyFlags(descriptor, access);
        descriptor.setPackage(resolvePackage(packaze));
        addToModules(descriptor, modules);
        return descriptor;
    }

    private void addToModules(PackageToModuleDescriptor packageToModuleDescriptor, String[] modules) {
        if (modules != null) {
            for (String module : modules) {
                ModuleDescriptor toModule = resolveModule(artifactFileDescriptor, module, null);
                packageToModuleDescriptor.getToModules()
                    .add(toModule);
            }
        }
    }

    private PackageDescriptor resolvePackage(String packaze) {
        ScannerContext scannerContext = visitorHelper.getScannerContext();
        return scannerContext.peek(TypeResolver.class)
            .require("/" + packaze, PackageDescriptor.class, scannerContext);
    }

    private void applyFlags(AccessModifierDescriptor accessModifierDescriptor, int access) {
        if (visitorHelper.hasFlag(access, Opcodes.ACC_STATIC_PHASE)) {
            accessModifierDescriptor.setStatic(TRUE);
        }
        if (visitorHelper.hasFlag(access, Opcodes.ACC_TRANSITIVE)) {
            accessModifierDescriptor.setTransitive(TRUE);
        }
        if (visitorHelper.hasFlag(access, Opcodes.ACC_SYNTHETIC)) {
            accessModifierDescriptor.setSynthetic(TRUE);
        }
        if (visitorHelper.hasFlag(access, Opcodes.ACC_MANDATED)) {
            accessModifierDescriptor.setMandated(TRUE);
        }
    }
}
