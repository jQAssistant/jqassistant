package com.buschmais.jqassistant.core.scanner.impl;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;

public enum TestScope implements Scope {

    TEST;

    @Override
    public String getPrefix() {
        return "test";
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public void onEnter(ScannerContext context) {
    }

    @Override
    public void onLeave(ScannerContext context) {
    }
}
