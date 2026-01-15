package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.rule.api.RuleHelper;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSelection;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Lists all effective rules.
 */
@Mojo(name = "effective-rules", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class EffectiveRulesMojo extends AbstractRuleMojo {

    @Override
    protected void beforeProject(MojoExecutionContext mojoExecutionContext) {
        // nothing to do here
    }

    @Override
    public void afterProject(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException {
        getLog().info("Effective rules for '" + mojoExecutionContext.getRootModule()
            .getName() + "'.");
        Analyze analyze = mojoExecutionContext.getConfiguration()
            .analyze();
        RuleSet ruleSet = readRules(mojoExecutionContext);
        RuleSelection ruleSelection = RuleSelection.select(ruleSet, analyze.groups(), analyze.constraints(), analyze.excludeConstraints(), analyze.concepts());
        RuleHelper ruleHelper = new RuleHelper();
        try {
            ruleHelper.printRuleSet(ruleSet, ruleSelection, analyze.rule());
        } catch (RuleException e) {
            throw new MojoExecutionException("Cannot print effective rules.", e);
        }
    }

}
