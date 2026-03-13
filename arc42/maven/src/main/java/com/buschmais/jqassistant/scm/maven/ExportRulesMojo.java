package com.buschmais.jqassistant.scm.maven;

import java.io.*;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.writer.RuleSetWriter;
import com.buschmais.jqassistant.core.rule.impl.writer.XmlRuleSetWriter;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Exports the all rules to an XML file.
 */
@Mojo(name = "export-rules", threadSafe = true)
@Slf4j
public class ExportRulesMojo extends AnalyzeMojo {

    @Override
    protected MavenTask getMavenTask() {
        return new AbstractMavenRuleTask(cachingStoreProvider) {
            @Override
            public void leaveProject(MavenTaskContext mavenTaskContext) throws MojoExecutionException {
                MavenProject rootModule = mavenTaskContext.getRootModule();
                log.info("Exporting rules for '{}'.", rootModule.getName());
                final RuleSet ruleSet = readRules(mavenTaskContext);
                RuleSetWriter ruleSetWriter = new XmlRuleSetWriter(mavenTaskContext.getConfiguration()
                    .analyze()
                    .rule());
                File outputFile = new File(mavenTaskContext.getOutputDirectory() + "/jqassistant-rules.xml");
                Writer writer;
                try {
                    writer = new OutputStreamWriter(new FileOutputStream(outputFile), UTF_8);
                } catch (IOException e) {
                    throw new MojoExecutionException("Cannot create writer for rule export.", e);
                }
                try {
                    ruleSetWriter.write(ruleSet, writer);
                } catch (RuleException e) {
                    throw new MojoExecutionException("Cannot write rules.", e);
                }
            }
        };

    }

}
