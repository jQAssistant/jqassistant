package com.buschmais.jqassistant.core.runtime.impl.plugin;

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
