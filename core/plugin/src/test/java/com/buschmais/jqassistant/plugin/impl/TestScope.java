package com.buschmais.jqassistant.plugin.impl;

import com.buschmais.jqassistant.core.scanner.api.Scope;

public enum TestScope implements Scope {

    FOO;

    @Override
    public String getPrefix() {
        return "test";
    }

    @Override
    public String getName() {
        return name();
    }
}
