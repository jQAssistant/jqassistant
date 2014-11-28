package com.buschmais.jqassistant.scm.common.report;

import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Console;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Provides common functionality for working with scopes.
 */
public class ScopeHelper {

    private final Console console;

    /**
     * Constructor.
     * 
     * @param console
     *            The console used for writing messages.
     */
    public ScopeHelper(Console console) {
        this.console = console;
    }

    /**
     * Print a list of available scopes to the console.
     * 
     * @param scopes
     *            The available scopes.
     */
    public void printScopes(Map<String, Scope> scopes) {
        console.info("Scopes [" + scopes.size() + "]");
        for (String scopeName : scopes.keySet()) {
            console.info("\t" + scopeName);
        }
    }
}
