package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.api.model.CatchesDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ThrowableDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * Visitor for methods catching exceptions.
 */
class MethodCatchesVisitor extends MethodVisitor {

    private final MethodDescriptor methodDescriptor;

    private final ClassFileVisitorContext classFileVisitorContext;

    private final List<TryCatchBlock> tryCatchBlocks = new ArrayList<>();

    private final Map<Label, Integer> lineNumbers = new HashMap<>();

    public MethodCatchesVisitor(MethodDescriptor methodDescriptor, ClassFileVisitorContext classFileVisitorContext) {
        super(ClassFileVisitorContext.ASM_OPCODES);
        this.methodDescriptor = methodDescriptor;
        this.classFileVisitorContext = classFileVisitorContext;
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        this.lineNumbers.put(start, line);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        if (type != null) {
            tryCatchBlocks.add(new TryCatchBlock(type, start, end, handler));
        }
    }

    @Override
    public void visitEnd() {
        for (TryCatchBlock tryCatchBlock : tryCatchBlocks) {
            String throwableType = SignatureHelper.getObjectType(tryCatchBlock.getType());
            TypeDescriptor typeDescriptor = classFileVisitorContext.resolveType(throwableType);
            classFileVisitorContext.getStore()
                .addDescriptorType(typeDescriptor, ThrowableDescriptor.class);
            CatchesDescriptor catchesDescriptor = classFileVisitorContext.getStore()
                .create(methodDescriptor, CatchesDescriptor.class, typeDescriptor);
            catchesDescriptor.setFirstLineNumber(lineNumbers.get(tryCatchBlock.getStart()));
            catchesDescriptor.setLastLineNumber(lineNumbers.get(tryCatchBlock.getEnd()));
        }
    }

    /**
     * Represents a try-block
     */
    @Getter
    @RequiredArgsConstructor
    @ToString
    private static final class TryCatchBlock {

        private final String type;

        private final Label start;

        private final Label end;

        private final Label handler;
    }
}

