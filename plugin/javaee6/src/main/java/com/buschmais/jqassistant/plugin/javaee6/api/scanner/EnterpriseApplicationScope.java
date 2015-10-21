package com.buschmais.jqassistant.plugin.javaee6.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Defines the scopes for Java enterprise application archives (EAR).
 */
public enum EnterpriseApplicationScope implements Scope {

    EAR {
        @Override
        public void onEnter(ScannerContext context) {
        }

        @Override
        public void onLeave(ScannerContext context) {
        }
    };

    @Override
    public String getPrefix() {
        return "java-ee";
    }

    @Override
    public String getName() {
        return name();
    }

}
