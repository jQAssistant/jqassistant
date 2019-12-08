package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YAML2Descriptor;

class ContextType<D extends YAML2Descriptor> {
    private D descriptor;
    private Type type;

    private ContextType(Type contextType, D contextDescriptor) {
        type = contextType;
        descriptor = contextDescriptor;

    }

    public ContextType(Type type) {
        this(type, null);
    }

    static <D extends YAML2Descriptor> ContextType ofInFile(D descriptor) {
        return new ContextType(Type.IN_FILE, descriptor);
    }

    static <D extends YAML2Descriptor> ContextType ofInDocument(D descriptor) {
        return new ContextType(Type.IN_DOCUMENT, descriptor);
    }

    static <D extends YAML2Descriptor> ContextType ofMap(D descriptor) {
        return new ContextType(Type.IN_MAP, descriptor);
    }

    static <D extends YAML2Descriptor> ContextType ofSequence(D descriptor) {
        return new ContextType(Type.IN_SEQUENCE, descriptor);
    }

    static ContextType ofStream() {
        return new ContextType(Type.IN_STREAM);
    }

    Type getType() {
        return type;
    }

    public <E extends D> E getDescriptor() {
        // todo not nice to cast here
        return (E) descriptor;
    }

    enum Type {
        IN_DOCUMENT,
        IN_FILE,
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
