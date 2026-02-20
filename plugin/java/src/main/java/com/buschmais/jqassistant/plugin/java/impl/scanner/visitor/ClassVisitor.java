package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
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

    private final FileDescriptor fileDescriptor;

    private final VisitorHelper visitorHelper;

    private TypeCache.CachedType<? extends ClassFileDescriptor> cachedType;

    private JavaByteCodeDescriptor javaByteCodeDescriptor;

    private String sourceFileName;

    private String typeName;

    private String superTypeName;

    private String[] interfaceNames;

    private int byteCodeVersion;

    /**
     * Constructor.
     *
     * @param fileDescriptor
     *     The file descriptor to be migrated to a type descriptor.
     * @param visitorHelper
     *     The visitor helper.
     */
    public ClassVisitor(FileDescriptor fileDescriptor, VisitorHelper visitorHelper) {
        super(VisitorHelper.ASM_OPCODES);
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
        this.typeName = name;
        this.superTypeName = superName;
        this.interfaceNames = interfaces;
        this.byteCodeVersion = version;
        Class<? extends JavaByteCodeDescriptor> javaByteCodeType = getJavaByteCodeType(access);
        if (ClassFileDescriptor.class.isAssignableFrom(javaByteCodeType)) {
            String fullQualifiedName = SignatureHelper.getObjectType(name);
            cachedType = visitorHelper.createType(fullQualifiedName, fileDescriptor, (Class<? extends ClassFileDescriptor>) javaByteCodeType);
            visitorHelper.getTypeVariableResolver()
                .push();
            ClassFileDescriptor classFileDescriptor = cachedType.getTypeDescriptor();
            this.javaByteCodeDescriptor = classFileDescriptor;
            if (visitorHelper.hasFlag(access, Opcodes.ACC_ABSTRACT) && !visitorHelper.hasFlag(access, Opcodes.ACC_INTERFACE)) {
                classFileDescriptor.setAbstract(Boolean.TRUE);
            }
            setModifiers(access, classFileDescriptor);
            if (signature == null) {
                TypeDescriptor superClassType = visitorHelper.resolveType(SignatureHelper.getObjectType(superName), cachedType)
                    .getTypeDescriptor();
                classFileDescriptor.setSuperClass(superClassType);
                for (int i = 0; i < interfaces.length; i++) {
                    TypeDescriptor interfaceType = visitorHelper.resolveType(SignatureHelper.getObjectType(interfaces[i]), cachedType)
                        .getTypeDescriptor();
                    classFileDescriptor.getInterfaces()
                        .add(interfaceType);
                }
            } else {
                new SignatureReader(signature).accept(new ClassSignatureVisitor(cachedType, visitorHelper));
            }
        }
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        ScannerContext scannerContext = visitorHelper.getScannerContext();
        ModuleDescriptor moduleDescriptor = scannerContext.getStore()
            .addDescriptorType(fileDescriptor, ModuleDescriptor.class);
        moduleDescriptor.setFullQualifiedName(name);
        moduleDescriptor.setVersion(version);
        moduleDescriptor.setOpen(visitorHelper.hasFlag(access, Opcodes.ACC_OPEN));
        moduleDescriptor.setSynthetic(visitorHelper.hasFlag(access, Opcodes.ACC_SYNTHETIC));
        this.javaByteCodeDescriptor = moduleDescriptor;
        return new ModuleVisitor(moduleDescriptor, visitorHelper);
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        if (cachedType == null) {
            return null;
        }
        cachedType.getTypeDescriptor()
            .setStatic(true);
        cachedType.getTypeDescriptor()
            .setFinal(true);
        return null;
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        if (cachedType == null) {
            return null;
        }
        final FieldDescriptor fieldDescriptor = visitorHelper.getFieldDescriptor(cachedType, SignatureHelper.getFieldSignature(name, desc));
        fieldDescriptor.setName(name);
        fieldDescriptor.setVolatile(visitorHelper.hasFlag(access, Opcodes.ACC_VOLATILE));
        fieldDescriptor.setTransient(visitorHelper.hasFlag(access, Opcodes.ACC_TRANSIENT));
        setModifiers(access, fieldDescriptor);
        if (signature == null) {
            TypeDescriptor typeDescriptor = visitorHelper.resolveType(SignatureHelper.getType((desc)), cachedType)
                .getTypeDescriptor();
            fieldDescriptor.setType(typeDescriptor);
        } else {
            new SignatureReader(signature).accept(new AbstractBoundVisitor(visitorHelper, cachedType) {
                @Override
                protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                    fieldDescriptor.setType(rawTypeBound);
                    fieldDescriptor.setGenericType(bound);
                }
            });
        }
        if (value != null) {
            if (value instanceof org.objectweb.asm.Type) {
                visitorHelper.resolveType(SignatureHelper.getType((org.objectweb.asm.Type) value), cachedType);
            }
            PrimitiveValueDescriptor valueDescriptor = visitorHelper.getValueDescriptor(PrimitiveValueDescriptor.class);
            valueDescriptor.setValue(value);
            fieldDescriptor.setValue(valueDescriptor);
        }
        return new FieldVisitor(cachedType, fieldDescriptor, visitorHelper);
    }

    @Override
    public org.objectweb.asm.MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
        final String[] exceptions) {
        if (cachedType == null) {
            return null;
        }
        String methodSignature = SignatureHelper.getMethodSignature(name, desc);
        MethodDescriptor methodDescriptor = visitorHelper.getMethodDescriptor(cachedType, methodSignature);
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
            methodDescriptor.setReturns(visitorHelper.resolveType(returnType, cachedType)
                .getTypeDescriptor());
            org.objectweb.asm.Type[] types = org.objectweb.asm.Type.getArgumentTypes(desc);
            for (int i = 0; i < types.length; i++) {
                String parameterType = SignatureHelper.getType(types[i]);
                TypeDescriptor typeDescriptor = visitorHelper.resolveType(parameterType, cachedType)
                    .getTypeDescriptor();
                ParameterDescriptor parameterDescriptor = visitorHelper.addParameterDescriptor(methodDescriptor, i);
                parameterDescriptor.setType(typeDescriptor);
            }
        } else {
            new SignatureReader(signature).accept(new MethodSignatureVisitor(cachedType, methodDescriptor, visitorHelper));
        }
        for (int i = 0; exceptions != null && i < exceptions.length; i++) {
            TypeDescriptor exceptionType = visitorHelper.resolveType(SignatureHelper.getObjectType(exceptions[i]), cachedType)
                .getTypeDescriptor();
            visitorHelper.getStore()
                .addDescriptorType(exceptionType, ThrowableDescriptor.class);
            ThrowsDescriptor throwsDescriptor = visitorHelper.getStore()
                .create(methodDescriptor, ThrowsDescriptor.class, exceptionType);
            throwsDescriptor.setDeclaration(true);
        }
        Type type = getObjectType(typeName);
        MethodNode methodNode = new MethodNode(access, name, desc, signature, exceptions);
        MethodDataFlowVerifier methodDataFlowVerifier = getMethodDataFlowVerifier(type);
        return new DelegatingMethodVisitor(new MethodVisitor(cachedType, methodDescriptor, visitorHelper),
            new MethodCatchesVisitor(methodDescriptor, visitorHelper), new MethodLoCVisitor(methodDescriptor), new MethodComplexityVisitor(methodDescriptor),
            new MethodDataFlowVisitor(type, methodDescriptor, methodNode, methodDataFlowVerifier, visitorHelper));
    }

    private MethodDataFlowVerifier getMethodDataFlowVerifier(Type type) {
        return new MethodDataFlowVerifier(type, InterfaceTypeDescriptor.class.isAssignableFrom(this.cachedType.getTypeDescriptor()
            .getClass()), getObjectType(superTypeName), stream(interfaceNames).map(Type::getObjectType)
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
        this.sourceFileName = source;
    }

    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        if (cachedType == null) {
            return;
        }
        String fullQualifiedName = cachedType.getTypeDescriptor()
            .getFullQualifiedName();
        // innerName always represents the name of the inner class
        String innerTypeName = SignatureHelper.getObjectType(name);
        TypeDescriptor innerType = visitorHelper.resolveType(innerTypeName, cachedType)
            .getTypeDescriptor();
        // set relation only if outerName is current class
        if (outerName != null) {
            String outerTypeName = SignatureHelper.getObjectType(outerName);
            if (fullQualifiedName.equals(outerTypeName)) {
                cachedType.getTypeDescriptor()
                    .getDeclaredInnerClasses()
                    .add(innerType);
            }
        }
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
        if (cachedType == null) {
            return;
        }
        String outerTypeName = SignatureHelper.getObjectType(owner);
        TypeCache.CachedType<?> cachedOuterType = visitorHelper.resolveType(outerTypeName, this.cachedType);
        TypeDescriptor innerType = this.cachedType.getTypeDescriptor();
        cachedOuterType.getTypeDescriptor()
            .getDeclaredInnerClasses()
            .add(innerType);
        if (name != null) {
            String methodSignature = SignatureHelper.getMethodSignature(name, desc);
            MethodDescriptor methodDescriptor = visitorHelper.getMethodDescriptor(cachedOuterType, methodSignature);
            methodDescriptor.getDeclaredInnerClasses()
                .add(innerType);
        }
    }

    // ---------------------------------------------

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        if (cachedType == null) {
            return null;
        }
        return visitorHelper.addAnnotation(cachedType, cachedType.getTypeDescriptor(), SignatureHelper.getType(desc));
    }

    @Override
    public void visitEnd() {
        javaByteCodeDescriptor.setByteCodeVersion(byteCodeVersion);
        javaByteCodeDescriptor.setSourceFileName(sourceFileName);
        if (cachedType != null) {
            visitorHelper.storeDependencies(cachedType);
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
    private Class<? extends JavaByteCodeDescriptor> getJavaByteCodeType(int flags) {
        if (visitorHelper.hasFlag(flags, Opcodes.ACC_ANNOTATION)) {
            return AnnotationTypeDescriptor.class;
        } else if (visitorHelper.hasFlag(flags, Opcodes.ACC_ENUM)) {
            return EnumTypeDescriptor.class;
        } else if (visitorHelper.hasFlag(flags, Opcodes.ACC_INTERFACE)) {
            return InterfaceTypeDescriptor.class;
        } else if (visitorHelper.hasFlag(flags, Opcodes.ACC_RECORD)) {
            return RecordTypeDescriptor.class;
        } else if (visitorHelper.hasFlag(flags, Opcodes.ACC_MODULE)) {
            return ModuleDescriptor.class;
        }
        return ClassTypeDescriptor.class;
    }
}
