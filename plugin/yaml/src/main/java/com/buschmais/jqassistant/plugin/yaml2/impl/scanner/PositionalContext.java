package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

public class PositionalContext {
    public static PositionalContext NO_POSITIONAL_CONTEXT;

    static {
        NO_POSITIONAL_CONTEXT = new NoPositionalContext();
    }

    private int currentIndex = 0;

    int inc() {
        return currentIndex++;
    }
}

class NoPositionalContext extends PositionalContext {
    @Override
    int inc() {
        throw new UnsupportedOperationException();
    }
}
