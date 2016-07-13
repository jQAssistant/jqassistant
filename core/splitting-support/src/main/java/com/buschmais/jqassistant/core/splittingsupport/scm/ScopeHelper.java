package com.buschmais.jqassistant.core.splittingsupport.scm;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Provides common functionality for working with scopes.
 *
 * @deprecated Please use {@link com.buschmais.jqassistant.core.splittingsupport.impl.ScopeHelper}
 *             instead.
 */
@Deprecated
public class ScopeHelper {

    private final Logger logger;

    /**
     * Constructor.
     * 
     * @param log
     *            The logger used to log all messages
     */
    public ScopeHelper(Logger log) {
        this.logger = log;
    }

    /**
     * Print a list of available scopes to the console.
     * 
     * @param scopes
     *            The available scopes.
     */
    public void printScopes(Map<String, Scope> scopes) {
        logger.info("Scopes [" + scopes.size() + "]");
        for (String scopeName : scopes.keySet()) {
            logger.info("\t" + scopeName);
        }
    }
}
