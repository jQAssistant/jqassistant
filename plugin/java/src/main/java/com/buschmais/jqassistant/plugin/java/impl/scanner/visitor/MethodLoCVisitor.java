package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import java.util.HashSet;
import java.util.Set;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Visitor for calculating the LoC statistics of a method.
 */
class MethodLoCVisitor extends MethodVisitor {

    private final MethodDescriptor methodDescriptor;

    private Integer lineNumber = null;
    private Integer firstLineNumber = null;
    private Integer lastLineNumber = null;
    private Set<Integer> effectiveLines = new HashSet<>();

    public MethodLoCVisitor(MethodDescriptor methodDescriptor) {
        super(VisitorHelper.ASM_OPCODES);
        this.methodDescriptor = methodDescriptor;
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
        this.effectiveLines.add(line);
    }

    @Override
    public void visitEnd() {
        methodDescriptor.setFirstLineNumber(firstLineNumber);
        methodDescriptor.setLastLineNumber(lastLineNumber);
        if (!effectiveLines.isEmpty()) {
            methodDescriptor.setEffectiveLineCount(effectiveLines.size());
        }
    }
}
