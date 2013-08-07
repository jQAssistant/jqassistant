package com.buschmais.jqassistant.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.*;
import com.buschmais.jqassistant.scanner.impl.resolver.DescriptorResolverFactory;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassVisitor extends AbstractVisitor implements org.objectweb.asm.ClassVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassVisitor.class);

    private TypeDescriptor typeDescriptor;
    private ArtifactDescriptor artifactDescriptor;

    public ClassVisitor(DescriptorResolverFactory resolverFactory) {
        this(resolverFactory, null);
    }

    public ClassVisitor(DescriptorResolverFactory resolverFactory, ArtifactDescriptor artifactDescriptor) {
        super(resolverFactory);
        this.artifactDescriptor = artifactDescriptor;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        typeDescriptor = getClassDescriptor(name);

        if (this.artifactDescriptor != null) {
            artifactDescriptor.getContains().add(typeDescriptor);
        }

        typeDescriptor.setJavaType(getJavaType(access));
        typeDescriptor.setAbstract(hasFlag(access, Opcodes.ACC_ABSTRACT) && !hasFlag(access, Opcodes.ACC_INTERFACE));
        setAccessModifier(access, typeDescriptor);

        if (signature == null) {
            if (superName != null) {
                typeDescriptor.setSuperClass(getClassDescriptor(superName));
            }
            for (int i = 0; interfaces != null && i < interfaces.length; i++) {
                typeDescriptor.getInterfaces().add(getClassDescriptor(interfaces[i]));
            }
        } else {
            new SignatureReader(signature).accept(new ClassSignatureVisitor(typeDescriptor, getResolverFactory()));
        }
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        FieldDescriptor fieldDescriptor = getFielDescriptor(typeDescriptor, name, desc);
        typeDescriptor.getContains().add(fieldDescriptor);
        fieldDescriptor.setVolatile(hasFlag(access, Opcodes.ACC_VOLATILE));
        fieldDescriptor.setTransient(hasFlag(access, Opcodes.ACC_TRANSIENT));
        setAccessModifier(access, fieldDescriptor);

        if (signature == null) {
            addDependency(fieldDescriptor, getType((desc)));
        } else {
            new SignatureReader(signature).accept(new DependentTypeSignatureVisitor(fieldDescriptor, getResolverFactory()));
        }
        if (value instanceof org.objectweb.asm.Type) {
            addDependency(fieldDescriptor, getType((org.objectweb.asm.Type) value));
        }
        return new FieldVisitor(fieldDescriptor, getResolverFactory());
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodDescriptor methodDescriptor = getMethodDescriptor(typeDescriptor, name, desc);
        typeDescriptor.getContains().add(methodDescriptor);
        methodDescriptor.setAbstract(hasFlag(access, Opcodes.ACC_ABSTRACT));
        methodDescriptor.setNative(hasFlag(access, Opcodes.ACC_NATIVE));
        setAccessModifier(access, methodDescriptor);

        if (signature == null) {
            addDependency(methodDescriptor, getType(org.objectweb.asm.Type.getReturnType(desc)));
            org.objectweb.asm.Type[] types = org.objectweb.asm.Type.getArgumentTypes(desc);
            for (int i = 0; i < types.length; i++) {
                addDependency(methodDescriptor, getType(types[i]));
            }
        } else {
            new SignatureReader(signature).accept(new MethodSignatureVisitor(methodDescriptor, getResolverFactory()));
        }
        for (int i = 0; exceptions != null && i < exceptions.length; i++) {
            TypeDescriptor exception = getClassDescriptor(org.objectweb.asm.Type.getObjectType(exceptions[i]).getClassName());
            methodDescriptor.getDeclaredThrowables().add(exception);
        }
        return new MethodVisitor(methodDescriptor, getResolverFactory());
    }

    private void setAccessModifier(final int access, AccessModifierDescriptor descriptor) {
        descriptor.setVisibility(getVisibility(access));
        descriptor.setFinal(hasFlag(access, Opcodes.ACC_FINAL));
        descriptor.setStatic(hasFlag(access, Opcodes.ACC_STATIC));
    }

    @Override
    public void visitSource(final String source, final String debug) {
    }

    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        addInnerClass(typeDescriptor, getClassDescriptor(name));
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
        addInnerClass(getClassDescriptor(owner), typeDescriptor);
    }

    // ---------------------------------------------

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        addAnnotation(typeDescriptor, getType(desc));
        return new AnnotationVisitor(typeDescriptor, getResolverFactory());
    }

    @Override
    public void visitAttribute(Attribute attribute) {
    }

    @Override
    public void visitEnd() {
    }

    protected MethodDescriptor getMethodDescriptor(TypeDescriptor typeDescriptor, String name, String desc) {
        MethodDescriptor methodDescriptor = getStore().createMethodDescriptor(typeDescriptor, getMethodSignature(name, desc));
        return methodDescriptor;
    }

    protected FieldDescriptor getFielDescriptor(TypeDescriptor typeDescriptor, String name, String desc) {
        FieldDescriptor fieldDescriptor = getStore().createFieldDescriptor(typeDescriptor, getFieldSignature(name, desc));
        return fieldDescriptor;
    }

    private String getMethodSignature(String name, String desc) {
        StringBuffer signature = new StringBuffer();
        String returnType = org.objectweb.asm.Type.getReturnType(desc).getClassName();
        if (returnType != null) {
            signature.append(returnType);
            signature.append(' ');
        }
        signature.append(name);
        signature.append('(');
        org.objectweb.asm.Type[] types = org.objectweb.asm.Type.getArgumentTypes(desc);
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                signature.append(',');
            }
            signature.append(types[i].getClassName());
        }
        signature.append(')');
        return signature.toString();
    }

    private String getFieldSignature(String name, String desc) {
        StringBuffer signature = new StringBuffer();
        String returnType = org.objectweb.asm.Type.getReturnType(desc).getClassName();
        signature.append(returnType);
        signature.append(' ');
        signature.append(name);
        return signature.toString();
    }

    /**
     * Checks whether the value contains the flag.
     *
     * @param value the value
     * @param flag  the flag
     * @return <code>true</code> if (value & flag) == flag, otherwise
     *         <code>false</code>.
     */
    private boolean hasFlag(int value, int flag) {
        return (value & flag) == flag;
    }

    /**
     * Returns the AccessModifier for the flag pattern.
     *
     * @param flags the flags
     * @return the AccessModifier
     */
    private VisibilityModifier getVisibility(int flags) {
        if (hasFlag(flags, Opcodes.ACC_PRIVATE)) {
            return VisibilityModifier.PRIVATE;
        } else if (hasFlag(flags, Opcodes.ACC_PROTECTED)) {
            return VisibilityModifier.PROTECTED;
        } else if (hasFlag(flags, Opcodes.ACC_PUBLIC)) {
            return VisibilityModifier.PUBLIC;
        } else {
            return VisibilityModifier.DEFAULT;
        }
    }

    /**
     * Determine the type label to be applied to a class node.
     *
     * @param flags The access flags.
     * @return The type label.
     */
    private JavaType getJavaType(int flags) {
        if (hasFlag(flags, Opcodes.ACC_INTERFACE)) {
            return JavaType.INTERFACE;
        } else if (hasFlag(flags, Opcodes.ACC_ANNOTATION)) {
            return JavaType.ANNOTATION;
        } else if (hasFlag(flags, Opcodes.ACC_ENUM)) {
            return JavaType.ENUM;
        }
        return JavaType.CLASS;
    }

    /**
     * Adds an inner class relation.
     *
     * @param outerClass The outer class.
     * @param innerClass The inner class.
     */
    private void addInnerClass(TypeDescriptor outerClass, TypeDescriptor innerClass) {
        if (!innerClass.equals(outerClass)) {
            outerClass.getContains().add(innerClass);
        }
    }

}
