package com.buschmais.jqassistant.scanner.visitor;

import com.buschmais.jqassistant.scanner.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.store.api.model.descriptor.ClassDescriptor;
import org.objectweb.asm.signature.SignatureVisitor;

public class ClassSignatureVisitor extends DependentSignatureVisitor<ClassDescriptor> implements SignatureVisitor {

    protected ClassSignatureVisitor(ClassDescriptor classDescriptor, DescriptorResolverFactory resolverFactory) {
        super(classDescriptor, resolverFactory);
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return new DependentSignatureVisitor<ClassDescriptor>(getDependentDescriptor(), getResolverFactory()) {

            @Override
            public void visitClassType(String name) {
                getDependentDescriptor().setSuperClass(getClassDescriptor(name));
            }

            @Override
            public void visitInnerClassType(String name) {
                getDependentDescriptor().setSuperClass(getClassDescriptor(name));
            }

        };
    }

    @Override
    public SignatureVisitor visitInterface() {
        return new DependentSignatureVisitor<ClassDescriptor>(getDependentDescriptor(), getResolverFactory()) {

            @Override
            public void visitClassType(String name) {
                getDependentDescriptor().getInterfaces().add(getClassDescriptor(name));
            }

            @Override
            public void visitInnerClassType(String name) {
                getDependentDescriptor().getInterfaces().add(getClassDescriptor(name));
            }
        };
    }

}
