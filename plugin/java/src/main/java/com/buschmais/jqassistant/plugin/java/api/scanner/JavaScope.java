package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Defines the scopes for java.
 */
public enum JavaScope implements Scope {

    CLASSPATH {
        @Override
        public void onEnter(ScannerContext context) {
        }

        @Override
        public void onLeave(ScannerContext context) {
        }
    };

    @Override
    public String getPrefix() {
        return "java";
    }

    @Override
    public String getName() {
        return name();
    }

}
