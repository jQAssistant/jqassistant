package com.buschmais.jqassistant.core.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.*;
import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;

public class ClassVisitor extends org.objectweb.asm.ClassVisitor {

    /**
     * The name of constructor methods.
     */
    private static final String CONSTRUCTOR_METHOD = "<init>";

    private TypeDescriptor typeDescriptor;
    private VisitorHelper visitorHelper;

    public ClassVisitor(VisitorHelper visitorHelper) {
        super(Opcodes.ASM4);
        this.visitorHelper = visitorHelper;
    }

    /**
     * Return the type descriptor created by visiting the class.
     *
     * @return The type descriptor.
     */
    public TypeDescriptor getTypeDescriptor() {
        return typeDescriptor;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        typeDescriptor = visitorHelper.getTypeDescriptor(name);
        JavaType javaType = getJavaType(access);
        typeDescriptor.setJavaType(javaType);
        if (hasFlag(access, Opcodes.ACC_ABSTRACT) && !hasFlag(access, Opcodes.ACC_INTERFACE)) {
            typeDescriptor.setAbstract(Boolean.TRUE);
        }
        setAccessModifier(access, typeDescriptor);
        if (signature == null) {
            if (superName != null) {
                typeDescriptor.setSuperClass(visitorHelper.getTypeDescriptor(superName));
            }
            for (int i = 0; interfaces != null && i < interfaces.length; i++) {
                typeDescriptor.getInterfaces().add(visitorHelper.getTypeDescriptor(interfaces[i]));
            }
        } else {
            new SignatureReader(signature).accept(new ClassSignatureVisitor(typeDescriptor, visitorHelper));
        }
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        FieldDescriptor fieldDescriptor = getFieldDescriptor(typeDescriptor, name, desc);
        typeDescriptor.getContains().add(fieldDescriptor);
        fieldDescriptor.setVolatile(hasFlag(access, Opcodes.ACC_VOLATILE));
        fieldDescriptor.setTransient(hasFlag(access, Opcodes.ACC_TRANSIENT));
        setAccessModifier(access, fieldDescriptor);
        if (signature == null) {
            visitorHelper.addDependency(fieldDescriptor, visitorHelper.getType((desc)));
        } else {
            new SignatureReader(signature).accept(new DependentTypeSignatureVisitor(fieldDescriptor, visitorHelper));
        }
        if (value instanceof org.objectweb.asm.Type) {
            visitorHelper.addDependency(fieldDescriptor, visitorHelper.getType((org.objectweb.asm.Type) value));
        }
        return new FieldVisitor(fieldDescriptor, visitorHelper);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodDescriptor methodDescriptor = getMethodDescriptor(typeDescriptor, name, desc);
        typeDescriptor.getContains().add(methodDescriptor);
        setAccessModifier(access, methodDescriptor);
        if (CONSTRUCTOR_METHOD.equals(name)) {
            methodDescriptor.setConstructor(Boolean.TRUE);
        }
        if (hasFlag(access, Opcodes.ACC_ABSTRACT)) {
            methodDescriptor.setAbstract(Boolean.TRUE);
        }
        if (hasFlag(access, Opcodes.ACC_NATIVE)) {
            methodDescriptor.setNative(Boolean.TRUE);
        }
        if (signature == null) {
            visitorHelper.addDependency(methodDescriptor, visitorHelper.getType(org.objectweb.asm.Type.getReturnType(desc)));
            org.objectweb.asm.Type[] types = org.objectweb.asm.Type.getArgumentTypes(desc);
            for (int i = 0; i < types.length; i++) {
                ParameterDescriptor parameterDescriptor = visitorHelper.getParameterDescriptor(methodDescriptor, i);
                visitorHelper.addDependency(parameterDescriptor, visitorHelper.getType(types[i]));
            }
        } else {
            new SignatureReader(signature).accept(new MethodSignatureVisitor(methodDescriptor, visitorHelper));
        }
        for (int i = 0; exceptions != null && i < exceptions.length; i++) {
            TypeDescriptor exception = visitorHelper.getTypeDescriptor(org.objectweb.asm.Type.getObjectType(exceptions[i]).getClassName());
            methodDescriptor.getDeclaredThrowables().add(exception);
        }
        return new MethodVisitor(methodDescriptor, visitorHelper);
    }

    private void setAccessModifier(final int access, AccessModifierDescriptor descriptor) {
        descriptor.setVisibility(getVisibility(access));
        if (hasFlag(access, Opcodes.ACC_SYNTHETIC)) {
            descriptor.setSynthetic(Boolean.TRUE);
        }
        if (hasFlag(access, Opcodes.ACC_FINAL)) {
            descriptor.setFinal(Boolean.TRUE);
        }
        if (hasFlag(access, Opcodes.ACC_STATIC)) {
            descriptor.setStatic(Boolean.TRUE);
        }
    }

    @Override
    public void visitSource(final String source, final String debug) {
    }

    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        addInnerClass(typeDescriptor, visitorHelper.getTypeDescriptor(name));
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
        addInnerClass(visitorHelper.getTypeDescriptor(owner), typeDescriptor);
    }

    // ---------------------------------------------

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        AnnotationValueDescriptor annotationDescriptor = visitorHelper.addAnnotation(typeDescriptor, visitorHelper.getType(desc));
        return new AnnotationVisitor(annotationDescriptor, visitorHelper);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
    }

    @Override
    public void visitEnd() {
    }

    protected MethodDescriptor getMethodDescriptor(TypeDescriptor typeDescriptor, String name, String desc) {
        return visitorHelper.getMethodDescriptor(typeDescriptor, visitorHelper.getMethodSignature(name, desc));
    }

    protected FieldDescriptor getFieldDescriptor(TypeDescriptor typeDescriptor, String name, String desc) {
        return visitorHelper.getFieldDescriptor(typeDescriptor, visitorHelper.getFieldSignature(name, desc));
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
     * Determine the types label to be applied to a class node.
     *
     * @param flags The access flags.
     * @return The types label.
     */
    private JavaType getJavaType(int flags) {
        if (hasFlag(flags, Opcodes.ACC_ANNOTATION)) {
            return JavaType.ANNOTATION;
        } else if (hasFlag(flags, Opcodes.ACC_ENUM)) {
            return JavaType.ENUM;
        } else if (hasFlag(flags, Opcodes.ACC_INTERFACE)) {
            return JavaType.INTERFACE;
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
