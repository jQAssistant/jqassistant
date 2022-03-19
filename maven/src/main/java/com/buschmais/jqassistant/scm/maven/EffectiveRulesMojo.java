package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.rule.api.RuleHelper;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSelection;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lists all effective rules.
 */
@Mojo(name = "effective-rules", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, configurator = "custom")
public class EffectiveRulesMojo extends AbstractRuleMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(EffectiveRulesMojo.class);

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @Override
    public void aggregate(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException, MojoFailureException {
        getLog().info("Effective rules for '" + mojoExecutionContext.getRootModule()
            .getName() + "'.");
        RuleSet ruleSet = readRules(mojoExecutionContext);
        RuleSelection ruleSelection = RuleSelection.select(ruleSet, groups, constraints, concepts);
        RuleHelper ruleHelper = new RuleHelper(LOGGER);
        try {
            ruleHelper.printRuleSet(ruleSet, ruleSelection, mojoExecutionContext.getConfiguration()
                .analyze()
                .rule());
        } catch (RuleException e) {
            throw new MojoExecutionException("Cannot print effective rules.", e);
        }
    }

}
