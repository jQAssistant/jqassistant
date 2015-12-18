package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.common.report.RuleHelper;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Lists all effective rules.
 */
@Mojo(name = "effective-rules", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class EffectiveRulesMojo extends AbstractProjectMojo {
    private static final Logger LOGGER = LoggerFactory.getLogger(EffectiveRulesMojo.class);

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    public void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        getLog().info("Effective rules for '" + rootModule.getName() + "'.");
        RuleSet ruleSet = readRules(rootModule);
        RuleSelection ruleSelection = RuleSelection.Builder.select(ruleSet, groups, constraints, concepts);
        RuleHelper ruleHelper = new RuleHelper(LOGGER);
        try {
            ruleHelper.printRuleSet(ruleSet, ruleSelection);
        } catch (AnalysisException e) {
            throw new MojoExecutionException("Cannot print effective rules.", e);
        }
    }

}
