package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

import org.objectweb.asm.signature.SignatureVisitor;

public class ClassSignatureVisitor extends AbstractGenericDeclarationVisitor<ClassFileDescriptor> {

    public ClassSignatureVisitor(TypeCache.CachedType<? extends ClassFileDescriptor> cachedType, VisitorHelper visitorHelper) {
        super(visitorHelper, cachedType.getTypeDescriptor(), cachedType);
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return new AbstractBoundVisitor(visitorHelper, containingType) {
            @Override
            protected void apply(BoundDescriptor bound) {
                descriptor.setSuperClass(bound.getRawType());
                descriptor.setGenericSuperClass(bound);
            }
        };
    }

    @Override
    public SignatureVisitor visitInterface() {
        return new AbstractBoundVisitor(visitorHelper, containingType) {
            @Override
            protected void apply(BoundDescriptor bound) {
                descriptor.getInterfaces().add(bound.getRawType());
                descriptor.getGenericInterfaces().add(bound);
            }
        };
    }
}
