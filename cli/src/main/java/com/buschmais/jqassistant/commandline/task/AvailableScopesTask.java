package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.splittingsupport.scm.ScopeHelper;
import com.buschmais.jqassistant.core.store.api.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A task for listing all available scanner scopes as provided by plugins.
 */
public class AvailableScopesTask extends AbstractAnalyzeTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvailableScopesTask.class);

    @Override
    protected void executeTask(Store store) throws CliExecutionException {
        ScopeHelper scopeHelper = new ScopeHelper(LOGGER);
        try {
            scopeHelper.printScopes(pluginRepository.getScopePluginRepository().getScopes());
        } catch (PluginRepositoryException e) {
            throw new CliExecutionException("Cannot get scope plugin repository.", e);
        }
    }
}
