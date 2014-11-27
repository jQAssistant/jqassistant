package com.buschmais.jqassistant.plugin.junit.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Defines the scopes for junit.
 */
public enum JunitScope implements Scope {

    TESTREPORTS {
        @Override
        public void create(ScannerContext context) {
        }

        @Override
        public void destroy(ScannerContext context) {
        }
    };

    @Override
    public String getPrefix() {
        return "junit";
    }

    @Override
    public String getName() {
        return name();
    }
}
