package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.plugin.java.api.model.TypeClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.ClassFileVisitorContext;

import org.objectweb.asm.signature.SignatureVisitor;

public class ClassSignatureVisitor extends AbstractGenericDeclarationVisitor<TypeClassFileDescriptor> {

    public ClassSignatureVisitor(TypeClassFileDescriptor typeClassFileDescriptor, ClassFileVisitorContext classFileVisitorContext) {
        super(classFileVisitorContext, typeClassFileDescriptor, typeClassFileDescriptor);
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return new AbstractBoundVisitor(classFileVisitorContext, descriptor) {
            @Override
            protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                descriptor.setSuperClass(rawTypeBound);
                descriptor.setGenericSuperClass(bound);
            }
        };
    }

    @Override
    public SignatureVisitor visitInterface() {
        return new AbstractBoundVisitor(classFileVisitorContext, containingType) {
            @Override
            protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                descriptor.getInterfaces().add(rawTypeBound);
                descriptor.getGenericInterfaces().add(bound);
            }
        };
    }
}
