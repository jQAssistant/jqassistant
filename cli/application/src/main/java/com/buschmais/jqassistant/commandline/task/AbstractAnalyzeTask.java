package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.Task;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationBuilder;
import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSelection;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.api.source.UrlRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * Abstract base class for all tasks working with rules.
 */
public abstract class AbstractAnalyzeTask extends AbstractStoreTask {

    private static final String CMDLINE_OPTION_R = "r";
    private static final String CMDLINE_OPTION_RULEDIRECTORY = "ruleDirectory";
    private static final String CMDLINE_OPTION_RULESURL = "rulesUrl";
    private static final String CMDLINE_OPTION_GROUPS = "groups";
    private static final String CMDLINE_OPTION_CONSTRAINTS = "constraints";
    private static final String CMDLINE_OPTION_CONCEPTS = "concepts";
    private static final String CMDLINE_OPTION_DEFAULT_GROUP_SEVERITY = "defaultGroupSeverity";
    private static final String CMDLINE_OPTION_DEFAULT_CONCEPT_SEVERITY = "defaultConceptSeverity";
    private static final String CMDLINE_OPTION_DEFAULT_CONSTRAINT_SEVERITY = "defaultConstraintSeverity";

    private URL rulesUrl;
    private String ruleDirectory;

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    protected RuleSet getAvailableRules(Rule rule) throws CliExecutionException {
        List<RuleSource> sources = new ArrayList<>();
        if (rulesUrl != null) {
            sources.add(new UrlRuleSource(rulesUrl));
        } else {
            File selectedDirectory = new File(ruleDirectory);
            // read rules from rules directory
            sources.addAll(readRulesDirectory(selectedDirectory));
            List<RuleSource> ruleSources = pluginRepository.getRulePluginRepository()
                .getRuleSources();
            sources.addAll(ruleSources);
        }
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
        return RuleSelection.select(ruleSet, analyze.groups(), analyze.constraints(), analyze.concepts());
    }

    private List<RuleSource> readRulesDirectory(File rulesDirectory) throws CliExecutionException {
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new RuntimeException(rulesDirectory.getAbsolutePath() + " does not exist or is not a directory.");
        }
        try {
            return FileRuleSource.getRuleSources(rulesDirectory);
        } catch (IOException e) {
            throw new CliExecutionException("Cannot read rules directory: " + rulesDirectory.getAbsolutePath(), e);
        }
    }

    @Override
    public void withOptions(CommandLine options, ConfigurationBuilder configurationBuilder) throws CliConfigurationException {
        String rulesUrl = getOptionValue(options, CMDLINE_OPTION_RULESURL, null);
        if (rulesUrl != null) {
            try {
                this.rulesUrl = new URL(rulesUrl);
            } catch (MalformedURLException e) {
                throw new CliConfigurationException("'" + rulesUrl + "' is not a valid URL.", e);
            }
        }
        ruleDirectory = getOptionValue(options, CMDLINE_OPTION_R, Task.DEFAULT_RULE_DIRECTORY);
        configurationBuilder.with(Analyze.class, Analyze.GROUPS, getOptionValues(options, CMDLINE_OPTION_GROUPS, Collections.<String>emptyList()));
        configurationBuilder.with(Analyze.class, Analyze.CONSTRAINTS, getOptionValues(options, CMDLINE_OPTION_CONSTRAINTS, Collections.<String>emptyList()));
        configurationBuilder.with(Analyze.class, Analyze.CONCEPTS, getOptionValues(options, CMDLINE_OPTION_CONCEPTS, Collections.<String>emptyList()));
        try {
            configurationBuilder.with(Rule.class, Rule.DEFAULT_CONCEPT_SEVERITY,
                Severity.fromValue(getOptionValue(options, CMDLINE_OPTION_DEFAULT_CONCEPT_SEVERITY)));
            configurationBuilder.with(Rule.class, Rule.DEFAULT_CONSTRAINT_SEVERITY,
                Severity.fromValue(getOptionValue(options, CMDLINE_OPTION_DEFAULT_CONSTRAINT_SEVERITY)));
            configurationBuilder.with(Rule.class, Rule.DEFAULT_GROUP_SEVERITY,
                Severity.fromValue(getOptionValue(options, CMDLINE_OPTION_DEFAULT_GROUP_SEVERITY)));
        } catch (RuleException e) {
            throw new CliConfigurationException("Cannot convert severity.", e);
        }
    }

    @Override
    public void addTaskOptions(List<Option> options) {
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_R)
            .withLongOpt(CMDLINE_OPTION_RULEDIRECTORY)
            .withDescription("The directory containing rules.")
            .hasArgs()
            .create(CMDLINE_OPTION_R));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_RULESURL)
            .withDescription("The URL of a file containing rules.")
            .hasArgs()
            .create(CMDLINE_OPTION_RULESURL));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_GROUPS)
            .withDescription("The groups to execute (default='default').")
            .withValueSeparator(',')
            .hasArgs()
            .create(CMDLINE_OPTION_GROUPS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_CONSTRAINTS)
            .withDescription("The constraints to verify.")
            .withValueSeparator(',')
            .hasArgs()
            .create(CMDLINE_OPTION_CONSTRAINTS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_CONCEPTS)
            .withDescription("The concepts to apply.")
            .withValueSeparator(',')
            .hasArgs()
            .create(CMDLINE_OPTION_CONCEPTS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_DEFAULT_GROUP_SEVERITY)
            .withDescription("The default severity for groups.")
            .withValueSeparator(',')
            .hasArgs()
            .create(CMDLINE_OPTION_DEFAULT_GROUP_SEVERITY));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_DEFAULT_CONCEPT_SEVERITY)
            .withDescription("The default severity for concepts.")
            .withValueSeparator(',')
            .hasArgs()
            .create(CMDLINE_OPTION_DEFAULT_CONCEPT_SEVERITY));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_DEFAULT_CONSTRAINT_SEVERITY)
            .withDescription("The default severity for constraints.")
            .withValueSeparator(',')
            .hasArgs()
            .create(CMDLINE_OPTION_DEFAULT_CONSTRAINT_SEVERITY));
    }

    protected Severity getSeverity(String severityValue) throws CliConfigurationException {
        try {
            return Severity.fromValue(severityValue);
        } catch (RuleException e) {
            throw new CliConfigurationException("Unknown severity value " + severityValue);
        }
    }
}
