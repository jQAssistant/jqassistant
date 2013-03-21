package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

import com.buschmais.jqassistant.store.model.ClassDescriptor;

public class ClassVisitor extends AbstractVisitor implements org.objectweb.asm.ClassVisitor {

    private ClassDescriptor classDescriptor;

    protected ClassVisitor(DependencyModel model) {
        super(model);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName,
            final String[] interfaces) {
        classDescriptor = getModel().getClassDescriptor(name);
        if (signature == null) {
            getModel().addDependency(classDescriptor, getInternalName(superName));
            addInternalNames(interfaces);
        } else {
            addSignature(signature);
        }
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        if (signature == null) {
            getModel().addDependency(classDescriptor, getType((desc)));
        } else {
            getModel().addDependency(classDescriptor, getTypeSignature(signature));
        }
        if (value instanceof Type) {
            getModel().addDependency(classDescriptor, getType((Type) value));
        }
        return new FieldVisitor(getModel(), classDescriptor);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        if (signature == null) {
            addMethodDesc(desc);
        } else {
            addSignature(signature);
        }
        addInternalNames(exceptions);
        return new MethodVisitor(getModel(), classDescriptor);
    }

    @Override
    public void visitSource(final String source, final String debug) {
    }

    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        // addName( outerName);
        // addName( innerName);
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
        // addName(owner);
        // addMethodDesc(desc);
    }

    // ---------------------------------------------

    private void addInternalNames(final String[] names) {
        for (int i = 0; names != null && i < names.length; i++) {
            getModel().addDependency(classDescriptor, getInternalName(names[i]));
        }
    }

    private void addMethodDesc(final String desc) {
        getModel().addDependency(classDescriptor, getType(Type.getReturnType(desc)));
        Type[] types = Type.getArgumentTypes(desc);
        for (int i = 0; i < types.length; i++) {
            getModel().addDependency(classDescriptor, getType(types[i]));
        }
    }

    private void addSignature(final String signature) {
        if (signature != null) {
            SignatureVisitor signatureVisitor = new SignatureVisitor(getModel());
            new SignatureReader(signature).accept(signatureVisitor);
            getModel().addDependency(classDescriptor, signatureVisitor.getSignatureClassName());
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        getModel().addDependency(classDescriptor, getType(desc));
        return new AnnotationVisitor(getModel(), classDescriptor);
    }

}
