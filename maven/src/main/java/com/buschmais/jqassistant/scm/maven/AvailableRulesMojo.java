package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.rule.api.RuleHelper;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Lists all available rules.
 */
@Mojo(name = "available-rules", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class AvailableRulesMojo extends AbstractRuleMojo {

    @Override
    protected void beforeProject(MojoExecutionContext mojoExecutionContext) {
        // nothing to do here
    }

    @Override
    public void afterProject(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException {
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
