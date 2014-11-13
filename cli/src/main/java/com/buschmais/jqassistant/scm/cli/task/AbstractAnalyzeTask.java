package com.buschmais.jqassistant.scm.cli.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.DirectoryWalker;

import com.buschmais.jqassistant.core.analysis.api.CompoundRuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.source.FileRuleSource;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import com.buschmais.jqassistant.scm.cli.JQATask;
import com.buschmais.jqassistant.scm.cli.Log;

/**
 * Abstract base class for all tasks working with rules.
 */
public abstract class AbstractAnalyzeTask extends AbstractJQATask {

    private static final String CMDLINE_OPTION_RULEDIR = "r";
    private static final String CMDLINE_OPTION_GROUPS = "groups";
    private static final String CMDLINE_OPTION_CONSTRAINTS = "constraints";
    private static final String CMDLINE_OPTION_CONCEPTS = "concepts";

    private static final Log LOG = Log.getLog();

    private final RuleSetReader ruleSetReader = new CompoundRuleSetReader();
    private String ruleDirectory;
    private List<String> conceptIds;
    private List<String> constraintIds;
    private List<String> groupIds;

    /**
     * Constructor.
     *
     * @param pluginConfigurationReader
     */
    protected AbstractAnalyzeTask(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
    }

    // copied from AbstractAnalysisMojo
    protected RuleSet getAvailableRules() throws CliExecutionException {
        File selectedDirectory = new File(ruleDirectory);
        List<RuleSource> sources = new ArrayList<>();
        // read rules from rules directory
        List<File> ruleFiles = readRulesDirectory(selectedDirectory);
        for (final File ruleFile : ruleFiles) {
            LOG.debug("Adding rules from file " + ruleFile.getAbsolutePath());
            sources.add(new FileRuleSource(ruleFile));
        }
        List<RuleSource> ruleSources = rulePluginRepository.getRuleSources();
        sources.addAll(ruleSources);
        return ruleSetReader.read(sources);
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

    private List<File> readRulesDirectory(File rulesDirectory) throws CliExecutionException {
        if (rulesDirectory.exists() && !rulesDirectory.isDirectory()) {
            throw new RuntimeException(rulesDirectory.getAbsolutePath() + " does not exist or is not a directory.");
        }
        LOG.info("Reading rules from directory " + rulesDirectory.getAbsolutePath());
        final List<File> ruleFiles = new ArrayList<>();
        try {
            new DirectoryWalker<File>() {

                @Override
                protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
                    if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                        results.add(file);
                    }
                }

                public void scan(File directory) throws IOException {
                    super.walk(directory, ruleFiles);
                }
            }.scan(rulesDirectory);
            return ruleFiles;
        } catch (IOException e) {
            throw new CliExecutionException("Cannot read rules directory: " + rulesDirectory.getAbsolutePath(), e);
        }
    }

    @Override
    public void withOptions(CommandLine options) {
        ruleDirectory = getOptionValue(options, CMDLINE_OPTION_RULEDIR, JQATask.DEFAULT_RULE_DIRECTORY);
        groupIds = getOptionValues(options, CMDLINE_OPTION_GROUPS, Arrays.asList("default"));
        constraintIds = getOptionValues(options, CMDLINE_OPTION_CONSTRAINTS, Collections.<String> emptyList());
        conceptIds = getOptionValues(options, CMDLINE_OPTION_CONCEPTS, Collections.<String> emptyList());
    }

    @Override
    protected void addTaskOptions(List<Option> options) {
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_RULEDIR).withLongOpt("ruleDirectory").withDescription("The directory containing rules.").hasArgs()
                .create(CMDLINE_OPTION_RULEDIR));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_GROUPS).withDescription("The groups to execute (default='default').").withValueSeparator(',')
                .hasArgs().create(CMDLINE_OPTION_GROUPS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_CONSTRAINTS).withDescription("The constraints to verify.").withValueSeparator(',').hasArgs()
                .create(CMDLINE_OPTION_CONSTRAINTS));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_CONCEPTS).withDescription("The concepts to apply.").withValueSeparator(',').hasArgs()
                .create(CMDLINE_OPTION_CONCEPTS));
    }
}
