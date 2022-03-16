package com.buschmais.jqassistant.scm.maven;

import java.util.List;

import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.rule.api.RuleHelper;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lists all available rules.
 */
@Mojo(name = "available-rules", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true,
      configurator = "custom")
public class AvailableRulesMojo extends AbstractProjectMojo {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvailableRulesMojo.class);


    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @Override
    public void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store, Configuration configuration)
        throws MojoExecutionException, MojoFailureException {
        getLog().info("Available rules for '" + rootModule.getName() + "'.");
        RuleSet ruleSet = readRules(rootModule, configuration);
        RuleHelper ruleHelper = new RuleHelper(LOGGER);
        try {
            ruleHelper.printRuleSet(ruleSet, configuration.analyze()
                .rule());
        } catch (RuleException e) {
            throw new MojoExecutionException("Cannot print available rules.", e);
        }
    }
}
