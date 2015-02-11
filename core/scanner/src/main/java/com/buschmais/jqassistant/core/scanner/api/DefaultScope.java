package com.buschmais.jqassistant.core.scanner.api;

/**
 * The default scope(s).
 */
public enum DefaultScope implements Scope {

    NONE {
        @Override
        public void create(ScannerContext context) {
        }

        @Override
        public void destroy(ScannerContext context) {
        }
    };

    @Override
    public String getPrefix() {
        return "default";
    }

    @Override
    public String getName() {
        return name();
    }
}
