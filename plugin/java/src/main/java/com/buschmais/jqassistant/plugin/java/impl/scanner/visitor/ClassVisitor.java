package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;

/**
 * A class visitor implementation.
 */
public class ClassVisitor extends org.objectweb.asm.ClassVisitor {

    private TypeCache.CachedType<? extends ClassFileDescriptor> cachedType;
    private FileDescriptor fileDescriptor;
    private VisitorHelper visitorHelper;

    /**
     * Constructor.
     * 
     * @param fileDescriptor
     *            The file descriptor to be migrated to a type descriptor.
     * @param visitorHelper
     *            The visitor helper.
     */
    public ClassVisitor(FileDescriptor fileDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM5);
        this.fileDescriptor = fileDescriptor;
        this.visitorHelper = visitorHelper;
    }

    /**
     * Return the type descriptor created by visiting the class.
     * 
     * @return The type descriptor.
     */
    public ClassFileDescriptor getTypeDescriptor() {
        return cachedType != null ? cachedType.getTypeDescriptor() : null;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        Class<? extends ClassFileDescriptor> javaType = getJavaType(access);
        cachedType = visitorHelper.createType(SignatureHelper.getObjectType(name), fileDescriptor, javaType);
        ClassFileDescriptor classFileDescriptor = cachedType.getTypeDescriptor();
        classFileDescriptor.setByteCodeVersion(version);
        if (hasFlag(access, Opcodes.ACC_ABSTRACT) && !hasFlag(access, Opcodes.ACC_INTERFACE)) {
            classFileDescriptor.setAbstract(Boolean.TRUE);
        }
        setModifiers(access, classFileDescriptor);
        if (signature == null) {
            if (superName != null) {
                TypeDescriptor superClassType = visitorHelper.resolveType(SignatureHelper.getObjectType(superName), cachedType).getTypeDescriptor();
                classFileDescriptor.setSuperClass(superClassType);
            }
            for (int i = 0; interfaces != null && i < interfaces.length; i++) {
                TypeDescriptor interfaceType = visitorHelper.resolveType(SignatureHelper.getObjectType(interfaces[i]), cachedType).getTypeDescriptor();
                classFileDescriptor.getInterfaces().add(interfaceType);
            }
        } else {
            new SignatureReader(signature).accept(new ClassSignatureVisitor(cachedType, visitorHelper));
        }
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        final FieldDescriptor fieldDescriptor = visitorHelper.getFieldDescriptor(cachedType, SignatureHelper.getFieldSignature(name, desc));
        fieldDescriptor.setName(name);
        fieldDescriptor.setVolatile(hasFlag(access, Opcodes.ACC_VOLATILE));
        fieldDescriptor.setTransient(hasFlag(access, Opcodes.ACC_TRANSIENT));
        setModifiers(access, fieldDescriptor);
        if (signature == null) {
            TypeDescriptor type = visitorHelper.resolveType(SignatureHelper.getType((desc)), cachedType).getTypeDescriptor();
            fieldDescriptor.setType(type);
        } else {
            new SignatureReader(signature).accept(new AbstractTypeSignatureVisitor<FieldDescriptor>(cachedType, visitorHelper) {
                @Override
                public SignatureVisitor visitArrayType() {
                    return new DependentTypeSignatureVisitor(cachedType, visitorHelper);
                }

                @Override
                public SignatureVisitor visitTypeArgument(char wildcard) {
                    return new DependentTypeSignatureVisitor(cachedType, visitorHelper);
                }

                @Override
                public SignatureVisitor visitSuperclass() {
                    return this;
                }

                @Override
                public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
                    fieldDescriptor.setType(resolvedTypeDescriptor);
                }
            });
        }
        if (value instanceof org.objectweb.asm.Type) {
            visitorHelper.resolveType(SignatureHelper.getType((org.objectweb.asm.Type) value), cachedType);
        }
        return new FieldVisitor(cachedType, fieldDescriptor, visitorHelper);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        MethodDescriptor methodDescriptor = visitorHelper.getMethodDescriptor(cachedType, SignatureHelper.getMethodSignature(name, desc));
        methodDescriptor.setName(name);
        setModifiers(access, methodDescriptor);
        if (hasFlag(access, Opcodes.ACC_ABSTRACT)) {
            methodDescriptor.setAbstract(Boolean.TRUE);
        }
        if (hasFlag(access, Opcodes.ACC_NATIVE)) {
            methodDescriptor.setNative(Boolean.TRUE);
        }
        if (signature == null) {
            String returnType = SignatureHelper.getType(org.objectweb.asm.Type.getReturnType(desc));
            methodDescriptor.setReturns(visitorHelper.resolveType(returnType, cachedType).getTypeDescriptor());
            org.objectweb.asm.Type[] types = org.objectweb.asm.Type.getArgumentTypes(desc);
            for (int i = 0; i < types.length; i++) {
                ParameterDescriptor parameterDescriptor = visitorHelper.addParameterDescriptor(methodDescriptor, i);
                String parameterType = SignatureHelper.getType(types[i]);
                parameterDescriptor.setType(visitorHelper.resolveType(parameterType, cachedType).getTypeDescriptor());
            }
        } else {
            new SignatureReader(signature).accept(new MethodSignatureVisitor(cachedType, methodDescriptor, visitorHelper));
        }
        for (int i = 0; exceptions != null && i < exceptions.length; i++) {
            TypeDescriptor exceptionType = visitorHelper.resolveType(SignatureHelper.getObjectType(exceptions[i]), cachedType).getTypeDescriptor();
            methodDescriptor.getDeclaredThrowables().add(exceptionType);
        }
        return new MethodVisitor(cachedType, methodDescriptor, visitorHelper);
    }

    private void setModifiers(final int access, AccessModifierDescriptor descriptor) {
        VisibilityModifier visibility = getVisibility(access);
        descriptor.setVisibility(visibility.getValue());
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
       cachedType.getTypeDescriptor().setSourceFileName(source);
    }

    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        addInnerClass(cachedType.getTypeDescriptor(), visitorHelper.resolveType(SignatureHelper.getObjectType(name), cachedType).getTypeDescriptor());
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
        addInnerClass(visitorHelper.resolveType(SignatureHelper.getObjectType(owner), cachedType).getTypeDescriptor(), cachedType.getTypeDescriptor());
    }

    // ---------------------------------------------

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        AnnotationValueDescriptor annotationDescriptor = visitorHelper.addAnnotation(cachedType, cachedType.getTypeDescriptor(), SignatureHelper.getType(desc));
        return new AnnotationVisitor(cachedType, annotationDescriptor, visitorHelper);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
    }

    @Override
    public void visitEnd() {
    }

    /**
     * Checks whether the value contains the flag.
     * 
     * @param value
     *            the value
     * @param flag
     *            the flag
     * @return <code>true</code> if (value & flag) == flag, otherwise
     *         <code>false</code>.
     */
    private boolean hasFlag(int value, int flag) {
        return (value & flag) == flag;
    }

    /**
     * Returns the AccessModifier for the flag pattern.
     * 
     * @param flags
     *            the flags
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
     * @param flags
     *            The access flags.
     * @return The types label.
     */
    private Class<? extends ClassFileDescriptor> getJavaType(int flags) {
        if (hasFlag(flags, Opcodes.ACC_ANNOTATION)) {
            return AnnotationTypeDescriptor.class;
        } else if (hasFlag(flags, Opcodes.ACC_ENUM)) {
            return EnumTypeDescriptor.class;
        } else if (hasFlag(flags, Opcodes.ACC_INTERFACE)) {
            return InterfaceTypeDescriptor.class;
        }
        return ClassTypeDescriptor.class;
    }

    /**
     * Adds an inner class relation.
     * 
     * @param outerClass
     *            The outer class.
     * @param innerClass
     *            The inner class.
     */
    private void addInnerClass(TypeDescriptor outerClass, TypeDescriptor innerClass) {
        if (!innerClass.equals(outerClass)) {
            outerClass.getDeclaredInnerClasses().add(innerClass);
        }
    }
}
