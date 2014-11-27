package com.buschmais.jqassistant.plugin.maven3.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Defines the scopes for maven.
 */
public enum MavenScope implements Scope {

    PROJECT;

    @Override
    public String getPrefix() {
        return "maven";
    }

    @Override
    public String getName() {
        return name();
    }
}
