package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.MethodVisitor;
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
        try {
            Frame<BasicValue>[] frames = analyzer.analyze(typeName, methodNode);
            for (int i = 0; i < methodNode.instructions.size(); i++) {
                switch (methodNode.instructions.get(i)
                        .getOpcode()) {
                case ATHROW:
                    String throwableType = SignatureHelper.getType(frames[i].getStack(0)
                            .getType());
                    methodDescriptor.getThrows()
                            .add(visitorHelper.resolveType(throwableType)
                                    .getTypeDescriptor());
                    break;
                default:
                }
            }
        } catch (AnalyzerException e) {
            log.warn("Cannot analyze data flow of {}#{}.", typeName, methodNode.signature, e);
        }
    }
}
