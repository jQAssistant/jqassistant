package com.buschmais.jqassistant.plugin.rdbms.api;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Defines the scopes for the RDBMS plugin.
 */
public enum RdbmsScope implements Scope {

    /**
     * Defines the scope of a connection to a database.
     */
    CONNECTION {
        @Override
        public void onEnter(ScannerContext context) {
        }

        @Override
        public void onLeave(ScannerContext context) {
        }
    };

    @Override
    public String getPrefix() {
        return "rdbms";
    }

    @Override
    public String getName() {
        return name();
    }
}
