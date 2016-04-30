package com.buschmais.jqassistant.scm.maven;

import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
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

/**
 * Lists all available rules.
 */
@Mojo(name = "available-rules", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class AvailableRulesMojo extends AbstractProjectMojo {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvailableRulesMojo.class);


    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    public void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        getLog().info("Available rules for '" + rootModule.getName() + "'.");
        RuleSet ruleSet = readRules(rootModule);
        RuleHelper ruleHelper = new RuleHelper(LOGGER);
        try {
            ruleHelper.printRuleSet(ruleSet);
        } catch (AnalysisException e) {
            throw new MojoExecutionException("Cannot print available rules.", e);
        }
    }
}
