package com.buschmais.jqassistant.scm.maven;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.analysis.api.RuleSetWriter;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetWriterImpl;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Exports the effective rules to an XML file.
 */
@Mojo(name = "export-rules")
public class ExportRulesMojo extends AbstractProjectMojo {

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    protected void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        getLog().info("Exporting rules for '" + rootModule.getName() + "'.");
        final RuleSet ruleSet = resolveEffectiveRules(rootModule);
        RuleSetWriter ruleSetWriter = new RuleSetWriterImpl();
        String exportedRules = rootModule.getBuild().getDirectory() + "/jqassistant/jqassistant-rules.xml";
        Writer writer;
        try {
            writer = new FileWriter(exportedRules);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create writer for rule export.", e);
        }
        ruleSetWriter.write(ruleSet, writer);
    }

}
