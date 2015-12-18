package com.buschmais.jqassistant.plugin.junit.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Defines the scopes for junit.
 */
public enum JunitScope implements Scope {

    TESTREPORTS {
        @Override
        public void onEnter(ScannerContext context) {
        }

        @Override
        public void onLeave(ScannerContext context) {
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
