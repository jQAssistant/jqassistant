package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static org.objectweb.asm.Opcodes.*;

/**
 * Visitor for calculating the complexity of a method.
 */
class MethodComplexityVisitor extends MethodVisitor {

    private final MethodDescriptor methodDescriptor;

    @Builder
    @Getter
    @ToString
    private static class ExceptionHandler {

        private Label start;

        private Label end;

        private Label handler;

        private String type;
    }

    private static final Set<Integer> OPCODES_IF = unmodifiableSet(new HashSet<>(
        asList(IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT,
               IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE, IFNULL, IFNONNULL)));

    private Set<Label> whitelistLabels = new HashSet<>();

    private Map<Label, ExceptionHandler> exceptionHandlers = new HashMap<>();

    private boolean skip = false;

    private int complexity;

    public MethodComplexityVisitor(MethodDescriptor methodDescriptor) {
        super(VisitorHelper.ASM_OPCODES);
        this.methodDescriptor = methodDescriptor;
    }

    private void increment(int edges) {
        if (!skip) {
            this.complexity = complexity + edges;
        }
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        ExceptionHandler exceptionHandler = ExceptionHandler.builder().start(start).end(end).handler(handler).type(type).build();
        this.exceptionHandlers.put(handler, exceptionHandler);
        if (!isSyntheticExceptionHandler(exceptionHandler)) {
            increment(1);
        }
    }

    @Override
    public void visitLabel(Label label) {
        ExceptionHandler exceptionHandler = this.exceptionHandlers.get(label);
        if (exceptionHandler != null) {
            this.skip = isSyntheticExceptionHandler(exceptionHandler);
        }
        if (this.whitelistLabels.contains(label)) {
            this.skip = false;
        }
    }

    private boolean isSyntheticExceptionHandler(ExceptionHandler exceptionHandler) {
        if (exceptionHandler.getType() == null || "java/lang/Throwable".equals(exceptionHandler.getType())) {
            return true;
        }
        return this.exceptionHandlers.get(exceptionHandler.getStart()) != null;
    }

    @Override
    public void visitCode() {
        complexity = 1;
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == ATHROW) {
            increment(1);
        }
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if (!skip) {
            this.whitelistLabels.add(label);
        }
        if (OPCODES_IF.contains(opcode)) {
            increment(1);
        }
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        increment(labels.length + 1);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        increment(labels.length + 1);
    }

    @Override
    public void visitEnd() {
        this.methodDescriptor.setCyclomaticComplexity(complexity);
    }
}
