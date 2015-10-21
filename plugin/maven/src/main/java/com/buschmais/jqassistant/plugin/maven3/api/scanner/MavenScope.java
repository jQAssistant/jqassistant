package com.buschmais.jqassistant.plugin.maven3.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Defines the scopes for maven.
 */
public enum MavenScope implements Scope {

    PROJECT {
        @Override
        public void onEnter(ScannerContext context) {
        }

        @Override
        public void onLeave(ScannerContext context) {
        }
    },
    REPOSITORY {
        @Override
        public void onEnter(ScannerContext context) {
        }

        @Override
        public void onLeave(ScannerContext context) {
        }
    };

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getPrefix() {
        return "maven";
    }
}
