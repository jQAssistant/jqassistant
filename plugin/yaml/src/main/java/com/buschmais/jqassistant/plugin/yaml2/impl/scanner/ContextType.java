package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;

import static com.buschmais.jqassistant.plugin.yaml2.impl.scanner.PositionalContext.NO_POSITIONAL_CONTEXT;

class ContextType<D extends YMLDescriptor> {
    private D descriptor;
    private Type type;
    private PositionalContext positionalContext;
    boolean isKeyForValue = false;

    private ContextType(Type contextType, D contextDescriptor,
                        PositionalContext context) {
        type = contextType;
        descriptor = contextDescriptor;
        positionalContext = context;
    }

    public ContextType(Type type, PositionalContext context) {
        this(type, null, context);
    }

    static <D extends YMLDescriptor> ContextType<D> ofInFile(D descriptor) {
        return new ContextType<>(Type.IN_FILE, descriptor, NO_POSITIONAL_CONTEXT);
    }

    static <D extends YMLDescriptor> ContextType<D> ofInDocument(D descriptor) {
        return new ContextType<>(Type.IN_DOCUMENT, descriptor, NO_POSITIONAL_CONTEXT);
    }

    static <D extends YMLDescriptor> ContextType<D> ofInMap(D descriptor) {
        return new ContextType<>(Type.IN_MAP, descriptor, NO_POSITIONAL_CONTEXT);
    }

    static <D extends YMLDescriptor> ContextType<D> ofInSequence(D descriptor) {
        return new ContextType<>(Type.IN_SEQUENCE, descriptor, new PositionalContext());
    }

    static <D extends YMLDescriptor> ContextType<D> ofInStream() {
        return new ContextType<>(Type.IN_STREAM, NO_POSITIONAL_CONTEXT);
    }

    static <D extends YMLDescriptor> ContextType<D> ofInKey(D descriptor) {
        return new ContextType<>(Type.IN_KEY, descriptor, NO_POSITIONAL_CONTEXT);
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

    public D getDescriptor() {
        return descriptor;
    }

    enum Type {
        IN_DOCUMENT,
        IN_FILE,
        IN_KEY,
        IN_MAP,
        IN_SEQUENCE,
        IN_STREAM;
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
