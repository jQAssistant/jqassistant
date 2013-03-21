package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

public abstract class AbstractVisitor {

    private DependencyModel model;

    // common

    public void visitAttribute(final Attribute attr) {
    }

    public void visitEnd() {
    }

    protected DependencyModel getModel() {
        return model;
    }

    protected AbstractVisitor(DependencyModel model) {
        this.model = model;
    }

    protected String getInternalName(final String name) {
        if (name != null) {
            return getType(Type.getObjectType(name));
        }
        return null;
    }

    protected String getType(final String desc) {
        return getType(Type.getType(desc));
    }

    protected String getType(final Type t) {
        switch (t.getSort()) {
            case Type.ARRAY:
                return getType(t.getElementType());
            case Type.OBJECT:
                return t.getInternalName();
            default:
                return null;
        }
    }

    protected String getTypeSignature(final String signature) {
        if (signature != null) {
            SignatureVisitor signatureVisitor = new SignatureVisitor(model);
            new SignatureReader(signature).acceptType(signatureVisitor);
            return signatureVisitor.getSignatureClassName();
        }
        return null;
    }

}
