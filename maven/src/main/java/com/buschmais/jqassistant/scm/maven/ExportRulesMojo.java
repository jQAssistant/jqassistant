package com.buschmais.jqassistant.scm.maven;

import java.io.*;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.writer.RuleSetWriter;
import com.buschmais.jqassistant.core.rule.impl.writer.XmlRuleSetWriter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * Exports the all rules to an XML file.
 */
@Mojo(name = "export-rules", threadSafe = true)
public class ExportRulesMojo extends AbstractRuleMojo {

    @Override
    protected void aggregate(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException {
        MavenProject rootModule = mojoExecutionContext.getRootModule();
        getLog().info("Exporting rules for '" + rootModule.getName() + "'.");
        final RuleSet ruleSet = readRules(mojoExecutionContext);
        RuleSetWriter ruleSetWriter = new XmlRuleSetWriter(mojoExecutionContext.getConfiguration()
            .analyze()
            .rule());
        File outputFile = mojoExecutionContext.getOutputFile(null, "jqassistant-rules.xml");
        Writer writer;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create writer for rule export.", e);
        }
        try {
            ruleSetWriter.write(ruleSet, writer);
        } catch (RuleException e) {
            throw new MojoExecutionException("Cannot write rules.", e);
        }
    }
}
