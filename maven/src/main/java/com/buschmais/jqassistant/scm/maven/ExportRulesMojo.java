package com.buschmais.jqassistant.scm.maven;

import java.io.*;
import java.util.List;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.writer.RuleSetWriter;
import com.buschmais.jqassistant.core.rule.impl.writer.XmlRuleSetWriter;
import com.buschmais.jqassistant.core.store.api.Store;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * Exports the all rules to an XML file.
 */
@Mojo(name = "export-rules", threadSafe = true,
      configurator = "custom")
public class ExportRulesMojo extends AbstractProjectMojo {

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @Override
    protected void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException {
        getLog().info("Exporting rules for '" + rootModule.getName() + "'.");
        final RuleSet ruleSet = readRules(rootModule);
        RuleSetWriter ruleSetWriter = new XmlRuleSetWriter(getRuleConfiguration(), getConfiguration().analyze().rule());
        String exportedRules = rootModule.getBuild().getDirectory() + "/jqassistant/jqassistant-rules.xml";
        Writer writer;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(exportedRules), "UTF-8");
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
