package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.scanner.api.ScopeHelper;
import com.buschmais.jqassistant.core.store.api.Store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A task for listing all available scanner scopes as provided by plugins.
 */
public class AvailableScopesTask extends AbstractAnalyzeTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvailableScopesTask.class);

    @Override
    protected void executeTask(CliConfiguration configuration, Store store) {
        ScopeHelper scopeHelper = new ScopeHelper(LOGGER);
        scopeHelper.printScopes(pluginRepository.getScannerPluginRepository()
            .getScopes());
    }
}
