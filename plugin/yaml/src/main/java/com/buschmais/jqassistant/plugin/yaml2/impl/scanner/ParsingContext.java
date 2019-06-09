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
}
