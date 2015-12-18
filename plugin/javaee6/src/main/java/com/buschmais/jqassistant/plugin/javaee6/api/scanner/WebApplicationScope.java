package com.buschmais.jqassistant.plugin.javaee6.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Defines the scopes for Java web applications.
 */
public enum WebApplicationScope implements Scope {

    WAR {
        @Override
        public void onEnter(ScannerContext context) {
        }

        @Override
        public void onLeave(ScannerContext context) {
        }
    };

    @Override
    public String getPrefix() {
        return "java-web";
    }

    @Override
    public String getName() {
        return name();
    }

}
