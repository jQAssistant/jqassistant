package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.rule.api.RuleHelper;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lists all available rules.
 */
@Mojo(name = "available-rules", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class AvailableRulesMojo extends AbstractRuleMojo {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvailableRulesMojo.class);

    @Override
    public void aggregate(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException, MojoFailureException {
        getLog().info("Available rules for '" + mojoExecutionContext.getRootModule()
            .getName() + "'.");
        RuleSet ruleSet = readRules(mojoExecutionContext);
        RuleHelper ruleHelper = new RuleHelper(LOGGER);
        try {
            ruleHelper.printRuleSet(ruleSet, mojoExecutionContext.getConfiguration()
                .analyze()
                .rule());
        } catch (RuleException e) {
            throw new MojoExecutionException("Cannot print available rules.", e);
        }
    }
}
