package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import java.util.Objects;

class PositionalContext {
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

    public static PositionalContext noPositionalContext() {
        return new NoPositionalContext();
    }

    static class NoPositionalContext extends PositionalContext {
        @Override
        public int inc() {
            throw new UnsupportedOperationException();
        }
    }

}

