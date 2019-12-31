package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.Deque;
import java.util.LinkedList;

public class ParserContext {
    private LinkedList<ParsingContextType<? extends ParseNode>> stack = new LinkedList<>();
    private StreamNode rootNode;
    //private AliasCache aliasCache = new AliasCache();

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    //public AliasCache getAliasCache() {
    //    return aliasCache;
    //}

    public <N extends ParseNode> void enter(ParsingContextType<N> type) {
        getStack().push(type);
    }

    private Deque<ParsingContextType<? extends ParseNode>> getStack() {
        return stack;
    }

    public ParsingContextType<? extends ParseNode> peek() {
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
        return peek().getType() == ParsingContextType.Type.IN_DOCUMENT;
    }

    boolean isInMap() {
        return peek().getType() == ParsingContextType.Type.IN_MAP;
    }

    boolean isInKey() {
        return peek().getType() == ParsingContextType.Type.IN_KEY;
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

    public <T extends ParseNode> ParsingContextType<T> getAncestor(ParsingContextType.Ancestor ancestor) {
        ParsingContextType<T> result = (ParsingContextType<T>) stack.get(ancestor.getOffset());
        return result;
    }

    public <T extends ParseNode> ParsingContextType<T> getCurrent() {
        return (ParsingContextType<T>) stack.getFirst();
    }

    public StreamNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(StreamNode node) {
        rootNode = node;
    }
}
