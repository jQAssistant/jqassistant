package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Abstract base class for mojo using rules.
 */
public abstract class AbstractRuleMojo extends AbstractProjectMojo {

    /**
     * Reads the available rules from the rules directory and deployed catalogs.
     *
     * @return A rule set. .
     * @throws MojoExecutionException
     *     If the rules cannot be read.
     */
    protected final RuleSet readRules(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException {
        List<RuleSource> sources = new ArrayList<>();
        PluginRepository pluginRepository = mojoExecutionContext.getPluginRepository();
        // read rules from rules directory
        addRuleFiles(sources, mojoExecutionContext.getRuleDirectory());
        List<RuleSource> ruleSources = pluginRepository.getRulePluginRepository()
            .getRuleSources();
        sources.addAll(ruleSources);
        Collection<RuleParserPlugin> ruleParserPlugins;
        try {
            ruleParserPlugins = pluginRepository.getRulePluginRepository()
                .getRuleParserPlugins(mojoExecutionContext.getConfiguration()
                    .analyze()
                    .rule());
        } catch (RuleException e) {
            throw new MojoExecutionException("Cannot get rules rule source reader plugins.", e);
        }
        try {
            RuleParser ruleParser = new RuleParser(ruleParserPlugins);
            return ruleParser.parse(sources);
        } catch (RuleException e) {
            throw new MojoExecutionException("Cannot read rules.", e);
        }
    }

    /**
     * Add rules from the given directory to the list of sources.
     *
     * @param sources
     *     The sources.
     * @param directory
     *     The directory.
     * @throws MojoExecutionException
     *     On error.
     */
    private void addRuleFiles(List<RuleSource> sources, File directory) throws MojoExecutionException {
        List<RuleSource> ruleSources = readRulesDirectory(directory);
        for (RuleSource ruleSource : ruleSources) {
            getLog().debug("Adding rules from file " + ruleSource);
            sources.add(ruleSource);
        }
    }

    /**
     * Retrieves the list of available rules from the rules directory.
     *
     * @param rulesDirectory
     *     The rules directory.
     * @return The {@link java.util.List} of available rules {@link java.io.File}s.
     * @throws MojoExecutionException
     *     If the rules directory cannot be read.
     */
    private List<RuleSource> readRulesDirectory(File rulesDirectory) throws MojoExecutionException {
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new MojoExecutionException(rulesDirectory.getAbsolutePath() + " does not exist or is not a directory.");
        }
        try {
            return FileRuleSource.getRuleSources(rulesDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot read rule directory: " + rulesDirectory.getAbsolutePath(), e);
        }
    }

}
