package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDescriptor;

class ContextType<D extends YMLDescriptor> {
    private D descriptor;
    private Type type;

    private ContextType(Type contextType, D contextDescriptor) {
        type = contextType;
        descriptor = contextDescriptor;

    }

    public ContextType(Type type) {
        this(type, null);
    }

    static <D extends YMLDescriptor> ContextType<D> ofInFile(D descriptor) {
        return new ContextType<>(Type.IN_FILE, descriptor);
    }

    static <D extends YMLDescriptor> ContextType<D> ofInDocument(D descriptor) {
        return new ContextType<>(Type.IN_DOCUMENT, descriptor);
    }

    static <D extends YMLDescriptor> ContextType<D> ofInMap(D descriptor) {
        return new ContextType<>(Type.IN_MAP, descriptor);
    }

    static <D extends YMLDescriptor> ContextType<D> ofInSequence(D descriptor) {
        return new ContextType<>(Type.IN_SEQUENCE, descriptor);
    }

    static <D extends YMLDescriptor> ContextType<D> ofInStream() {
        return new ContextType<>(Type.IN_STREAM);
    }

    static <D extends YMLDescriptor> ContextType<D> ofInKey(D descriptor) {
        return new ContextType<>(Type.IN_KEY, descriptor);
    }

    Type getType() {
        return type;
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
