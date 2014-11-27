package com.buschmais.jqassistant.plugin.common.test.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scope;

public enum CustomScope implements Scope {

    CUSTOM;

    @Override
    public String getPrefix() {
        return "custom";
    }

    @Override
    public String getName() {
        return name();
    }
}
