package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.mcp.impl.MCPServer;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Starts an MCP server (stdio) providing access to the jQAssistant store and rules.
 * <p>
 * The server runs until the MCP client closes the connection. As stdout is reserved for the MCP protocol, Maven must be executed in quiet and batch
 * mode, e.g. <code>mvn -q -B jqassistant:mcp</code>.
 */
@Mojo(name = "mcp", threadSafe = true)
public class MCPMojo extends AbstractMojo {

    @Override
    protected MavenTask getMavenTask() {
        return new AbstractMavenRuleTask(cachingStoreProvider) {
            @Override
            public void leaveProject(MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException {
                RuleSet ruleSet = readRules(mavenTaskContext);
                withStore(store -> {
                    try {
                        new MCPServer(store, ruleSet).run();
                    } catch (InterruptedException e) {
                        Thread.currentThread()
                            .interrupt();
                        throw new MojoExecutionException("The MCP server has been interrupted.", e);
                    }
                }, mavenTaskContext);
            }
        };
    }
}
