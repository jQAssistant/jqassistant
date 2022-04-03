package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import java.lang.invoke.LambdaMetafactory;

import com.buschmais.jqassistant.plugin.java.api.model.*;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics.AbstractBoundVisitor;

import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodVisitor extends org.objectweb.asm.MethodVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(VisitorHelper.class);

    private static final String LAMBDA_META_FACTORY = Type.getType(LambdaMetafactory.class).getInternalName();

    /**
     * Annotation indicating a synthetic parameter of a method.
     */
    private static final String JAVA_LANG_SYNTHETIC = "java.lang.Synthetic";
    private static final String THIS = "this";

    private TypeCache.CachedType containingType;
    private MethodDescriptor methodDescriptor;
    private VisitorHelper visitorHelper;
    private int syntheticParameters = 0;
    private Integer lineNumber = null;

    protected MethodVisitor(TypeCache.CachedType containingType, MethodDescriptor methodDescriptor, VisitorHelper visitorHelper) {
        super(VisitorHelper.ASM_OPCODES);
        this.containingType = containingType;
        this.methodDescriptor = methodDescriptor;
        this.visitorHelper = visitorHelper;
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
        ParameterDescriptor parameterDescriptor = visitorHelper.getParameterDescriptor(methodDescriptor, parameter - syntheticParameters);
        if (parameterDescriptor == null) {
            LOGGER.warn("Cannot find parameter with index " + (parameter - syntheticParameters) + " in method signature "
                    + containingType.getTypeDescriptor().getFullQualifiedName() + "#" + methodDescriptor.getSignature());
            return null;
        }
        return visitorHelper.addAnnotation(containingType, parameterDescriptor, SignatureHelper.getType(desc));
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        visitorHelper.resolveType(SignatureHelper.getObjectType(type), containingType);
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        String fieldSignature = SignatureHelper.getFieldSignature(name, desc);
        TypeCache.CachedType targetType = visitorHelper.resolveType(SignatureHelper.getObjectType(owner), containingType);
        FieldDescriptor fieldDescriptor = visitorHelper.getFieldDescriptor(targetType, fieldSignature);
        switch (opcode) {
        case Opcodes.GETFIELD:
        case Opcodes.GETSTATIC:
            visitorHelper.addReads(methodDescriptor, lineNumber, fieldDescriptor);
            break;
        case Opcodes.PUTFIELD:
        case Opcodes.PUTSTATIC:
            visitorHelper.addWrites(methodDescriptor, lineNumber, fieldDescriptor);
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
        TypeCache.CachedType targetType = visitorHelper.resolveType(SignatureHelper.getObjectType(owner), containingType);
        MethodDescriptor invokedMethodDescriptor = visitorHelper.getMethodDescriptor(targetType, methodSignature);
        visitorHelper.addInvokes(methodDescriptor, lineNumber, invokedMethodDescriptor);
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        if (cst instanceof Type) {
            visitorHelper.resolveType(SignatureHelper.getType((Type) cst), containingType);
        }
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        visitorHelper.resolveType(SignatureHelper.getType(desc), containingType);
    }

    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
        if (visitorHelper.getClassModelConfiguration().isMethodDeclaresVariable() && !THIS.equals(name)) {
            final VariableDescriptor variableDescriptor = visitorHelper.getVariableDescriptor(name, SignatureHelper.getFieldSignature(name, desc));
            if (signature == null) {
                TypeDescriptor type = visitorHelper.resolveType(SignatureHelper.getType((desc)), containingType).getTypeDescriptor();
                variableDescriptor.setType(type);
            } else {
                new SignatureReader(signature).accept(new AbstractBoundVisitor( visitorHelper, containingType) {
                    @Override
                    protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                        variableDescriptor.setType(rawTypeBound);
                        variableDescriptor.setGenericType(bound);
                    }
                });
            }
            methodDescriptor.getVariables().add(variableDescriptor);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new AnnotationDefaultVisitor(containingType, this.methodDescriptor, visitorHelper);
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        if (type != null) {
            String fullQualifiedName = SignatureHelper.getObjectType(type);
            visitorHelper.resolveType(fullQualifiedName, containingType);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        return visitorHelper.addAnnotation(containingType, methodDescriptor, SignatureHelper.getType(desc));
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        this.lineNumber = line;
    }

    @Override
    public void visitEnd() {
        visitorHelper.getTypeVariableResolver().pop();
    }
}
