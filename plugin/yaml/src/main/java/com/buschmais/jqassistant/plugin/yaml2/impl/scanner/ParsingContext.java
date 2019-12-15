package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.util.Deque;
import java.util.LinkedList;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;

class ParsingContext {
    private LinkedList<ContextType<? extends YMLDescriptor>> stack = new LinkedList<>();

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public <D extends YMLDescriptor> void enter(ContextType<D> type) {
        getStack().push(type);
    }

    private Deque<ContextType<? extends YMLDescriptor>> getStack() {
        return stack;
    }

    public ContextType<? extends YMLDescriptor> peek() {
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

    boolean isInKey() {
        return peek().getType() == ContextType.Type.IN_KEY;
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

    public <T extends YMLDescriptor> ContextType<T> getAncestor(ContextType.Ancestor ancestor) {
        ContextType<T> result = (ContextType<T>) stack.get(ancestor.getOffset());
        return result;
    }

    public <T extends YMLDescriptor> ContextType<T> getCurrent() {
        return (ContextType<T>) stack.getFirst();

    }
}
