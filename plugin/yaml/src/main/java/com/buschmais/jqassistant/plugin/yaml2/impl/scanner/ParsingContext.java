package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.util.Stack;

class ParsingContext {
    private Stack<ContextType> stack = new Stack<>();

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public void enter(ContextType type) {
        getStack().push(type);
    }

    private Stack<ContextType> getStack() {
        return stack;
    }

    public ContextType peek() {
        if (isEmpty()) {
            IllegalStateException e = new IllegalStateException("No context available at the moment");
            throw e;
        }

        return getStack().peek();
    }

    public void leave() {
        if (isEmpty()) {
            IllegalStateException e = new IllegalStateException("No context available at the moment");
            throw e;
        }

        getStack().pop();
    }

    boolean isInDocument() {
        return peek().getType() == ContextType.Type.IN_DOCUMENT;
    }

    boolean isInMap() {
        return peek().getType() == ContextType.Type.IN_MAP;
    }

    boolean isInSequence() {
        return peek().getType() == ContextType.Type.IN_SEQUENCE;
    }

    boolean isInStream() {
        return peek().getType() == ContextType.Type.IN_STREAM;
    }

    boolean isNotInStream() {
        return !isInStream();
    }

    public ContextType getAncestor(ContextType.Ancestor ancestor) {
        return stack.get(stack.size() - 1 - ancestor.getOffset());
    }
}
