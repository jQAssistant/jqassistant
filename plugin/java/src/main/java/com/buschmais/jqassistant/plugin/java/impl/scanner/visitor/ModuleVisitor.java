package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;

import org.objectweb.asm.Opcodes;

import static com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper.getObjectType;
import static java.lang.Boolean.TRUE;

public class ModuleVisitor extends org.objectweb.asm.ModuleVisitor {

    private final ModuleClassFileDescriptor moduleClassFileDescriptor;
    private final ClassFileVisitorContext classFileVisitorContext;

    public ModuleVisitor(ModuleClassFileDescriptor moduleClassFileDescriptor, ClassFileVisitorContext classFileVisitorContext) {
        super(ClassFileVisitorContext.ASM_OPCODES);
        this.moduleClassFileDescriptor = moduleClassFileDescriptor;
        this.classFileVisitorContext = classFileVisitorContext;
    }

    @Override
    public void visitMainClass(String mainClass) {
        TypeDescriptor mainClassType = classFileVisitorContext.resolveType(mainClass);
        moduleClassFileDescriptor.setMainClass(mainClassType);
    }

    @Override
    public void visitRequire(String module, int access, String version) {
        ModuleDescriptor requiredModule = resolveModule(module, version);
        RequiresDescriptor requiresDescriptor = classFileVisitorContext.getStore()
            .create(moduleClassFileDescriptor, RequiresDescriptor.class, requiredModule);
        applyFlags(requiresDescriptor, access);
    }

    @Override
    public void visitExport(String packaze, int access, String... modules) {
        // (:Module)-[:EXPORTS]->(pe:ExportedPackage)
        // (pe)-[:OF_PACKAGE]->(:Package);
        // (pe)-[:TO_MODULE]->(:Module);
        ExportedPackageDescriptor exportedPackage = packageToModule(packaze, access, modules, ExportedPackageDescriptor.class);
        moduleClassFileDescriptor.getExportedPackages()
            .add(exportedPackage);
    }

    @Override
    public void visitOpen(String packaze, int access, String... modules) {
        // (:Module)-[:OPENS]->(op:OpenPackage)
        // (op)-[:OF_PACKAGE]->(:Package);
        // (op)-[:TO_MODULE]->(:Module);
        OpenPackageDescriptor openPackage = packageToModule(packaze, access, modules, OpenPackageDescriptor.class);
        moduleClassFileDescriptor.getOpenPackages()
            .add(openPackage);
    }

    @Override
    public void visitUse(String service) {
        TypeDescriptor serviceType = classFileVisitorContext.resolveType(getObjectType(service));
        moduleClassFileDescriptor.getUsesServices()
            .add(serviceType);
    }

    @Override
    public void visitProvide(String service, String... providers) {
        // (:Module)-[:PROVIDES]->(ps:ProvidedService)
        // (providedService)-[:OF_TYPE]->(:Type)
        // (providedService)-[:WITH_PROVIDER]->(:Type)
        ProvidedServiceDescriptor providesService = classFileVisitorContext.getStore()
            .create(ProvidedServiceDescriptor.class);
        providesService.setService(classFileVisitorContext.resolveType(getObjectType(service)));
        for (String provider : providers) {
            providesService.getProviders()
                .add(classFileVisitorContext.resolveType(getObjectType(provider)));
        }
        moduleClassFileDescriptor.getProvidesServices()
            .add(providesService);
    }

    private ModuleDescriptor resolveModule(String module, String version) {
        ScannerContext scannerContext = classFileVisitorContext.getScannerContext();
        return scannerContext.peek(TypeResolver.class)
            .requireModule(module, version, scannerContext);
    }

    private <D extends PackageToModuleDescriptor> D packageToModule(String packaze, int access, String[] modules, Class<D> descriptorType) {
        D descriptor = classFileVisitorContext.getStore()
            .create(descriptorType);
        applyFlags(descriptor, access);
        descriptor.setPackage(resolvePackage(packaze));
        addToModules(descriptor, modules);
        return descriptor;
    }

    private void addToModules(PackageToModuleDescriptor packageToModuleDescriptor, String[] modules) {
        if (modules != null) {
            for (String module : modules) {
                ModuleDescriptor toModule = resolveModule(module, null);
                packageToModuleDescriptor.getToModules()
                    .add(toModule);
            }
        }
    }

    private PackageDescriptor resolvePackage(String packaze) {
        ScannerContext scannerContext = classFileVisitorContext.getScannerContext();
        return scannerContext.peek(TypeResolver.class)
            .require("/" + packaze, PackageDescriptor.class, scannerContext);
    }

    private void applyFlags(AccessModifierDescriptor accessModifierDescriptor, int access) {
        if (classFileVisitorContext.hasFlag(access, Opcodes.ACC_STATIC_PHASE)) {
            accessModifierDescriptor.setStatic(TRUE);
        }
        if (classFileVisitorContext.hasFlag(access, Opcodes.ACC_TRANSITIVE)) {
            accessModifierDescriptor.setTransitive(TRUE);
        }
        if (classFileVisitorContext.hasFlag(access, Opcodes.ACC_SYNTHETIC)) {
            accessModifierDescriptor.setSynthetic(TRUE);
        }
        if (classFileVisitorContext.hasFlag(access, Opcodes.ACC_MANDATED)) {
            accessModifierDescriptor.setMandated(TRUE);
        }
    }
}
