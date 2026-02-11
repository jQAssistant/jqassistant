package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.rule.api.RuleHelper;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Lists all available rules.
 */
@Mojo(name = "available-rules", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
@Slf4j
public class AvailableRulesMojo extends AbstractMojo {

    @Override
    protected MavenTask getMavenTask() {
        return new AbstractMavenRuleTask(cachingStoreProvider) {

            @Override
            public void leaveProject(MavenTaskContext mavenTaskContext) throws MojoExecutionException {
                log.info("Available rules for '{}'.", mavenTaskContext.getRootModule()
                    .getName());
                RuleSet ruleSet = readRules(mavenTaskContext);
                RuleHelper ruleHelper = new RuleHelper();
                try {
                    ruleHelper.printRuleSet(ruleSet, mavenTaskContext.getConfiguration()
                        .analyze()
                        .rule());
                } catch (RuleException e) {
                    throw new MojoExecutionException("Cannot print available rules.", e);
                }
            }

        };
    }

}
