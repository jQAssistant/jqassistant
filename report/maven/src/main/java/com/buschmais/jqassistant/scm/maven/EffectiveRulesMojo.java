package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.rule.api.RuleHelper;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSelection;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Lists all effective rules.
 */
@Mojo(name = "effective-rules", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
@Slf4j
public class EffectiveRulesMojo extends AbstractMojo {

    @Override
    protected MavenTask getMavenTask() {
        return new AbstractMavenRuleTask(cachingStoreProvider) {
            @Override
            public void leaveProject(MavenTaskContext mavenTaskContext) throws MojoExecutionException {
                log.info("Effective rules for '" + mavenTaskContext.getRootModule()
                    .getName() + "'.");
                Analyze analyze = mavenTaskContext.getConfiguration()
                    .analyze();
                RuleSet ruleSet = readRules(mavenTaskContext);
                RuleSelection ruleSelection = RuleSelection.select(ruleSet, analyze.groups(), analyze.constraints(), analyze.excludeConstraints(),
                    analyze.concepts());
                RuleHelper ruleHelper = new RuleHelper();
                try {
                    ruleHelper.printRuleSet(ruleSet, ruleSelection, analyze.rule());
                } catch (RuleException e) {
                    throw new MojoExecutionException("Cannot print effective rules.", e);
                }
            }
        };
    }

}
