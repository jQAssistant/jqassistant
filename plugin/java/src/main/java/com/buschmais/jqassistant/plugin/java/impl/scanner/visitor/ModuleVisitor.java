package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;

import org.objectweb.asm.Opcodes;

public class ModuleVisitor extends org.objectweb.asm.ModuleVisitor {

    private final ModuleDescriptor moduleDescriptor;
    private final VisitorHelper visitorHelper;

    public ModuleVisitor(ModuleDescriptor moduleDescriptor, VisitorHelper visitorHelper) {
        super(VisitorHelper.ASM_OPCODES);
        this.moduleDescriptor = moduleDescriptor;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public void visitMainClass(String mainClass) {
        TypeDescriptor mainClassType = visitorHelper.resolveType(mainClass)
            .getTypeDescriptor();
        moduleDescriptor.setMainClass(mainClassType);
    }

    @Override
    public void visitRequire(String module, int access, String version) {
        JavaArtifactFileDescriptor artifactFileDescriptor = visitorHelper.getScannerContext()
            .peek(JavaArtifactFileDescriptor.class);
        ModuleDescriptor requiredModule = moduleDescriptor.findModuleInDependencies(artifactFileDescriptor, module, version);
        if (requiredModule == null) {
            requiredModule = visitorHelper.getStore().create(ModuleDescriptor.class);
            requiredModule.setModuleName(module);
            requiredModule.setVersion(version);
        }
        RequiresModuleDescriptor requiresModuleDescriptor = visitorHelper.getStore()
            .create(moduleDescriptor, RequiresModuleDescriptor.class, requiredModule);
        applyFlags(requiresModuleDescriptor, access);
    }

    @Override
    public void visitExport(String packaze, int access, String... modules) {
        // (:Module)-[:EXPORTS_PACKAGE]->(pe:ExportedPackage)
        // (pe)-[:OF_PACKAGE]->(:Package);
        // (pe)-[:TO_MODULE]->(:Module);
        ExportedPackageDescriptor exportedPackage = visitorHelper.getStore()
            .create(ExportedPackageDescriptor.class);
        exportedPackage.setPackage(resolvePackage(packaze));
    }

    @Override
    public void visitOpen(String packaze, int access, String... modules) {
        // (:Module)-[:OPENS_PACKAGE]->(op:OpenPackage)
        // (op)-[:OF_PACKAGE]->(:Package);
        // (op)-[:TO_MODULE]->(:Module);
    }

    @Override
    public void visitUse(String service) {
        TypeDescriptor serviceType = visitorHelper.resolveType(service)
            .getTypeDescriptor();
        moduleDescriptor.getUsesServices().add(serviceType);
    }

    @Override
    public void visitProvide(String service, String... providers) {
        // (:Module)-[:PROVIDES_SERVICE]->(ps:ProvidedService)
        // (providedService)-[:OF_TYPE]->(:Type)
        // (providedService)-[:WITH_PROVIDER]->(:Type)
    }

    private PackageDescriptor resolvePackage(String packaze) {
        ScannerContext scannerContext = visitorHelper.getScannerContext();
        return scannerContext.peek(TypeResolver.class)
            .require(packaze, PackageDescriptor.class, scannerContext);
    }

    private void applyFlags(AccessModifierDescriptor accessModifierDescriptor, int access) {
        if (visitorHelper.hasFlag(access, Opcodes.ACC_STATIC_PHASE)) {
            accessModifierDescriptor.setStatic(Boolean.TRUE);
        }
        if (visitorHelper.hasFlag(access, Opcodes.ACC_TRANSITIVE)) {
            accessModifierDescriptor.setTransitive(Boolean.TRUE);
        }
        if (visitorHelper.hasFlag(access, Opcodes.ACC_SYNTHETIC)) {
            accessModifierDescriptor.setSynthetic(Boolean.TRUE);
        }
        if (visitorHelper.hasFlag(access, Opcodes.ACC_MANDATED)) {
            accessModifierDescriptor.setMandated(Boolean.TRUE);
        }
    }
}
