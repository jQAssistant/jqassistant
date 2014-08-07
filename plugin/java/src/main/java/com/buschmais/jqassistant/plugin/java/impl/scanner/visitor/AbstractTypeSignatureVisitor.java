package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

import com.buschmais.jqassistant.core.store.api.type.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;

/**
 * Abstract implementation of a types signature visitor.
 */
public abstract class AbstractTypeSignatureVisitor<T extends Descriptor> extends SignatureVisitor {

    /**
     * The resolved types descriptor.
     */
    private TypeDescriptor resolvedTypeDescriptor;

    /**
     * The descriptor using the resolved types descriptor.
     */
    private T usingDescriptor;

    private VisitorHelper visitorHelper;

    /**
     * Constructor.
     * 
     * @param usingDescriptor
     *            The descriptor using the resolved types descriptor.
     * @param visitorHelper
     *            The {@link VisitorHelper}.
     */
    protected AbstractTypeSignatureVisitor(T usingDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM5);
        this.usingDescriptor = usingDescriptor;
        this.visitorHelper = visitorHelper;
    }

    protected T getUsingDescriptor() {
        return usingDescriptor;
    }

    protected VisitorHelper getVisitorHelper() {
        return visitorHelper;
    }

    @Override
    public void visitClassType(String name) {
        resolvedTypeDescriptor = visitorHelper.getTypeDescriptor(SignatureHelper.getObjectType(name));
    }

    @Override
    public void visitInnerClassType(String name) {
        String innerClassName = resolvedTypeDescriptor.getFullQualifiedName() + "$" + name;
        resolvedTypeDescriptor = visitorHelper.getTypeDescriptor(innerClassName);
    }

    @Override
    public void visitEnd() {
        visitEnd(resolvedTypeDescriptor);
    }

    public abstract void visitEnd(TypeDescriptor typeDescriptor);
}
