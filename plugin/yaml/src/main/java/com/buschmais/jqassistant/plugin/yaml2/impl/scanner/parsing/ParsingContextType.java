package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;


import static com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.PositionalContext.NO_POSITIONAL_CONTEXT;

class ParsingContextType<D extends BaseNode<?>> {
    private D node;
    private Type type;
    private PositionalContext positionalContext;
    boolean isKeyForValue = false;

    private ParsingContextType(Type contextType, D contextDescriptor,
                               PositionalContext context) {
        type = contextType;
        node = contextDescriptor;
        positionalContext = context;
    }

    public ParsingContextType(Type type, PositionalContext context) {
        this(type, null, context);
    }

    static <D extends BaseNode<?>> ParsingContextType<D> ofInFile(D descriptor) {
        return new ParsingContextType<>(Type.IN_FILE, descriptor, NO_POSITIONAL_CONTEXT);
    }

    static <D extends BaseNode<?>> ParsingContextType<D> ofInDocument(D descriptor) {
        return new ParsingContextType<>(Type.IN_DOCUMENT, descriptor, NO_POSITIONAL_CONTEXT);
    }

    static <D extends BaseNode<?>> ParsingContextType<D> ofInMap(D descriptor) {
        return new ParsingContextType<>(Type.IN_MAP, descriptor, NO_POSITIONAL_CONTEXT);
    }

    static <D extends BaseNode<?>> ParsingContextType<D> ofInSequence(D descriptor) {
        return new ParsingContextType<>(Type.IN_SEQUENCE, descriptor, new PositionalContext());
    }

    static <N extends BaseNode<?>> ParsingContextType<N> ofInStream(N node) {
        return new ParsingContextType<>(Type.IN_STREAM, node, NO_POSITIONAL_CONTEXT);
    }

    static <D extends BaseNode<?>> ParsingContextType<D> ofInKey(D node) {
        return new ParsingContextType<>(Type.IN_KEY, node, NO_POSITIONAL_CONTEXT);
    }

    static <D extends BaseNode<?>> ParsingContextType<D> ofInComplexKey(D node) {
        return new ParsingContextType<>(Type.IN_COMPLEX_KEY, node, NO_POSITIONAL_CONTEXT);
    }

    Type getType() {
        return type;
    }

    public boolean isKeyForValue() {
        return isKeyForValue;
    }

    public void setKeyForValue(boolean value) {
        isKeyForValue = value;
    }

    public PositionalContext getPositionalContext() {
        return positionalContext;
    }

    public D getNode() {
        return node;
    }

    enum Type {
        IN_COMPLEX_KEY,
        IN_DOCUMENT,
        IN_FILE,
        IN_KEY,
        IN_MAP,
        IN_SEQUENCE,
        IN_STREAM
    }

    enum Ancestor {
        FIRST(1),
        SECOND(2);

        private final int offset;

        Ancestor(int ancestorsOffset) {
            offset = ancestorsOffset;
        }

        public int getOffset() {
            return offset;
        }
    }

    @Override
    public String toString() {
        return "ContextType{" +
            "type=" + type +
            '}';
    }
}
