package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.core.analysis.api.RuleSetWriter;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetWriterImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

/**
 * Exports the effective rules to an XML file.
 */
@Mojo(name = "export-rules")
public class ExportRulesMojo extends AbstractAnalysisAggregatorMojo {

    @Override
    protected void aggregate(MavenProject baseProject, Set<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        getLog().info("Exporting rules for '" + baseProject.getName() + "'.");
        final RuleSet ruleSet = resolveEffectiveRules(baseProject);
        RuleSetWriter ruleSetWriter = new RuleSetWriterImpl();
        String exportedRules = baseProject.getBuild().getDirectory() + "/jqassistant/jqassistant-rules.xml";
        Writer writer;
        try {
            writer = new FileWriter(exportedRules);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create writer for rule export.", e);
        }
        ruleSetWriter.write(ruleSet, writer);
    }

    @Override
    protected boolean isResetStoreOnInitialization() {
        return false;
    }
}
