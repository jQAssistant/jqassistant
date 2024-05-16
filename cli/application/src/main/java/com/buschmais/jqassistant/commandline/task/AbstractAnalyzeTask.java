package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSelection;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;

/**
 * Abstract base class for all tasks working with rules.
 */
public abstract class AbstractAnalyzeTask extends AbstractStoreTask {

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    protected RuleSet getAvailableRules(Rule rule) throws CliExecutionException {
        List<RuleSource> sources = new ArrayList<>();
        File selectedDirectory = new File(rule.directory()
            .orElse(DEFAULT_RULE_DIRECTORY));
        // read rules from rules directory
        sources.addAll(readRulesDirectory(selectedDirectory));
        List<RuleSource> ruleSources = pluginRepository.getRulePluginRepository()
            .getRuleSources();
        sources.addAll(ruleSources);
        Collection<RuleParserPlugin> ruleParserPlugins;
        try {
            ruleParserPlugins = pluginRepository.getRulePluginRepository()
                .getRuleParserPlugins(rule);
        } catch (RuleException e) {
            throw new CliExecutionException("Cannot get rule source reader plugins.", e);
        }
        try {
            RuleParser ruleParser = new RuleParser(ruleParserPlugins);
            return ruleParser.parse(sources);
        } catch (RuleException e) {
            throw new CliExecutionException("Cannot read rules.", e);
        }
    }

    /**
     * Return the selection of rules.
     *
     * @param ruleSet
     *     The rule set.
     * @param analyze
     *     The {@link Analyze} configuration.
     * @return The selection of rules.
     */
    protected RuleSelection getRuleSelection(RuleSet ruleSet, Analyze analyze) {
        return RuleSelection.select(ruleSet, analyze.groups(), analyze.constraints(), analyze.excludeConstraints(), analyze.concepts());
    }

    private List<RuleSource> readRulesDirectory(File rulesDirectory) throws CliExecutionException {
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new CliExecutionException(rulesDirectory.getAbsolutePath() + " does not exist or is not a directory.");
        }
        try {
            return FileRuleSource.getRuleSources(rulesDirectory);
        } catch (IOException e) {
            throw new CliExecutionException("Cannot read rules directory: " + rulesDirectory.getAbsolutePath(), e);
        }
    }
}
