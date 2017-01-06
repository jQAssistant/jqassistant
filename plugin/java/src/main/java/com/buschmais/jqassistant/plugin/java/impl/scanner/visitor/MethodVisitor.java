package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.plugin.java.api.model.AnnotationValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ParameterDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;

import java.util.HashSet;

public class MethodVisitor extends org.objectweb.asm.MethodVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(VisitorHelper.class);

    /**
     * Annotation indicating a synthetic parameter of a method.
     */
    private static final String JAVA_LANG_SYNTHETIC = "java.lang.Synthetic";

    private TypeCache.CachedType containingType;
    private MethodDescriptor methodDescriptor;
    private VisitorHelper visitorHelper;
    private DependentTypeSignatureVisitor dependentTypeSignatureVisitor;
    private int syntheticParameters = 0;
    private int cyclomaticComplexity = 1;
    private Integer lineNumber = null;
    private Integer firstLineNumber = null;
    private Integer lastLineNumber = null;
    private HashSet<Integer> effectiveLines = new HashSet<>();

    protected MethodVisitor(TypeCache.CachedType containingType, MethodDescriptor methodDescriptor, VisitorHelper visitorHelper, DependentTypeSignatureVisitor dependentTypeSignatureVisitor) {
        super(Opcodes.ASM5);
        this.containingType = containingType;
        this.methodDescriptor = methodDescriptor;
        this.visitorHelper = visitorHelper;
        this.dependentTypeSignatureVisitor = dependentTypeSignatureVisitor;
    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
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
        AnnotationValueDescriptor annotationDescriptor = visitorHelper.addAnnotation(containingType, parameterDescriptor, SignatureHelper.getType(desc));
        return new AnnotationVisitor(containingType, annotationDescriptor, visitorHelper);
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
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, boolean itf) {
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
        if (signature != null) {
            new SignatureReader(signature).accept(dependentTypeSignatureVisitor);
        }
    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitAnnotationDefault() {
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
        AnnotationValueDescriptor annotationDescriptor = visitorHelper.addAnnotation(containingType, methodDescriptor, SignatureHelper.getType(desc));
        return new AnnotationVisitor(containingType, annotationDescriptor, visitorHelper);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        if (this.lineNumber == null) {
            this.firstLineNumber = line;
            this.lastLineNumber = line;
        } else {
            this.firstLineNumber = Math.min(line, this.firstLineNumber);
            this.lastLineNumber = Math.max(line, this.lastLineNumber);
        }
        this.lineNumber = line;
        this.effectiveLines.add (line);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        cyclomaticComplexity++;
    }

    @Override
    public void visitEnd() {
        methodDescriptor.setCyclomaticComplexity(cyclomaticComplexity);
        if (firstLineNumber!=null) {
            methodDescriptor.setFirstLineNumber(firstLineNumber);
        }
        if (lastLineNumber!=null) {
            methodDescriptor.setLastLineNumber(lastLineNumber);
        }
        if (!effectiveLines.isEmpty()) {
            methodDescriptor.setEffectiveLineCount(effectiveLines.size());
        }
    }
}
