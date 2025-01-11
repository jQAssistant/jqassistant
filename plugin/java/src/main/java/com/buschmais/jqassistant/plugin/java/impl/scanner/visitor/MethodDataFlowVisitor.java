package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ThrowableDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ThrowsDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import static com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper.ASM_OPCODES;
import static org.objectweb.asm.Opcodes.ATHROW;

@Slf4j
public class MethodDataFlowVisitor extends MethodVisitor {

    private final Type type;

    private final MethodDescriptor methodDescriptor;

    private final MethodNode methodNode;

    private final VisitorHelper visitorHelper;

    private final Analyzer<BasicValue> analyzer;

    MethodDataFlowVisitor(Type type, MethodDescriptor methodDescriptor, MethodNode methodNode, MethodDataFlowVerifier methodDataFlowVerifier,
        VisitorHelper visitorHelper) {
        super(ASM_OPCODES, methodNode);
        this.type = type;
        this.methodDescriptor = methodDescriptor;
        this.methodNode = methodNode;
        this.visitorHelper = visitorHelper;
        this.analyzer = new Analyzer<>(methodDataFlowVerifier);
    }

    @Override
    public void visitEnd() {
        try {
            Frame<BasicValue>[] frames = analyzer.analyze(type.getClassName(), methodNode);
            Integer lineNumber = null;
            for (int i = 0; i < methodNode.instructions.size(); i++) {
                AbstractInsnNode insnNode = methodNode.instructions.get(i);
                if (insnNode instanceof LineNumberNode) {
                    lineNumber = ((LineNumberNode) insnNode).line;
                } else {
                    Frame<BasicValue> frame = frames[i];
                    if (insnNode.getOpcode() == ATHROW) {
                        athrow(frame, lineNumber);
                    }
                }
            }
        } catch (AnalyzerException e) {
            log.warn("Cannot analyze data flow of '{}#{}': '{}'.", type.getClassName(), methodNode.signature, e.getMessage());
            log.debug("Analyzer exception details.", e);
        }
    }

    /**
     * Evaluates a thrown exception and creates a {@link ThrowsDescriptor}.
     *
     * @param frame
     *     The {@link Frame}.
     * @param lineNumber
     *     The line number (can be <code>null</code>)
     */
    private void athrow(Frame<BasicValue> frame, Integer lineNumber) {
        if (frame == null) {
            log.warn("Expected frame for athrow is null, skipping ({}#{}).", type.getClassName(), methodNode.signature);
        } else {
            String throwableType = SignatureHelper.getType(frame.getStack(0)
                .getType());
            TypeDescriptor typeDescriptor = visitorHelper.resolveType(throwableType)
                .getTypeDescriptor();
            Store store = visitorHelper.getStore();
            store.addDescriptorType(typeDescriptor, ThrowableDescriptor.class);
            ThrowsDescriptor throwsDescriptor = store.create(methodDescriptor, ThrowsDescriptor.class, typeDescriptor);
            throwsDescriptor.setDeclaration(false);
            if (lineNumber != null) {
                throwsDescriptor.setLineNumber(lineNumber);
            }
        }
    }

}
