package com.buschmais.jqassistant.scm.cli.task;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import com.buschmais.jqassistant.scm.cli.Log;
import com.buschmais.jqassistant.scm.common.report.ScopeHelper;

/**
 * A task for listing all available scanner scopes as provided by plugins.
 */
public class AvailableScopesTask extends AbstractAnalyzeTask {

    @Override
    protected void executeTask(Store store) throws CliExecutionException {
        ScopeHelper scopeHelper = new ScopeHelper(Log.getLog());
        try {
            scopeHelper.printScopes(pluginRepository.getScopePluginRepository().getScopes());
        } catch (PluginRepositoryException e) {
            throw new CliExecutionException("Cannot get scope plugin repository.", e);
        }
    }
}
