package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ThrowsDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

import static com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper.ASM_OPCODES;
import static org.objectweb.asm.Opcodes.ATHROW;

@Slf4j
public class MethodDataFlowVisitor extends MethodVisitor {

    private final String typeName;

    private final MethodDescriptor methodDescriptor;

    private final MethodNode methodNode;

    private final VisitorHelper visitorHelper;

    private final Analyzer<BasicValue> analyzer = new Analyzer<>(new SimpleVerifier());

    MethodDataFlowVisitor(String typeName, MethodDescriptor methodDescriptor, MethodNode methodNode, VisitorHelper visitorHelper) {
        super(ASM_OPCODES, methodNode);
        this.typeName = typeName;
        this.methodDescriptor = methodDescriptor;
        this.methodNode = methodNode;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public void visitEnd() {
        Frame<BasicValue>[] frames;
        try {
            frames = analyzer.analyze(typeName, methodNode);
            Integer lineNumber = null;
            for (int i = 0; i < methodNode.instructions.size(); i++) {
                AbstractInsnNode insnNode = methodNode.instructions.get(i);
                if (insnNode instanceof LineNumberNode) {
                    lineNumber = ((LineNumberNode) insnNode).line;
                } else if (insnNode.getOpcode() == ATHROW) {
                    athrow(frames[i], lineNumber);
                }
            }
        } catch (AnalyzerException e) {
            log.warn("Cannot analyze data flow of {}#{}.", typeName, methodNode.signature, e);
        }
    }

    /**
     * Evaluates a thrown exception and creates a {@link ThrowsDescriptor}.
     *
     * @param frame
     *         The {@link Frame}.
     * @param lineNumber
     *         The line number (can be <code>null</code>)
     */
    private void athrow(Frame<BasicValue> frame, Integer lineNumber) {
        String throwableType = SignatureHelper.getType(frame.getStack(0)
                .getType());
        TypeDescriptor typeDescriptor = visitorHelper.resolveType(throwableType)
                .getTypeDescriptor();
        ThrowsDescriptor throwsDescriptor = visitorHelper.getStore()
                .create(methodDescriptor, ThrowsDescriptor.class, typeDescriptor);
        if (lineNumber != null) {
            throwsDescriptor.setLineNumber(lineNumber);
        }
    }
}
