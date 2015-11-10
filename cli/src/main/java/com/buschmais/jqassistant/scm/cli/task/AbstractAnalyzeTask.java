package com.buschmais.jqassistant.scm.cli.task;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import com.buschmais.jqassistant.core.analysis.api.CompoundRuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.analysis.api.rule.source.FileRuleSource;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.api.rule.source.UrlRuleSource;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.scm.cli.CliConfigurationException;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import com.buschmais.jqassistant.scm.cli.Log;
import com.buschmais.jqassistant.scm.cli.Task;

/**
 * Abstract base class for all tasks working with rules.
 */
public abstract class AbstractAnalyzeTask extends AbstractTask {

    private static final String CMDLINE_OPTION_R = "r";
    private static final String CMDLINE_OPTION_RULEDIRECTORY = "ruleDirectory";
    private static final String CMDLINE_OPTION_RULESURL = "rulesUrl";
    private static final String CMDLINE_OPTION_GROUPS = "groups";
    private static final String CMDLINE_OPTION_CONSTRAINTS = "constraints";
    private static final String CMDLINE_OPTION_CONCEPTS = "concepts";

    private static final Log LOG = Log.getLog();

    private final RuleSetReader ruleSetReader = new CompoundRuleSetReader();
    private URL rulesUrl;
    private String ruleDirectory;
    private List<String> conceptIds;
    private List<String> constraintIds;
    private List<String> groupIds;

    protected RuleSet getAvailableRules() throws CliExecutionException {
        List<RuleSource> sources = new ArrayList<>();
        if (rulesUrl != null) {
            sources.add(new UrlRuleSource(rulesUrl));
        } else {
            File selectedDirectory = new File(ruleDirectory);
            // read rules from rules directory
            sources.addAll(readRulesDirectory(selectedDirectory));
            List<RuleSource> ruleSources;
            try {
                ruleSources = pluginRepository.getRulePluginRepository().getRuleSources();
            } catch (PluginRepositoryException e) {
                throw new CliExecutionException("Cannot get rule plugin repository.", e);
            }
            sources.addAll(ruleSources);
        }
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        try {
            ruleSetReader.read(sources, ruleSetBuilder);
        } catch (RuleException e) {
            throw new CliExecutionException("Cannot read rules.", e);
        }
        return ruleSetBuilder.getRuleSet();
    }

    /**
     * Return the selection of rules.
     * 
     * @param ruleSet
     *            The rule set.
     * @return The selection of rules.
     */
    protected RuleSelection getRuleSelection(RuleSet ruleSet) throws CliExecutionException {
        return RuleSelection.Builder.select(ruleSet, groupIds, constraintIds, conceptIds);
    }

    private List<RuleSource> readRulesDirectory(File rulesDirectory) throws CliExecutionException {
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new RuntimeException(rulesDirectory.getAbsolutePath() + " does not exist or is not a directory.");
        }
        LOG.info("Reading rules from directory " + rulesDirectory.getAbsolutePath());
        try {
            return FileRuleSource.getRuleSources(rulesDirectory);
        } catch (IOException e) {
            throw new CliExecutionException("Cannot read rules directory: " + rulesDirectory.getAbsolutePath(), e);
        }
    }

    @Override
    public void withOptions(CommandLine options) throws CliConfigurationException {
        String rulesUrl = getOptionValue(options, CMDLINE_OPTION_RULESURL, null);
        if (rulesUrl != null) {
            try {
                this.rulesUrl = new URL(rulesUrl);
            } catch (MalformedURLException e) {
                throw new CliConfigurationException("'" + rulesUrl + "' is not a valid URL.", e);
            }
        }
        ruleDirectory = getOptionValue(options, CMDLINE_OPTION_R, Task.DEFAULT_RULE_DIRECTORY);
        groupIds = getOptionValues(options, CMDLINE_OPTION_GROUPS, Collections.<String> emptyList());
        constraintIds = getOptionValues(options, CMDLINE_OPTION_CONSTRAINTS, Collections.<String> emptyList());
        conceptIds = getOptionValues(options, CMDLINE_OPTION_CONCEPTS, Collections.<String> emptyList());
    }

    @Override
    protected void addTaskOptions(List<Option> options) {
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_R).withLongOpt(CMDLINE_OPTION_RULEDIRECTORY).withDescription("The directory containing rules.")
                .hasArgs().create(CMDLINE_OPTION_R));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_RULESURL).withDescription("The URL of a file containing rules.").hasArgs()
                .create(CMDLINE_OPTION_RULESURL));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_GROUPS).withDescription("The groups to execute (default='default').").withValueSeparator(',')
                .hasArgs().create(CMDLINE_OPTION_GROUPS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_CONSTRAINTS).withDescription("The constraints to verify.").withValueSeparator(',').hasArgs()
                .create(CMDLINE_OPTION_CONSTRAINTS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_CONCEPTS).withDescription("The concepts to apply.").withValueSeparator(',').hasArgs()
                .create(CMDLINE_OPTION_CONCEPTS));
    }
}
