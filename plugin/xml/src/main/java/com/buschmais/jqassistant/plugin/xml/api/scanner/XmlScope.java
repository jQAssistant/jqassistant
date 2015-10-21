package com.buschmais.jqassistant.plugin.xml.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Defines the scopes related to XML documents.
 */
public enum XmlScope implements Scope {
    DOCUMENT {
        @Override
        public void onEnter(ScannerContext context) {
        }

        @Override
        public void onLeave(ScannerContext context) {
        }
    };

    @Override
    public String getPrefix() {
        return "xml";
    }

    @Override
    public String getName() {
        return name();
    }
}
