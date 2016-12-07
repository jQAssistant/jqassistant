package com.buschmais.jqassistant.plugin.json.impl.scanner;

public class NestingLevelCounter {
    private int level;
    private final int maxNestingLevel;

    public NestingLevelCounter(int maxDeep) {
        this.maxNestingLevel = maxDeep;
    }

    public Checker enter() {
        ++level;

        return new Checker();
    }

    public void leave() {
        --level;
    }

    public int level() {
        return level;
    }

    class Checker {
        void check() {
            if (level >= maxNestingLevel) {
                throw new IllegalStateException("Maximum nesting level reached. JSON is to deep nested.");
            }
        }
    }
}
