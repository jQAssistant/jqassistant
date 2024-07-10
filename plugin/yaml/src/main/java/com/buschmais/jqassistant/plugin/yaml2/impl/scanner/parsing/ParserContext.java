package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.Deque;
import java.util.LinkedList;

public class ParserContext {
    private LinkedList<ParsingContextType<? extends AbstractBaseNode>> stack = new LinkedList<>();
    private StreamNode rootNode;

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public <N extends AbstractBaseNode> void enter(ParsingContextType<N> type) {
        getStack().push(type);
    }

    private Deque<ParsingContextType<? extends AbstractBaseNode>> getStack() {
        return stack;
    }

    public ParsingContextType<? extends AbstractBaseNode> peek() {
        if (isEmpty()) {
            throw new IllegalStateException("No context available at the moment");
        }

        return getStack().peek();
    }

    public void leave() {
        if (isEmpty()) {
            throw new IllegalStateException("No context available at the moment");
        }

        getStack().pop();
    }

    boolean isInDocument() {
        return peek().getType() == ParsingContextType.Type.IN_DOCUMENT;
    }

    boolean isInMap() {
        return peek().getType() == ParsingContextType.Type.IN_MAP;
    }

    boolean isInSimpleKey() {
        return peek().getType() == ParsingContextType.Type.IN_KEY;
    }

    boolean isInComplexKey() {
        return peek().getType() == ParsingContextType.Type.IN_COMPLEX_KEY;
    }

    boolean isInKey() {
        return isInSimpleKey() || isInComplexKey() || isInAliasKey();
    }

    boolean isInAliasKey() {
        return peek().getType() == ParsingContextType.Type.IN_ALIAS_KEY;
    }

    boolean isInSequence() {
        return peek().getType() == ParsingContextType.Type.IN_SEQUENCE;
    }

    boolean isInStream() {
        return peek().getType() == ParsingContextType.Type.IN_STREAM;
    }

    boolean isNotInStream() {
        return !isInStream();
    }

    public <T extends AbstractBaseNode> ParsingContextType<T> getParent() {
        ParsingContextType<T> result = (ParsingContextType<T>) stack.get(1);
        return result;
    }

    public <T extends AbstractBaseNode> ParsingContextType<T> getCurrent() {
        return (ParsingContextType<T>) stack.getFirst();
    }

    public StreamNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(StreamNode node) {
        rootNode = node;
    }
}
