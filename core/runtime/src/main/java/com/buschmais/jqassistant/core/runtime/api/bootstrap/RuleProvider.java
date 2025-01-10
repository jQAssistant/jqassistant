package com.buschmais.jqassistant.core.runtime.api.bootstrap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSelection;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;
import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;

import lombok.Getter;

public class RuleProvider {

    private final Configuration configuration;

    private final PluginRepository pluginRepository;

    @Getter
    private final List<RuleSource> ruleSources;

    @Getter
    private final RuleSet availableRules;

    @Getter
    private final RuleSelection effectiveRules;

    public static RuleProvider create(Configuration configuration, String defaultRuleDirectory, PluginRepository pluginRepository) throws RuleException {
        return new RuleProvider(configuration, pluginRepository, defaultRuleDirectory);
    }

    private RuleProvider(Configuration configuration, PluginRepository pluginRepository, String defaultRuleDirectory) throws RuleException {
        this.configuration = configuration;
        this.pluginRepository = pluginRepository;
        this.ruleSources = initRuleSources(defaultRuleDirectory);
        this.availableRules = initAvailableRules();
        this.effectiveRules = initEffectiveRules();
    }

    private List<RuleSource> initRuleSources(String defaultRuleDirectory) throws RuleException {
        List<RuleSource> sources = new ArrayList<>();
        sources.addAll(readRulesDirectory(defaultRuleDirectory));
        sources.addAll(pluginRepository.getRulePluginRepository()
            .getRuleSources());
        return sources;
    }

    private List<RuleSource> readRulesDirectory(String defaultRuleDirectory) throws RuleException {
        File rulesDirectory = new File(configuration.analyze()
            .rule()
            .directory()
            .orElse(defaultRuleDirectory));
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new RuleException(rulesDirectory.getAbsolutePath() + " is not a directory.");
        }
        try {
            return FileRuleSource.getRuleSources(rulesDirectory);
        } catch (IOException e) {
            throw new RuleException("Cannot read rules directory: " + rulesDirectory.getAbsolutePath(), e);
        }
    }

    private RuleSet initAvailableRules() throws RuleException {
        Collection<RuleParserPlugin> ruleParserPlugins;
        ruleParserPlugins = pluginRepository.getRulePluginRepository()
            .getRuleParserPlugins(configuration.analyze()
                .rule());
        RuleParser ruleParser = new RuleParser(ruleParserPlugins);
        return ruleParser.parse(ruleSources);
    }

    private RuleSelection initEffectiveRules() {
        Analyze analyze = this.configuration.analyze();
        return RuleSelection.select(this.availableRules, analyze.groups(), analyze.constraints(), analyze.excludeConstraints(), analyze.concepts());
    }
}
