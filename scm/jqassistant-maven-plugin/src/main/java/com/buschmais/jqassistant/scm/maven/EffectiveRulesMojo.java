package com.buschmais.jqassistant.scm.maven;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.common.report.ReportHelper;

/**
 * Lists all effective rules.
 */
@Mojo(name = "effective-rules", defaultPhase = LifecyclePhase.VALIDATE)
public class EffectiveRulesMojo extends AbstractProjectMojo {

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    public void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        getLog().info("Effective rules for '" + rootModule.getName() + "'.");
        RuleSet targetRuleSet = resolveEffectiveRules(rootModule);
        ReportHelper reportHelper = new ReportHelper(new MavenConsole(getLog()));
        reportHelper.printRuleSet(targetRuleSet);
    }

}
