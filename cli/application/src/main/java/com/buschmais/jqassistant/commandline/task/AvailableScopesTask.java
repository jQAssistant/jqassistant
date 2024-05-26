package com.buschmais.jqassistant.commandline.task;

import java.util.List;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.scanner.api.ScopeHelper;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A task for listing all available scanner scopes as provided by plugins.
 */
public class AvailableScopesTask extends AbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvailableScopesTask.class);

    @Override
    protected void addTaskOptions(List<Option> options) {
        // nothing to add here
    }

    @Override
    public void run(CliConfiguration configuration, Options options) {
        ScopeHelper scopeHelper = new ScopeHelper(LOGGER);
        scopeHelper.printScopes(pluginRepository.getScannerPluginRepository()
            .getScopes());
    }

}
