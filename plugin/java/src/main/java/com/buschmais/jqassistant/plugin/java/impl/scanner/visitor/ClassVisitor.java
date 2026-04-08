package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.delegate.DelegatingMethodVisitor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics.AbstractBoundVisitor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics.ClassSignatureVisitor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics.MethodSignatureVisitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.tree.MethodNode;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.objectweb.asm.Type.getObjectType;

/**
 * A class visitor implementation.
 */
public class ClassVisitor extends org.objectweb.asm.ClassVisitor {

    private JavaByteCodeFileDescriptor javaByteCodeFileDescriptor;

    private final VisitorHelper visitorHelper;

    private ClassFileDescriptor classFileDescriptor;

    private String typeName;

    private String superTypeName;

    private String[] interfaceNames;

    /**
     * Constructor.
     *
     * @param javaByteCodeFileDescriptor
     *     The file descriptor to be migrated to a type descriptor.
     * @param visitorHelper
     *     The visitor helper.
     */
    public ClassVisitor(JavaByteCodeFileDescriptor javaByteCodeFileDescriptor, VisitorHelper visitorHelper) {
        super(VisitorHelper.ASM_OPCODES);
        this.javaByteCodeFileDescriptor = javaByteCodeFileDescriptor;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        this.typeName = name;
        this.superTypeName = superName;
        this.interfaceNames = interfaces;
        javaByteCodeFileDescriptor.setByteCodeVersion(version);
        Class<? extends JavaByteCodeFileDescriptor> javaByteCodeType = getClassFileType(access);
        if (ClassFileDescriptor.class.isAssignableFrom(javaByteCodeType)) {
            String fullQualifiedName = SignatureHelper.getObjectType(name);
            classFileDescriptor = visitorHelper.createType(fullQualifiedName, javaByteCodeFileDescriptor,
                (Class<? extends ClassFileDescriptor>) javaByteCodeType);
            visitorHelper.getTypeVariableResolver()
                .push();
            if (visitorHelper.hasFlag(access, Opcodes.ACC_ABSTRACT) && !visitorHelper.hasFlag(access, Opcodes.ACC_INTERFACE)) {
                classFileDescriptor.setAbstract(Boolean.TRUE);
            }
            setModifiers(access, classFileDescriptor);
            if (signature == null) {
                TypeDescriptor superClassType = visitorHelper.resolveType(SignatureHelper.getObjectType(superName), classFileDescriptor);
                classFileDescriptor.setSuperClass(superClassType);
                for (String anInterface : interfaces) {
                    TypeDescriptor interfaceType = visitorHelper.resolveType(SignatureHelper.getObjectType(anInterface), classFileDescriptor);
                    classFileDescriptor.getInterfaces()
                        .add(interfaceType);
                }
            } else {
                new SignatureReader(signature).accept(new ClassSignatureVisitor(classFileDescriptor, visitorHelper));
            }
        }
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        ScannerContext scannerContext = visitorHelper.getScannerContext();
        ModuleFileDescriptor moduleFileDescriptor = scannerContext.getStore()
            .addDescriptorType(javaByteCodeFileDescriptor, ModuleFileDescriptor.class);
        moduleFileDescriptor.setFullQualifiedName(name);
        moduleFileDescriptor.setVersion(version);
        moduleFileDescriptor.setOpen(visitorHelper.hasFlag(access, Opcodes.ACC_OPEN));
        moduleFileDescriptor.setSynthetic(visitorHelper.hasFlag(access, Opcodes.ACC_SYNTHETIC));
        return new ModuleVisitor(moduleFileDescriptor, visitorHelper);
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        classFileDescriptor.setStatic(true);
        classFileDescriptor.setFinal(true);
        return null;
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        final FieldDescriptor fieldDescriptor = visitorHelper.getFieldDescriptor(classFileDescriptor, SignatureHelper.getFieldSignature(name, desc));
        fieldDescriptor.setName(name);
        fieldDescriptor.setVolatile(visitorHelper.hasFlag(access, Opcodes.ACC_VOLATILE));
        fieldDescriptor.setTransient(visitorHelper.hasFlag(access, Opcodes.ACC_TRANSIENT));
        setModifiers(access, fieldDescriptor);
        if (signature == null) {
            TypeDescriptor typeDescriptor = visitorHelper.resolveType(SignatureHelper.getType((desc)), classFileDescriptor);
            fieldDescriptor.setType(typeDescriptor);
        } else {
            new SignatureReader(signature).accept(new AbstractBoundVisitor(visitorHelper, classFileDescriptor) {
                @Override
                protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                    fieldDescriptor.setType(rawTypeBound);
                    fieldDescriptor.setGenericType(bound);
                }
            });
        }
        if (value != null) {
            if (value instanceof org.objectweb.asm.Type) {
                visitorHelper.resolveType(SignatureHelper.getType((org.objectweb.asm.Type) value), classFileDescriptor);
            }
            PrimitiveValueDescriptor valueDescriptor = visitorHelper.getValueDescriptor(PrimitiveValueDescriptor.class);
            valueDescriptor.setValue(value);
            fieldDescriptor.setValue(valueDescriptor);
        }
        return new FieldVisitor(classFileDescriptor, fieldDescriptor, visitorHelper);
    }

    @Override
    public org.objectweb.asm.MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
        final String[] exceptions) {
        String methodSignature = SignatureHelper.getMethodSignature(name, desc);
        MethodDescriptor methodDescriptor = visitorHelper.getMethodDescriptor(classFileDescriptor, methodSignature);
        if (isLambda(name, access)) {
            visitorHelper.getStore()
                .addDescriptorType(methodDescriptor, LambdaMethodDescriptor.class);
        }
        visitorHelper.getTypeVariableResolver()
            .push();
        methodDescriptor.setName(name);
        setModifiers(access, methodDescriptor);
        if (visitorHelper.hasFlag(access, Opcodes.ACC_ABSTRACT)) {
            methodDescriptor.setAbstract(Boolean.TRUE);
        }
        if (visitorHelper.hasFlag(access, Opcodes.ACC_NATIVE)) {
            methodDescriptor.setNative(Boolean.TRUE);
        }
        if (signature == null) {
            String returnType = SignatureHelper.getType(org.objectweb.asm.Type.getReturnType(desc));
            methodDescriptor.setReturns(visitorHelper.resolveType(returnType, classFileDescriptor));
            org.objectweb.asm.Type[] types = org.objectweb.asm.Type.getArgumentTypes(desc);
            for (int i = 0; i < types.length; i++) {
                String parameterType = SignatureHelper.getType(types[i]);
                TypeDescriptor typeDescriptor = visitorHelper.resolveType(parameterType, classFileDescriptor);
                ParameterDescriptor parameterDescriptor = visitorHelper.addParameterDescriptor(methodDescriptor, i);
                parameterDescriptor.setType(typeDescriptor);
            }
        } else {
            new SignatureReader(signature).accept(new MethodSignatureVisitor(classFileDescriptor, methodDescriptor, visitorHelper));
        }
        for (int i = 0; exceptions != null && i < exceptions.length; i++) {
            TypeDescriptor exceptionType = visitorHelper.resolveType(SignatureHelper.getObjectType(exceptions[i]), classFileDescriptor);
            visitorHelper.getStore()
                .addDescriptorType(exceptionType, ThrowableDescriptor.class);
            ThrowsDescriptor throwsDescriptor = visitorHelper.getStore()
                .create(methodDescriptor, ThrowsDescriptor.class, exceptionType);
            throwsDescriptor.setDeclaration(true);
        }
        Type type = getObjectType(typeName);
        MethodNode methodNode = new MethodNode(access, name, desc, signature, exceptions);
        MethodDataFlowVerifier methodDataFlowVerifier = getMethodDataFlowVerifier(type);
        return new DelegatingMethodVisitor(new MethodVisitor(classFileDescriptor, methodDescriptor, visitorHelper),
            new MethodCatchesVisitor(methodDescriptor, visitorHelper), new MethodLoCVisitor(methodDescriptor), new MethodComplexityVisitor(methodDescriptor),
            new MethodDataFlowVisitor(type, methodDescriptor, methodNode, methodDataFlowVerifier, visitorHelper));
    }

    private MethodDataFlowVerifier getMethodDataFlowVerifier(Type type) {
        return new MethodDataFlowVerifier(type, InterfaceTypeDescriptor.class.isAssignableFrom(this.classFileDescriptor.getClass()),
            getObjectType(superTypeName), stream(interfaceNames).map(Type::getObjectType)
            .collect(toList()));
    }

    /**
     * Determine if a method represents a lambda expression.
     *
     * @param name
     *     The method name.
     * @param access
     *     The access modifiers.
     * @return <code>true</code> if the method represents a lambda expression.
     */
    private boolean isLambda(String name, int access) {
        if (!visitorHelper.hasFlag(access, Opcodes.ACC_SYNTHETIC)) {
            return false;
        }
        return (visitorHelper.hasFlag(access, Opcodes.ACC_STATIC) && name.startsWith("lambda$"));
    }

    private void setModifiers(final int access, AccessModifierDescriptor descriptor) {
        VisibilityModifier visibility = getVisibility(access);
        descriptor.setVisibility(visibility.getValue());
        if (visitorHelper.hasFlag(access, Opcodes.ACC_SYNTHETIC)) {
            descriptor.setSynthetic(Boolean.TRUE);
        }
        if (visitorHelper.hasFlag(access, Opcodes.ACC_FINAL)) {
            descriptor.setFinal(Boolean.TRUE);
        }
        if (visitorHelper.hasFlag(access, Opcodes.ACC_STATIC)) {
            descriptor.setStatic(Boolean.TRUE);
        }
    }

    @Override
    public void visitSource(final String source, final String debug) {
        this.javaByteCodeFileDescriptor.setSourceFileName(source);
    }

    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        if (outerName != null && classFileDescriptor != null) {
            String innerTypeName = SignatureHelper.getObjectType(name);
            // resolve inner type against outer cached type to add dependencies
            TypeDescriptor innerType = visitorHelper.resolveType(innerTypeName, classFileDescriptor);
            String outerTypeName = SignatureHelper.getObjectType(outerName);
            // set relation from outer to inner class only if outer type is the currently visited class
            if (outerTypeName.equals(classFileDescriptor.getFullQualifiedName())) {
                classFileDescriptor.getDeclaredInnerClasses()
                    .add(innerType);
            }
        }
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
        String outerTypeName = SignatureHelper.getObjectType(owner);
        TypeDescriptor typeDescriptor = visitorHelper.resolveType(outerTypeName, this.classFileDescriptor);
        typeDescriptor.getDeclaredInnerClasses()
            .add(this.classFileDescriptor);
        if (name != null) {
            String methodSignature = SignatureHelper.getMethodSignature(name, desc);
            MethodDescriptor methodDescriptor = visitorHelper.getMethodDescriptor(typeDescriptor, methodSignature);
            methodDescriptor.getDeclaredInnerClasses()
                .add(this.classFileDescriptor);
        }
    }

    // ---------------------------------------------

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        return visitorHelper.addAnnotation(classFileDescriptor, classFileDescriptor, SignatureHelper.getType(desc));
    }

    @Override
    public void visitEnd() {
        if (classFileDescriptor != null) {
            visitorHelper.flush();
            visitorHelper.getTypeVariableResolver()
                .pop();
        }
    }

    /**
     * Returns the AccessModifier for the flag pattern.
     *
     * @param flags
     *     the flags
     * @return the AccessModifier
     */
    private VisibilityModifier getVisibility(int flags) {
        if (visitorHelper.hasFlag(flags, Opcodes.ACC_PRIVATE)) {
            return VisibilityModifier.PRIVATE;
        } else {
            if (visitorHelper.hasFlag(flags, Opcodes.ACC_PROTECTED)) {
                return VisibilityModifier.PROTECTED;
            } else {
                if (visitorHelper.hasFlag(flags, Opcodes.ACC_PUBLIC)) {
                    return VisibilityModifier.PUBLIC;
                } else {
                    return VisibilityModifier.DEFAULT;
                }
            }
        }
    }

    /**
     * Determine the types label to be applied to a class node.
     *
     * @param flags
     *     The access flags.
     * @return The types label.
     */
    private Class<? extends JavaByteCodeFileDescriptor> getClassFileType(int flags) {
        if (visitorHelper.hasFlag(flags, Opcodes.ACC_ANNOTATION)) {
            return AnnotationTypeDescriptor.class;
        } else if (visitorHelper.hasFlag(flags, Opcodes.ACC_ENUM)) {
            return EnumTypeDescriptor.class;
        } else if (visitorHelper.hasFlag(flags, Opcodes.ACC_INTERFACE)) {
            return InterfaceTypeDescriptor.class;
        } else if (visitorHelper.hasFlag(flags, Opcodes.ACC_RECORD)) {
            return RecordTypeDescriptor.class;
        } else if (visitorHelper.hasFlag(flags, Opcodes.ACC_MODULE)) {
            return ModuleFileDescriptor.class;
        }
        return ClassTypeDescriptor.class;
    }
}
