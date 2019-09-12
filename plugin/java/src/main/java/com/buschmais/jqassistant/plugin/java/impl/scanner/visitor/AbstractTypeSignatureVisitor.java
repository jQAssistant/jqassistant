package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;

import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Abstract implementation of a types signature visitor.
 */
public abstract class AbstractTypeSignatureVisitor extends SignatureVisitor {

    /**
     * The resolved types descriptor.
     */
    private TypeDescriptor resolvedTypeDescriptor;

    /**
     *
     */
    private TypeCache.CachedType containingType;

    private VisitorHelper visitorHelper;

    /**
     * Constructor.
     *
     * @param containingType
     *            The descriptor using the resolved types descriptor.
     * @param visitorHelper
     *            The {@link VisitorHelper}.
     */
    protected AbstractTypeSignatureVisitor(TypeCache.CachedType containingType, VisitorHelper visitorHelper) {
        super(VisitorHelper.OPCODE);
        this.containingType = containingType;
        this.visitorHelper = visitorHelper;
    }

    protected VisitorHelper getVisitorHelper() {
        return visitorHelper;
    }

    @Override
    public void visitClassType(String name) {
        resolvedTypeDescriptor = visitorHelper.resolveType(SignatureHelper.getObjectType(name), containingType).getTypeDescriptor();
    }

    @Override
    public void visitInnerClassType(String name) {
        String innerClassName = resolvedTypeDescriptor.getFullQualifiedName() + "$" + name;
        resolvedTypeDescriptor = visitorHelper.resolveType(innerClassName, containingType).getTypeDescriptor();
    }

    @Override
    public void visitEnd() {
        visitEnd(resolvedTypeDescriptor);
    }

    public abstract void visitEnd(TypeDescriptor typeDescriptor);
}
