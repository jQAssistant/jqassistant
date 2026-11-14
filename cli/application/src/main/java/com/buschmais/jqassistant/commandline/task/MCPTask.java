package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.mcp.impl.MCPServer;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

import org.apache.commons.cli.Options;

/**
 * Starts an MCP server (stdio) providing access to the jQAssistant store and rules.
 * <p>
 * The server runs until the MCP client closes the connection.
 */
public class MCPTask extends AbstractRuleTask {

    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
        RuleSet ruleSet = getAvailableRules(configuration.analyze()
            .rule());
        withStore(configuration, store -> {
            try {
                new MCPServer(store, ruleSet).run();
            } catch (InterruptedException e) {
                Thread.currentThread()
                    .interrupt();
                throw new CliExecutionException("The MCP server has been interrupted.", e);
            }
        });
    }
}
