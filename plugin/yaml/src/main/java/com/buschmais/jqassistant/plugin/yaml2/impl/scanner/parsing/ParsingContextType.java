package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;


import static com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.PositionalContext.noPositionalContext;

class ParsingContextType<D extends AbstractBaseNode> {
    private D node;
    private Type type;
    private PositionalContext positionalContext;
    boolean isKeyForValue = false;

    private ParsingContextType(Type contextType, D contextNode,
                               PositionalContext context) {
        type = contextType;
        node = contextNode;
        positionalContext = context;
    }

    static ParsingContextType<DocumentNode> ofInDocument(DocumentNode descriptor) {
        return new ParsingContextType<>(Type.IN_DOCUMENT, descriptor, noPositionalContext());
    }

    static ParsingContextType<MapNode> ofInMap(MapNode descriptor) {
        return new ParsingContextType<>(Type.IN_MAP, descriptor, noPositionalContext());
    }

    static ParsingContextType<SequenceNode> ofInSequence(SequenceNode descriptor) {
        return new ParsingContextType<>(Type.IN_SEQUENCE, descriptor, new PositionalContext());
    }

    static ParsingContextType<StreamNode> ofInStream(StreamNode node) {
        return new ParsingContextType<>(Type.IN_STREAM, node, noPositionalContext());
    }

    static <D extends AbstractBaseNode> ParsingContextType<D> ofInKey(D node) {
        return new ParsingContextType<>(Type.IN_KEY, node, noPositionalContext());
    }

    static ParsingContextType<AliasKeyNode> ofInAliasKey(AliasKeyNode node) {
        return new ParsingContextType<>(Type.IN_ALIAS_KEY, node, noPositionalContext());
    }

    static <D extends AbstractBaseNode> ParsingContextType<D> ofInComplexKey(D node) {
        return new ParsingContextType<>(Type.IN_COMPLEX_KEY, node, noPositionalContext());
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
        IN_ALIAS_KEY,
        IN_COMPLEX_KEY,
        IN_DOCUMENT,
        IN_KEY,
        IN_MAP,
        IN_SEQUENCE,
        IN_STREAM
    }

    @Override
    public String toString() {
        return "ContextType{" +
            "type=" + type +
            '}';
    }
}
