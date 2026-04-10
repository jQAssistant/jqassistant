package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import java.lang.invoke.LambdaMetafactory;

import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics.AbstractBoundVisitor;

import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodVisitor extends org.objectweb.asm.MethodVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodVisitor.class);

    private static final String LAMBDA_META_FACTORY = Type.getType(LambdaMetafactory.class)
        .getInternalName();

    /**
     * Annotation indicating a synthetic parameter of a method.
     */
    private static final String JAVA_LANG_SYNTHETIC = "java.lang.Synthetic";
    private static final String THIS = "this";

    private final TypeClassFileDescriptor containingType;
    private final MethodDescriptor methodDescriptor;
    private final ClassFileVisitorContext classFileVisitorContext;

    private int syntheticParameters = 0;
    private Integer lineNumber = null;

    protected MethodVisitor(TypeClassFileDescriptor containingType, MethodDescriptor methodDescriptor, ClassFileVisitorContext classFileVisitorContext) {
        super(ClassFileVisitorContext.ASM_OPCODES);
        this.containingType = containingType;
        this.methodDescriptor = methodDescriptor;
        this.classFileVisitorContext = classFileVisitorContext;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        String annotationType = SignatureHelper.getType(desc);
        if (JAVA_LANG_SYNTHETIC.equals(annotationType)) {
            // Ignore synthetic parameters add the start of the signature, i.e.
            // determine the number of synthetic parameters
            syntheticParameters = Math.max(syntheticParameters, parameter + 1);
            return null;
        }
        ParameterDescriptor parameterDescriptor = classFileVisitorContext.getParameterDescriptor(methodDescriptor, parameter - syntheticParameters);
        if (parameterDescriptor == null) {
            LOGGER.warn("Cannot find parameter with index {} in method signature {}#{}", parameter - syntheticParameters, methodDescriptor.getDeclaringType()
                .getFullQualifiedName(), methodDescriptor.getSignature());
            return null;
        }
        return classFileVisitorContext.addAnnotation(parameterDescriptor, SignatureHelper.getType(desc));
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        classFileVisitorContext.resolveType(SignatureHelper.getObjectType(type));
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        String fieldSignature = SignatureHelper.getFieldSignature(name, desc);
        TypeDescriptor targetType = classFileVisitorContext.resolveType(SignatureHelper.getObjectType(owner));
        FieldDescriptor fieldDescriptor = classFileVisitorContext.getFieldDescriptor(targetType, fieldSignature);
        switch (opcode) {
        case Opcodes.GETFIELD:
        case Opcodes.GETSTATIC:
            classFileVisitorContext.addReads(methodDescriptor, lineNumber, fieldDescriptor);
            break;
        case Opcodes.PUTFIELD:
        case Opcodes.PUTSTATIC:
            classFileVisitorContext.addWrites(methodDescriptor, lineNumber, fieldDescriptor);
            break;
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        // Invocations of lambda methods and method references are implemented as dynamic invocation using the #LAMBDA_META_FACTORY.
        if (LAMBDA_META_FACTORY.equals(bootstrapMethodHandle.getOwner())) {
            Handle bootstrapMethodArgument = (Handle) bootstrapMethodArguments[1];
            invoke(bootstrapMethodArgument.getOwner(), bootstrapMethodArgument.getName(), bootstrapMethodArgument.getDesc());
        }
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, boolean itf) {
        invoke(owner, name, desc);
    }

    /**
     * Adds an invocation of the current method to the method specified by the parameters.
     *
     * @param owner
     *     The owner of the invoked method.
     * @param name
     *     The name of the invoked method
     * @param desc
     *     The raw signature of the invoked method.
     */
    private void invoke(String owner, String name, String desc) {
        String methodSignature = SignatureHelper.getMethodSignature(name, desc);
        TypeDescriptor targetType = classFileVisitorContext.resolveType(SignatureHelper.getObjectType(owner));
        MethodDescriptor invokedMethodDescriptor = classFileVisitorContext.getMethodDescriptor(targetType, methodSignature);
        classFileVisitorContext.addInvokes(methodDescriptor, lineNumber, invokedMethodDescriptor);
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        if (cst instanceof Type) {
            classFileVisitorContext.resolveType(SignatureHelper.getType((Type) cst));
        }
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        classFileVisitorContext.resolveType(SignatureHelper.getType(desc));
    }

    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
        if (classFileVisitorContext.getConfiguration()
            .isIncludeLocalVariables() && !THIS.equals(name)) {
            final VariableDescriptor variableDescriptor = classFileVisitorContext.getVariableDescriptor(name, SignatureHelper.getFieldSignature(name, desc));
            if (signature == null) {
                TypeDescriptor type = classFileVisitorContext.resolveType(SignatureHelper.getType((desc)));
                variableDescriptor.setType(type);
            } else {
                new SignatureReader(signature).accept(new AbstractBoundVisitor(classFileVisitorContext, containingType) {
                    @Override
                    protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                        variableDescriptor.setType(rawTypeBound);
                        variableDescriptor.setGenericType(bound);
                    }
                });
            }
            methodDescriptor.getVariables()
                .add(variableDescriptor);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new AnnotationDefaultVisitor(this.methodDescriptor, classFileVisitorContext);
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        if (type != null) {
            String fullQualifiedName = SignatureHelper.getObjectType(type);
            classFileVisitorContext.resolveType(fullQualifiedName);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        return classFileVisitorContext.addAnnotation(methodDescriptor, SignatureHelper.getType(desc));
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        this.lineNumber = line;
    }

    @Override
    public void visitEnd() {
        classFileVisitorContext.getTypeVariableResolver()
            .pop();
    }
}
