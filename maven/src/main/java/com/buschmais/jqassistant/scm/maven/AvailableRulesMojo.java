package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.rule.api.RuleHelper;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Lists all available rules.
 */
@Mojo(name = "available-rules", defaultPhase = LifecyclePhase.VALIDATE, aggregator = true, threadSafe = true)
public class AvailableRulesMojo extends AbstractRuleMojo {

    @Override
    public void aggregate(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException, MojoFailureException {
        getLog().info("Available rules for '" + mojoExecutionContext.getRootModule()
            .getName() + "'.");
        RuleSet ruleSet = readRules(mojoExecutionContext);
        RuleHelper ruleHelper = new RuleHelper();
        try {
            ruleHelper.printRuleSet(ruleSet, mojoExecutionContext.getConfiguration()
                .analyze()
                .rule());
        } catch (RuleException e) {
            throw new MojoExecutionException("Cannot print available rules.", e);
        }
    }
}
