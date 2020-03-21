package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.util.Objects;

public class PositionalContext {
    public static PositionalContext NO_POSITIONAL_CONTEXT = new NoPositionalContext();

    private int currentIndex = 0;

    public int inc() {
        return currentIndex++;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        PositionalContext that = (PositionalContext) other;
        return currentIndex == that.currentIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentIndex);
    }
}

class NoPositionalContext extends PositionalContext {
    @Override
    public int inc() {
        throw new UnsupportedOperationException();
    }
}
